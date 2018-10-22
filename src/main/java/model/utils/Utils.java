package model.utils;

import com.google.common.math.BigIntegerMath;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.math.RoundingMode;

@UtilityClass
public class Utils {
    public static int distance(@NotNull BigInteger key1, @NotNull BigInteger key2) {
        return log2(key1.xor(key2));
    }

    private static int log2(@NotNull BigInteger x) {
        if (x.equals(BigInteger.ZERO)) {
            return 0;
        }
        return BigIntegerMath.log2(x, RoundingMode.FLOOR) + 1;
    }

    @NotNull
    public static byte[] concatenate(@NotNull byte[]... arrays) {
        var len = 0;
        for (val array : arrays) {
            len += array.length;
        }

        val newArray = new byte[len];
        var offset = 0;
        for (val array : arrays) {
            System.arraycopy(array, 0, newArray, offset, array.length);
            offset += array.length;
        }

        return newArray;
    }
}
