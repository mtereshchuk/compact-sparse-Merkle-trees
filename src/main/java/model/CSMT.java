package model;

import model.proof.Proof;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public interface CSMT<V, H> {
    void insert(@NotNull BigInteger key, @NotNull V value);
    void remove(@NotNull BigInteger key);
    @NotNull Proof<V, H> getProof(@NotNull BigInteger key);
}
