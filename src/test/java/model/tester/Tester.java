package model.tester;

import model.CSMT;
import model.CSMTImpl;
import model.TestUtils;
import model.node.LeafNode;
import model.proof.MembershipProof;
import model.proof.Proof;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class Tester {

    @Test
    public void merkleTreeTest() {
        TestFactory.Test test = TestFactory.getRandomMerkelTest(10);
        Merkle<String, byte[]> tree = new Merkle<>(test.hashes.length, test.input,
                TestUtils.LEAF_HASH_FUNCTION,
                TestUtils.NODE_HASH_FUNCTION);

        checkProofs(test, tree);
    }

    @Test
    public void CSMTInsertTest() {
        CSMT<String, byte[]> tree = new CSMTImpl<>(
                TestUtils.LEAF_HASH_FUNCTION,
                TestUtils.NODE_HASH_FUNCTION);

        TestFactory.Test test = TestFactory.makeCSMTTest("abc", "cde", "efg", "ghi", "ijk");
        for (int i = 0; i != test.input.length; i++) {
            tree.insert(BigInteger.valueOf(i), test.input[i]);
        }

        checkProofs(test, tree);
    }

    @Test
    public void CSMTRemoveTest() {
        CSMT<String, byte[]> tree = new CSMTImpl<>(
                TestUtils.LEAF_HASH_FUNCTION,
                TestUtils.NODE_HASH_FUNCTION);

        TestFactory.Test test = TestFactory.makeCSMTTest("abc", "cde", "efg", "ghi", "ijk");
        for (int i = 0; i != test.input.length; i++) {
            tree.insert(BigInteger.valueOf(i), test.input[i]);
        }

        for (int i = 0; i != test.input.length; i++) {
            test.remove(i);
            tree.remove(BigInteger.valueOf(i));
            checkProofs(test, tree);
        }
    }

    @Test
    public void stressRemoveSCMTTest() {
        CSMT<String, byte[]> tree = new CSMTImpl<>(
                TestUtils.LEAF_HASH_FUNCTION,
                TestUtils.NODE_HASH_FUNCTION);

        TestFactory.Test test = TestFactory.getRandomCSMTTest(1000);
        for (int i = 0; i != test.input.length; i++) {
            tree.insert(BigInteger.valueOf(i), test.input[i]);
        }

        for (int i = 0; i != test.input.length; i++) {
            test.remove(i);
            tree.remove(BigInteger.valueOf(i));
            checkProofs(test, tree);
        }
    }

    @Test
    public void stressInsertCSMTTest() {
        CSMT<String, byte[]> tree = new CSMTImpl<>(
                TestUtils.LEAF_HASH_FUNCTION,
                TestUtils.NODE_HASH_FUNCTION);

        TestFactory.Test test = TestFactory.getRandomCSMTTest(500_000);
        for (int i = 0; i != test.input.length; i++) {
            tree.insert(BigInteger.valueOf(i), test.input[i]);
        }

        checkProofs(test, tree);
    }

    private void checkProofs(TestFactory.Test test, CSMT<String, byte[]> tree) {
        final int high = TestUtils.log2(test.input.length);

        for (int i = 0; i != test.input.length; i++) {
            Proof proof = tree.getProof(BigInteger.valueOf(i));

            if (proof instanceof MembershipProof) {
                MembershipProof membershipProof = (MembershipProof) proof;
                LeafNode leaf = membershipProof.getNode();

                assertArrayEquals(test.hashes[0][i], (byte[]) leaf.getHash());

                int index = i;
                Iterator proofIterator = membershipProof.getProof().iterator();
                for (int h = 0; h != high; h++) {
                    int currIndex = index % 2 == 0 ? index + 1 : index - 1;
                    byte[] testHash = test.hashes[h][currIndex];
                    if (testHash != null) {
                        byte[] currHash = (byte[]) ((MembershipProof.Entry) proofIterator.next()).getHash();
                        assertArrayEquals("at index=" + i + " at level: " + h, testHash, currHash);
                    }
                    index /= 2;
                }
            }
        }
    }

    private void checkProofs(TestFactory.Test test, Merkle<String, byte[]> merkleTree) {
        final int high = TestUtils.log2(test.input.length);

        for (int i = 0; i != test.input.length; i++) {
            MerkleProof proof = merkleTree.getProof(i);

            assertArrayEquals(test.hashes[0][i], (byte[]) proof.getTargetByteArray());
            List neighbors = proof.getNeighbors();
            int index = i;

            for (int h = 0; h != high; h++) {
                int currIndex = index % 2 == 0 ? index + 1 : index - 1;
                byte[] currHash = (byte[]) neighbors.get(h);

                assertArrayEquals("at index=" + i + " at level: " + h,
                        test.hashes[h][currIndex], currHash);
                index /= 2;
            }
        }
    }

    @Test
    public void timeTest() {
        System.out.println("time test:");
        for (int numberOfElements = 100_000; numberOfElements <= 800_000; numberOfElements *= 2) {
            BigInteger[] keys = new BigInteger[numberOfElements];
            String[] input = new String[numberOfElements];
            final int high = (int) Math.ceil(TestUtils.log2(
                    TestUtils.toPow2(input.length))) + 1;
            for (int i = 0; i != numberOfElements; i++) {
                keys[i] = BigInteger.valueOf(i);
                input[i] = TestFactory.getRandomString(10);
            }

            Merkle<String, byte[]> treeMerkle = new Merkle<>(high, input,
                    TestUtils.LEAF_HASH_FUNCTION,
                    TestUtils.NODE_HASH_FUNCTION);

            long time = -System.currentTimeMillis();
            for (int i = 0; i != numberOfElements; i++) {
                treeMerkle.getProof(i);
            }
            System.out.println(String.format("for %d elements Merkle tree: %d",
                    numberOfElements, System.currentTimeMillis() + time));

            CSMT<String, byte[]> treeCSMT = new CSMTImpl<>(TestUtils.LEAF_HASH_FUNCTION, TestUtils.NODE_HASH_FUNCTION);
            for (int i = 0; i != numberOfElements; i++) {
                treeCSMT.insert(keys[i], input[i]);
            }

            time = -System.currentTimeMillis();
            for (int i = 0; i != numberOfElements; i++) {
                treeCSMT.getProof(keys[i]);
            }
            System.out.println(String.format("for %d elements CSMT: %d",
                    numberOfElements, System.currentTimeMillis() + time));
            System.out.println();
        }
    }

    @Test
    public void KeyTimeTest() {
        final int step = 50_000;
        System.out.println("key time test step: " + step);
        for (int count = 100_000; count < 6_000_000; count *= 2) {
            BigInteger[] keys = new BigInteger[count];
            String[] input = new String[count];
            final int high = (int) Math.ceil(TestUtils.log2(
                    TestUtils.toPow2(input.length))) + 1;

            final int numberOfElements = count / step;
            System.out.println(String.format("for %d input elements with step %d and %d operations",
                    numberOfElements, step, numberOfElements * 10000));
            for (int i = 0; i != count; i++) {
                keys[i] = BigInteger.valueOf(i);
                if (i % step == 0) {
                    input[i] = TestFactory.getRandomString(7);
                }
            }

            Merkle<String, byte[]> treeMerkle = new Merkle<>(high, input,
                    TestUtils.LEAF_HASH_FUNCTION,
                    TestUtils.NODE_HASH_FUNCTION);

            long time = -System.currentTimeMillis();
            for (int k = 0; k != 10000; k++) {
                for (int i = 0; i < count; i += step) {
                    treeMerkle.getProof(i);
                }
            }

            System.out.println(String.format("Merkle tree has %d nulls, time: %d",
                    treeMerkle.countNulls(), System.currentTimeMillis() + time));

            CSMT<String, byte[]> treeCSMT = new CSMTImpl<>(TestUtils.LEAF_HASH_FUNCTION, TestUtils.NODE_HASH_FUNCTION);
            for (int i = 0; i < count; i += step) {
                treeCSMT.insert(keys[i], input[i]);
            }
            time = -System.currentTimeMillis();
            for (int k = 0; k != 10000; k++) {
                for (int i = 0; i < count; i += step) {
                    treeCSMT.getProof(keys[i]);
                }
            }
            System.out.println("CSMT time: " + (System.currentTimeMillis() + time));
            System.out.println();
        }
    }
}
