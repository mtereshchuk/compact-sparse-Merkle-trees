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
        for (int numberOfElements = 65000; numberOfElements < 530000; numberOfElements *= 2) {
            TestFactory.Test test = TestFactory.getRandomMerkelTest(numberOfElements);
            BigInteger[] keys = new BigInteger[numberOfElements];
            for (int i = 0; i != numberOfElements; i++) {
                keys[i] = BigInteger.valueOf(i);
            }

            Merkle<String, byte[]> treeMerkle = new Merkle<>(test.hashes.length, test.input,
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
                treeCSMT.insert(keys[i], test.input[i]);
            }
            time = -System.currentTimeMillis();
            for (int i = 0; i != numberOfElements; i++) {
                treeCSMT.getProof(keys[i]);
            }
            System.out.println(String.format("for %d elements CSMT: %d",
                    numberOfElements, System.currentTimeMillis() + time));
        }
    }
}
