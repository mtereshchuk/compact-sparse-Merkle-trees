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
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class Tester {

    @Test
    public void merkleTreeTest() {
        TestFactory.Test test = TestFactory.getRandomTest(10);
        MerkleTree merkleTree = new MerkleTree(test.input);

        checkProofs(test, merkleTree);
    }

    @Test
    public void CSMTTest() {
        CSMT<String, byte[]> tree = new CSMTImpl<>(
                TestUtils.LEAF_HASH_FUNCTION,
                TestUtils.NODE_HASH_FUNCTION);

        TestFactory.Test test = TestFactory.getRandomTest(5);
        for (int i = 0; i != test.input.length; i++) {
            tree.insert(BigInteger.valueOf(i), test.input[i]);
        }

        checkProofs(test, tree);
    }

    @Test
    public void stressCSMTTest() {
        CSMT<String, byte[]> tree = new CSMTImpl<>(
                TestUtils.LEAF_HASH_FUNCTION,
                TestUtils.NODE_HASH_FUNCTION);

        TestFactory.Test test = TestFactory.getRandomTest(500_000);
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
                List proofList = membershipProof.getProof();
                byte[] currHash = (byte[]) ((MembershipProof.Entry) proofList.get(0)).getHash();
                for (int h = 0; h != high; h++) {
                    int currIndex = index % 2 == 0 ? index + 1 : index - 1;
                    assertArrayEquals("at index=" + i + " at level: " + h,
                            test.hashes[h][currIndex], currHash);
                    currHash = (byte[]) ((MembershipProof.Entry) proofList.get(h + 1)).getHash();
                    index /= 2;
                }
            } else {
                NonMembershipProof nonMembershipProof = (NonMembershipProof) proof;
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
