package model.tester;

import model.Converter;
import model.TestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MerkleTree {

    private Node[] leafs;
    private Map<Integer, Node> neighbors;

    public MerkleTree(String[] input) {
        final int size = TestUtils.toPow2(input.length);

        leafs = new Node[size];
        neighbors = new HashMap<>();
        final int high = TestUtils.log2(size) + 1;

        for (int i = 0; i != input.length; i++) {
            leafs[i] = new Node(i);
            leafs[i].hash = Converter.encode(input[i]);
        }

        Node[] temp = leafs;
        int currId = leafs.length;

        for (int i = 1; i != high; i++) {

            final int count = (int) Math.floor(size / Math.pow(2, i - 1));
            Node[] nextNodes = new Node[count];

            for (int j = 0; j < count; j += 2) {
                Node node1 = temp[j];
                Node node2 = temp[j + 1];

                Node node = new Node(currId + j / 2);
                node.left = node1;
                node.right = node2;

                byte[] hash1 = null;
                byte[] hash2 = null;

                if (node1 != null) {
                    neighbors.put(node1.id, node2);
                    node1.parent = node;
                    hash1 = node1.hash;
                }

                if (node2 != null) {
                    neighbors.put(node2.id, node1);
                    node2.parent = node;
                    hash2 = node2.hash;
                }

                node.hash = Converter.encode(hash1, hash2);
                nextNodes[j / 2] = node;
            }
            currId += nextNodes.length;
            temp = nextNodes;
        }
    }

    public MerkleProof getProof(int id) {
        List<Node> list = new ArrayList<>();
        if (leafs[id] != null) {
            Node curr = leafs[id];

            while (curr != null) {
                list.add(neighbors.get(curr.id));
                curr = curr.parent;
            }
        }
        return new MerkleProof(leafs[id], list);
    }

    public class Node {
        final int id;
        byte[] hash;
        Node parent, left, right;

        public Node(int id) {
            this.id = id;
        }
    }
}
