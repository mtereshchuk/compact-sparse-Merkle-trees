package model;

import model.proof.Proof;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public interface CSMT<V, H> {
    void insert(BigInteger key, @NotNull V value);
    void remove(BigInteger key);
    @NotNull Proof<V, H> getProof(BigInteger key);
}
