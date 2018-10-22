package model.node;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
@Value
public class LeafNode<V, H> extends Node<H> {
    @NotNull V value;

    public LeafNode(@NotNull BigInteger key, @NotNull V value, @NotNull H hash) {
        super(key, hash);
        this.value = value;
    }
}
