package model.node;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

@Value
@NonFinal
public class Node<H> {
    @NotNull BigInteger key;
    @NotNull H hash;
}
