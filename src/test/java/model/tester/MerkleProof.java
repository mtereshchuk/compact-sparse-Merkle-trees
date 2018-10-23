package model.tester;

import java.util.List;

public class MerkleProof {
    MerkleTree.Node item;
    List<MerkleTree.Node> neighbors;

    public MerkleProof(MerkleTree.Node item, List<MerkleTree.Node> neighbors) {
        this.item = item;
        this.neighbors = neighbors;
    }
}
