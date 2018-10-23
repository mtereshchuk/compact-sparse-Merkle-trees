package model;

import java.util.function.BinaryOperator;
import java.util.function.Function;

public final class TestUtils {

    public static final Function<Integer, Integer> SIMPLE_LEAF_FUNC = (it) -> it;
    public static final BinaryOperator<Integer> SIMPLE_NODE_FUNC = (a, b) -> a + b;

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
}
