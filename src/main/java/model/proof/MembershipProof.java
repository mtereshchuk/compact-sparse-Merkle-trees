package model.proof;

import lombok.EqualsAndHashCode;
import lombok.Value;
import model.node.LeafNode;
import model.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@EqualsAndHashCode(callSuper = false)
@Value
public class MembershipProof<V, H> extends Proof<V, H> {
    @NotNull LeafNode<V, H> node;
    @NotNull List<Entry<H>> proof;


    @Value
    public static class Entry<H> {
        @NotNull H hash;
        @NotNull Direction direction;
    }
}