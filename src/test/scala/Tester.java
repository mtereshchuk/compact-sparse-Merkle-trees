import implementation.Tree;
import implementation.adt.ProofResult;
import org.junit.Test;
import scala.math.BigInt;

import static org.junit.Assert.assertNotNull;

public class Tester {

    @Test
    public void CSMTInsertTestScala() {
        Tree tree = new Tree();
        String[] input = {"abc", "cde", "efg", "ghi", "ijk"};
        for (int i = 0; i != input.length; i++) {
            tree.insert(BigInt.apply(i), input[i]);
        }
        checkProof(tree, input.length);
    }

    private void checkProof(Tree tree, final int numberOfElements) {
        for (int i = 0; i != numberOfElements; i++) {
            implementation.adt.MembershipProof proof = tree.getProof(BigInt.apply(i));
            ProofResult membershipProof = (ProofResult) proof;

            assertNotNull(proof);
            assertNotNull(membershipProof);
//                int index = i;
//                Iterator proofIterator = membershipProof.proof().iterator();
//                for (int h = 0; h != high; h++) {
//                    int currIndex = index % 2 == 0 ? index + 1 : index - 1;
//                    byte[] testHash = test.hashes[h][currIndex];
//                    if (testHash != null) {
//                        byte[] currHash = (byte[]) ((Tuple2<String, String>) proofIterator.next())._1.getBytes();
//                        assertArrayEquals("at index=" + i + " at level: " + h, testHash, currHash);
//                    }
//                    index /= 2;
//                }
        }
    }
}

