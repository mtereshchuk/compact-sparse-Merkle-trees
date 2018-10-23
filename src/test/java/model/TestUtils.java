package model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public final class TestUtils {

    public static final Function<Integer, Integer> SIMPLE_LEAF_FUNC = (it) -> it;
    public static final BinaryOperator<Integer> SIMPLE_NODE_FUNC = (a, b) -> a + b;

    private static byte[] zero = {0b00000000};
    private static byte[] one = {0b00000001};
    private static byte[] two = {0b00000010};

    private static Base64.Encoder encoder = Base64.getEncoder();
    private static Base64.Decoder decoder = Base64.getDecoder();

    public static final Function<String, byte[]> LEAF_HASH_FUNCTION = Converter::encode;
    public static final BinaryOperator<byte[]> NODE_HASH_FUNCTION = Converter::encode;

    public static int toPow2(final int value) {
        int highestOneBit = Integer.highestOneBit(value);
        if (value == highestOneBit) {
            return value;
        }
        return highestOneBit << 1;
    }

    public static int log2(final int x) {
        return (int) Math.ceil(Math.log(x) / Math.log(2));
    }


    public static String countLeafHash(String value) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return encoder.encodeToString(digest.digest(concatenateByteArrays(zero, value.getBytes())));
    }

    public static String countHash(String left, String right) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        if (left.equals("null") && !right.equals("null")) {
            byte[] bytes = digest.digest(concatenateByteArrays(one, two, decoder.decode(right)));
            return encoder.encodeToString(bytes);
        } else if (!left.equals("null") && right.equals("null")) {
            byte[] bytes = digest.digest(concatenateByteArrays(one, decoder.decode(left), two));
            return encoder.encodeToString(bytes);
        } else if (!left.equals("null") && !right.equals("null")) {
            byte[] bytes = digest.digest(concatenateByteArrays(one, decoder.decode(left), two, decoder.decode(right)));
            return encoder.encodeToString(bytes);
        }
        return "null";
    }

    private static byte[] concatenateByteArrays(byte[]... bytes) {
        int length = 0;
        for (byte[] seg : bytes)
            length += seg.length;
        byte[] concatenated = new byte[length];
        int index = 0;
        for (byte[] seq : bytes) {
            for (byte b : seq) {
                concatenated[index] = b;
                index++;
            }
        }
        return concatenated;
    }


}
