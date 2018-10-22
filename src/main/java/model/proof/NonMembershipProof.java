package model.proof;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode(callSuper = false)
@Value
public class NonMembershipProof<V, H> extends Proof<V, H> {
    @Nullable MembershipProof<V, H> leftBoundProof;
    @Nullable MembershipProof<V, H> rightBoundProof;
}
