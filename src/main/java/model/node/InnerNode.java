package model.node;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@Value
public class InnerNode<H> extends Node<H> {
    @NotNull Node<H> left;
    @NotNull Node<H> right;

    public InnerNode(@NotNull H hash, @NotNull Node<H> left, @NotNull Node<H> right) {
        super(left.getKey().max(right.getKey()), hash);
        this.left = left;
        this.right = right;
    }
}
