package model.node;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode(callSuper = true)
@Value
public class LeafNode<V, H> extends Node<H> {
    V value;

    public LeafNode(int key, @Nullable V value, @NotNull H hash) {
        super(key, hash);
        this.value = value;
    }
}
