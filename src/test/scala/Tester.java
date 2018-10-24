import implementation.Tree;
import implementation.adt.MembershipProof;
import implementation.adt.ProofResult;
import model.TestUtils;
import model.tester.Merkle;
import model.tester.MerkleProof;
import org.junit.Test;
import scala.math.BigInt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Tester {

    @Test
    public void CSMTInsertTestScala() {
        Tree csmt = new Tree();
        String[] input = {"abc", "cde", "efg", "ghi", "ijk"};
        for (int i = 0; i != input.length; i++) {
            csmt.insert(BigInt.apply(i), input[i]);
        }
        final int high = (int) Math.ceil(TestUtils.log2(
                TestUtils.toPow2(input.length))) + 1;
        Merkle<String, String> merkleTree = new Merkle<>(high, input,
                TestUtils.SCALA_LEAF_FUNC,
                TestUtils.SCALA_NODE_FUNC);
        checkProof(csmt, merkleTree, input.length, high);
    }

    private void checkProof(Tree tree, Merkle<String, String> merkleTree, final int numberOfElements, final int high) {
        for (int i = 0; i != numberOfElements; i++) {
            MembershipProof proof = tree.getProof(BigInt.apply(i));
            MerkleProof<String> merkleProof = merkleTree.getProof(i);

            ProofResult membershipProof = (ProofResult) proof;
            assertNotNull(membershipProof);
            assertEquals(merkleProof.getTargetByteArray(), membershipProof.hash());
        }
    }
}

