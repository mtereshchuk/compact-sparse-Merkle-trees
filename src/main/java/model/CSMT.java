package model;

import model.proof.Proof;
import org.jetbrains.annotations.NotNull;

public interface CSMT<V, H> {
    void insert(int key, @NotNull V value);
    void remove(int key);
    @NotNull Proof<V, H> getProof(int key);
}
