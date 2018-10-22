package model;

import model.proof.Proof;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CSMT<V, H> {
    void insert(int key, @Nullable V value);
    void remove(int key);
    @NotNull Proof<V, H> getProof(int key);
}
