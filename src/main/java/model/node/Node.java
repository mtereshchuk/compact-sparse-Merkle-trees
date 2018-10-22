package model.node;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

@Value
@NonFinal
public class Node<H> {
    int key;
    @NotNull H hash;
}
