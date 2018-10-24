package model.proof;

import java.math.BigInteger;

public class Key {
    public final BigInteger value;
    public Key left, right;

    public Key(BigInteger key) {
        this.value = key;
    }
}
