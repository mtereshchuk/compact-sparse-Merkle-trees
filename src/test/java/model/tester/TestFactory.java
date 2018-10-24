package model.tester;

import model.Converter;
import model.TestUtils;

import java.util.Random;

import static model.TestUtils.toPow2;

public final class TestFactory {

    private static final Random random = new Random();

    public static Test getRandomMerkelTest(final int numberOfElements) {
        String[] input = new String[numberOfElements];

        for (int i = 0; i != numberOfElements; i++) {
            input[i] = getRandomString(10);
        }

        return makeMerkleTest(input);
    }

    public static Test getRandomCSMTTest(final int numberOfElements) {
        String[] input = new String[numberOfElements];

        for (int i = 0; i != numberOfElements; i++) {
            input[i] = getRandomString(10);
        }

        return makeCSMTTest(input);
    }

    public static Test makeCSMTTest(String... input) {
        final int size = toPow2(input.length);
        byte[][][] levels = makeByteArray(size, input);

        for (int i = 1; i != levels.length; i++) {
            final int count = (int) Math.floor(size / Math.pow(2, i - 1));
            for (int j = 0; j < count; j += 2) {
                final byte[] bytes1 = levels[i - 1][j];
                final byte[] bytes2 = levels[i - 1][j + 1];

                if (bytes1 == null && bytes2 == null) {
                    levels[i][j / 2] = null;
                } else if (bytes1 == null) {
                    levels[i][j / 2] = bytes2;
                } else if (bytes2 == null) {
                    levels[i][j / 2] = bytes1;
                } else {
                    levels[i][j / 2] = Converter.encode(bytes1, bytes2);
                }
            }
        }

        return new Test(input, levels);
    }

    public static Test makeMerkleTest(String... input) {
        final int size = toPow2(input.length);
        byte[][][] levels = makeByteArray(size, input);

        for (int i = 1; i != levels.length; i++) {
            final int count = (int) Math.floor(size / Math.pow(2, i - 1));
            for (int j = 0; j < count; j += 2) {
                final byte[] bytes1 = levels[i - 1][j];
                final byte[] bytes2 = levels[i - 1][j + 1];

                levels[i][j / 2] = Converter.encode(bytes1, bytes2);
            }
        }

        return new Test(input, levels);
    }

    private static byte[][][] makeByteArray(final int size, String[] input) {
        final int high = (int) Math.ceil(TestUtils.log2(size)) + 1;
        byte[][][] levels = new byte[high][][];

        for (int i = 0; i != high; i++) {
            levels[i] = new byte[size / (int) Math.pow(2, i)][];
        }

        for (int i = 0; i != input.length; i++) {
            levels[0][i] = Converter.encode(input[i]);
        }

        return levels;
    }

    public static String getRandomString(final int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i != length; i++) {
            builder.append('a' + random.nextInt(26));
        }
        return builder.toString();
    }

    public static class Test {
        public final String[] input;
        public final byte[][][] hashes;

        public Test(String[] input, byte[][][] hashes) {
            this.input = input;
            this.hashes = hashes;
        }

        public void remove(final int index) {
            hashes[0][index] = null;

            int i = index;
            for (int h = 1; h < hashes.length; h++) {
                byte[] hash1;
                byte[] hash2;

                if (i % 2 == 0) {
                    hash1 = hashes[h - 1][i];
                    hash2 = hashes[h - 1][i + 1];
                } else {
                    hash1 = hashes[h - 1][i - 1];
                    hash2 = hashes[h - 1][i];
                }

                if (hash1 != null && hash2 != null) {
                    hashes[h][i / 2] = Converter.encode(hash1, hash2);
                } else if (hash1 != null) {
                    hashes[h][i / 2] = hash1;
                } else if (hash2 != null) {
                    hashes[h][i / 2] = hash2;
                } else {
                    hashes[h][i / 2] = null;
                }

                i /= 2;
            }
        }
    }
}
