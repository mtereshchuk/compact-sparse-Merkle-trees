package model;

import lombok.*;
import lombok.experimental.*;
import model.exceptions.*;
import model.node.*;
import model.node.Node;
import model.proof.*;
import model.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static model.utils.Direction.*;
import static model.utils.Utils.distance;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CSMTImpl<V, H> implements CSMT<V, H> {
    @Nullable @NonFinal
    Node<H> root;
    @NotNull Function<V, H> leafHashFunction;
    @NotNull BinaryOperator<H> nodeHashFunction;

    @Override
    public void insert(@NotNull BigInteger key, @NotNull V value) {
        try {
            root = (root == null
                    ? createNode(key, value)
                    : doInsert(root, key, value));
        } catch (KeyExistsException ignored) {}
    }

    @NotNull
    private Node<H> doInsert(
            @NotNull Node<H> node,
            @NotNull BigInteger key,
            @NotNull V value
    ) throws KeyExistsException {
        if (node instanceof LeafNode) {
            if (key.equals(node.getKey())) {
                throw new KeyExistsException();
            }

            val newLeaf = createNode(key, value);
            return key.compareTo(node.getKey()) < 0
                    ? createNode(newLeaf, node)
                    : createNode(node, newLeaf);
        } else {
            val left = ((InnerNode<H>) node).getLeft();
            val right = ((InnerNode<H>) node).getRight();

            val leftDistance = distance(key, left.getKey());
            val rightDistance = distance(key, right.getKey());

            if (leftDistance == rightDistance) {
                val newLeaf = createNode(key, value);
                val minKey = left.getKey().min(right.getKey());

                return key.compareTo(minKey) < 0
                        ? createNode(newLeaf, node)
                        : createNode(node, newLeaf);
            }

            return leftDistance < rightDistance
                    ? createNode(doInsert(left, key, value), right)
                    : createNode(left, doInsert(right, key, value));
        }
    }

    @Override
    public void remove(@NotNull BigInteger key) {
        if (root == null) return;

        if (root instanceof LeafNode) {
            if (key.equals(root.getKey())) {
                root = null;
            }
        } else {
            try {
                root = doRemove((InnerNode<H>) root, key);
            } catch (NoSuchKeyException ignored) {}
        }
    }

    @NotNull
    private Node<H> doRemove(@NotNull InnerNode<H> node, @NotNull BigInteger key) throws NoSuchKeyException {
        val left = node.getLeft();
        val right = node.getRight();

        if (left instanceof LeafNode && key.equals(left.getKey())) {
            return right;
        }

        if (right instanceof  LeafNode && key.equals(right.getKey())) {
            return left;
        }

        val leftDistance = distance(key, left.getKey());
        val rightDistance = distance(key, right.getKey());

        if (leftDistance == rightDistance) {
            throw new NoSuchKeyException();
        }

        if (leftDistance < rightDistance) {
            if (left instanceof  LeafNode) {
                throw new NoSuchKeyException();
            }
            return createNode(doRemove((InnerNode<H>) left, key), right);
        }

        //noinspection ConstantConditions
        if (leftDistance > rightDistance) {
            if (right instanceof LeafNode) {
                throw new NoSuchKeyException();
            }
            return createNode(left, doRemove((InnerNode<H>) right, key));
        }

        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public Proof<V, H> getProof(@NotNull BigInteger key) {
        if (root == null) {
            return new NonMembershipProof<>(null, null);
        }

        if (root instanceof LeafNode) {
            val rootProof = new MembershipProof<V, H>((LeafNode<V, H>) root, Collections.emptyList());

            if (key.equals(root.getKey())) {
                return rootProof;
            }

            return key.compareTo(root.getKey()) < 0
                    ? new NonMembershipProof<>(null, rootProof)
                    : new NonMembershipProof<>(rootProof, null);
        } else {
            val bounds = findBounds((InnerNode<H>) root, key);

            val leftBound = bounds.getFirst();
            val rightBound = bounds.getSecond();

            val castedRoot = (InnerNode<H>) root;

            if (leftBound != null && leftBound.equals(rightBound)) {
                return findProof(castedRoot, leftBound);
            }

            if (leftBound != null && rightBound != null) {
                return new NonMembershipProof<>(findProof(castedRoot, leftBound), findProof(castedRoot, rightBound));
            }

            if (leftBound == null) {
                //noinspection ConstantConditions
                return new NonMembershipProof<>(null, findProof(castedRoot, rightBound));
            }

            return new NonMembershipProof<>(findProof(castedRoot, leftBound), null);
        }
    }

    @NotNull
    private MembershipProof<V, H> findProof(@NotNull InnerNode<H> root, @NotNull BigInteger key) {
        val left = root.getLeft();
        val right = root.getRight();

        val leftDistance = distance(key, left.getKey());
        val rightDistance = distance(key, right.getKey());

        val result = leftDistance < rightDistance
                ? findProof(right, LEFT, left, key)
                : findProof(left, RIGHT, right, key);

        return new MembershipProof<>(result.getSecond(), result.getFirst());
    }

    @NotNull
    private Pair<List<MembershipProof.Entry<H>>, LeafNode<V, H>> findProof(
            @NotNull Node<H> sibling,
            @NotNull Direction direction,
            @NotNull Node<H> node,
            @NotNull BigInteger key
    ) {
        if (node instanceof LeafNode) {
            //noinspection ArraysAsListWithZeroOrOneArgument
            return Pair.of(
                    new ArrayList<>(Arrays.asList(
                            new MembershipProof.Entry<>(
                                    sibling.getHash(),
                                    direction.reverse()
                            )
                    )),
                    (LeafNode<V, H>) node
            );
        } else {
            val left = ((InnerNode<H>) node).getLeft();
            val right = ((InnerNode<H>) node).getRight();

            val leftDistance = distance(key, left.getKey());
            val rightDistance = distance(key, right.getKey());

            val result = leftDistance < rightDistance
                    ? findProof(right, LEFT, left, key)
                    : findProof(left, RIGHT, right, key);


            val proof = result.getFirst();
            //noinspection ConstantConditions
            proof.add(new MembershipProof.Entry<>(sibling.getHash(), direction.reverse()));
            return Pair.of(proof, result.getSecond());
        }
    }

    @NotNull
    private Pair<BigInteger, BigInteger> findBounds(@NotNull InnerNode<H> root, @NotNull BigInteger key) {
        val left = root.getLeft();
        val right = root.getRight();

        val leftDistance = distance(key, left.getKey());
        val rightDistance = distance(key, right.getKey());

        if (leftDistance == rightDistance) {
            return key.compareTo(root.getKey()) > 0
                    ? Pair.of(right.getKey(), null)
                    : Pair.of(null, left.getKey());
        }

        return leftDistance < rightDistance
                ? findBounds(right, LEFT, left, key)
                : findBounds(left, RIGHT, right, key);
    }

    @NotNull
    private Pair<BigInteger, BigInteger> findBounds(
            @NotNull Node<H> sibling,
            @NotNull Direction direction,
            @NotNull Node<H> node,
            @NotNull BigInteger key
    ) {
        if (node instanceof LeafNode) {
            return key.equals(node.getKey())
                    ? Pair.of(key, key)
                    : findBounds(key, node, direction, sibling);
        } else {
            val left = ((InnerNode<H>) node).getLeft();
            val right = ((InnerNode<H>) node).getRight();

            val leftDistance = distance(key, left.getKey());
            val rightDistance = distance(key, right.getKey());

            if (leftDistance == rightDistance) {
                return findBounds(key, node, direction, sibling);
            }

            val result = leftDistance < rightDistance
                    ? findBounds(right, LEFT, left, key)
                    : findBounds(left, RIGHT, right, key);

            if (result.getSecond() == null && direction == LEFT) {
                return Pair.of(result.getFirst(), minInSubtree(sibling));
            }

            if (result.getFirst() == null && direction == RIGHT) {
                return Pair.of(maxInSubtree(sibling), result.getSecond());
            }

            return result;
        }
    }

    @NotNull
    private Pair<BigInteger, BigInteger> findBounds(
            @NotNull BigInteger key,
            @NotNull Node<H> node,
            @NotNull Direction direction,
            @NotNull Node<H> sibling
    ) {
        if (key.compareTo(node.getKey()) > 0 && direction == LEFT) {
            return Pair.of(node.getKey(), minInSubtree(sibling));
        }
        if (key.compareTo(node.getKey()) > 0 && direction == RIGHT) {
            return Pair.of(node.getKey(), null);
        }
        if (key.compareTo(node.getKey()) <= 0 && direction == LEFT) {
            return Pair.of(null, minInSubtree(node));
        } else {
            return Pair.of(maxInSubtree(sibling), minInSubtree(node));
        }
    }

    @NotNull
    private BigInteger maxInSubtree(@NotNull Node<H> node) {
        return node.getKey();
    }

    @NotNull
    private BigInteger minInSubtree(@NotNull Node<H> node) {
        if (node instanceof LeafNode) {
            return node.getKey();
        } else {
            return minInSubtree(((InnerNode<H>) node).getLeft());
        }
    }

    @NotNull
    private Node<H> createNode(@NotNull BigInteger key, @NotNull V value) {
        return new LeafNode<>(key, value, leafHashFunction.apply(value));
    }

    @NotNull
    private Node<H> createNode(@NotNull Node<H> left, @NotNull Node<H> right) {
        return new InnerNode<>(nodeHashFunction.apply(left.getHash(), right.getHash()), left, right);
    }

    @NotNull
    public static CSMTImpl<String, byte[]> createDefault() {
        try {
            val digest = MessageDigest.getInstance("SHA-256");

            final Function<String, byte[]> leafHashFunction = (s) -> {
                val valueBytes = Base64.getDecoder().decode(s);
                return digest.digest(Utils.concatenate(new byte[] {0}, valueBytes));
            };

            final BinaryOperator<byte[]> hashFunction = (left, right) ->
                    digest.digest(Utils.concatenate(new byte[] {1}, left, new byte[] {2}, right));

            return new CSMTImpl<>(leafHashFunction, hashFunction);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }
}
