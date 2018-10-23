import model.TestUtils;
import model.adt.MembershipProof;
import model.tester.TestFactory;
import org.junit.Test;
import scala.math.BigInt;

import java.math.BigInteger;
import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;

public class Tester {

    @Test
    public void test() {
        Tree tree = new Tree();

        TestFactory.Test test = TestFactory.getRandomMerkelTest(5);
        for (int i = 0; i != test.input.length; i++) {
            tree.insert(new BigInt(BigInteger.valueOf(i)), test.input[i]);
        }


    }

    //    private void checkProofs(TestFactory.Test test, Tree tree) {
//        final int high = TestUtils.log2(test.input.length);
//
//        for (int i = 0; i != test.input.length; i++) {
//            MembershipProof proof = tree.getProof(new BigInt(BigInteger.valueOf(i)));
//
//            LeafNode leaf = proof.getNode();
//
//            assertArrayEquals(test.hashes[0][i], (byte[]) leaf.getHash());
//
//            int index = i;
//            Iterator proofIterator = membershipProof.getProof().iterator();
//            for (int h = 0; h != high; h++) {
//                int currIndex = index % 2 == 0 ? index + 1 : index - 1;
//                byte[] testHash = test.hashes[h][currIndex];
//                if (testHash != null) {
//                    byte[] currHash = (byte[]) ((MembershipProof.Entry) proofIterator.next()).getHash();
//                    assertArrayEquals("at index=" + i + " at level: " + h, testHash, currHash);
//                }
//                index /= 2;
//            }
//        }
//    }

}
