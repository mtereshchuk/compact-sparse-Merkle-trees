package model.tester;

import model.Converter;
import model.TestUtils;

import java.util.Random;

import static model.TestUtils.toPow2;

public final class TestFactory {

    private static final Random random = new Random();

    public static Test getRandomTest(final int numberOfElements) {
        final int size = toPow2(numberOfElements);

        final int high = (int) Math.ceil(TestUtils.log2(size)) + 1;
        byte[][][] levels = new byte[high][][];
        String[] input = new String[numberOfElements];

        for (int i = 0; i != high; i++) {
            levels[i] = new byte[size / (int) Math.pow(2, i)][];
        }

        for (int i = 0; i != numberOfElements; i++) {
            input[i] = getRandomString(10);
            levels[0][i] = Converter.encode(input[i]);
        }

        for (int i = 1; i != high; i++) {
            final int count = (int) Math.floor(size / Math.pow(2, i - 1));
            for (int j = 0; j < count; j += 2) {
                final byte[] bytes1 = levels[i - 1][j];
                final byte[] bytes2 = levels[i - 1][j + 1];

                levels[i][j / 2] = Converter.encode(bytes1, bytes2);
            }
        }

        return new Test(input, levels);
    }

    private static String getRandomString(final int length) {
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
    }
}
