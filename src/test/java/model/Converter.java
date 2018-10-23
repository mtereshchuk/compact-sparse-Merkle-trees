package model;

import com.google.common.base.Charsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Converter {

    private static final byte[] ZERO = new byte[]{0b00000000};
    private static final byte[] ONE = new byte[]{0b00000001};
    private static final byte[] TWO = new byte[]{0b00000010};
    private static final byte[] EMPTY_LINE = new byte[]{};

    private static MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] encode(String str) {
        if (str != null) {
            return encode(str.getBytes(Charsets.UTF_8));
        } else {
            return null;
        }
    }

    public static byte[] encode(byte[] bytes1, byte[] bytes2) {
        if (bytes1 != null && bytes2 != null) {
            return encode(combine(bytes1, bytes2));
        } else if (bytes1 != null) {
            return encode(bytes1);
        } else if (bytes2 != null) {
            return encode(bytes2);
        } else {
            return null;
        }
    }

    public static byte[] encode(byte[] bytes) {
        return digest.digest(bytes);
    }

    private static byte[] combine(byte[] bytes1, byte[] bytes2) {
        byte[] bytes = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
        System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);

        return bytes;
    }
}
