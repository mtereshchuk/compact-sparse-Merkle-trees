package model.tester;

import model.CSMT;
import model.CSMTImpl;
import model.TestUtils;
import model.node.LeafNode;
import model.proof.MembershipProof;
import model.proof.NonMembershipProof;
import model.proof.Proof;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;

public class Tester {

    @Test
    public void merkleTreeTest() {
        TestFactory.Test test = TestFactory.getRandomMerkelTest(10);
        MerkleTree merkleTree = new MerkleTree(test.input);

        checkProofs(test, merkleTree);
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

        TestFactory.Test test = TestFactory.getRandomCSMTTest(50_000);
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
            } else {
                NonMembershipProof nonMembershipProof = (NonMembershipProof) proof;
                MembershipProof proofLeft = nonMembershipProof.getLeftBoundProof();
                MembershipProof proofRight = nonMembershipProof.getRightBoundProof();


            }
        }
    }

    private void checkProofs(TestFactory.Test test, MerkleTree merkleTree) {
        final int high = TestUtils.log2(test.input.length);

        for (int i = 0; i != test.input.length; i++) {
            MerkleProof proof = merkleTree.getProof(i);

            assertArrayEquals(test.hashes[0][i], proof.item.hash);
            MerkleTree.Node curr = proof.neighbors.get(0);
            int index = i;

            for (int h = 0; h != high; h++) {
                int currIndex = index % 2 == 0 ? index + 1 : index - 1;
                assertArrayEquals("at index=" + i + " at level: " + h,
                        test.hashes[h][currIndex],
                        curr != null ? curr.hash : null);
                curr = proof.neighbors.get(h + 1);
                index /= 2;
            }
        }
    }
}
