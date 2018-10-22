package model.node;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Node<H> {
    int key;
    H hash;
}
