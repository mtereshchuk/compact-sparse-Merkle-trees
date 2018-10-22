package model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import lombok.var;
import model.exceptions.KeyExistsException;
import model.exceptions.NoSuchKeyException;
import model.node.InnerNode;
import model.node.LeafNode;
import model.node.Node;
import model.proof.MembershipProof;
import model.proof.NonMembershipProof;
import model.proof.Proof;
import model.utils.Direction;
import model.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static model.utils.Direction.LEFT;
import static model.utils.Direction.RIGHT;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class CSMTImpl<V, H> implements CSMT<V, H> {
    @Nullable @NonFinal Node<H> root;
    Function<V, H> leafHashFunction;
    BinaryOperator<H> nodeHashFunction;

    @Override
    public void insert(int key, @Nullable V value) {
        try {
            root = (root == null
                    ? createNode(key, value)
                    : doInsert(root, key, value));
        } catch (KeyExistsException ignored) {}
    }

    @NotNull
    private Node<H> doInsert(@NotNull Node<H> node, int key, @Nullable V value) throws KeyExistsException {
        if (node instanceof LeafNode) {
            if (key == node.getKey()) {
                throw new KeyExistsException();
            }

            val newLeaf = createNode(key, value);
            return key < node.getKey()
                    ? createNode(newLeaf, node)
                    : createNode(node, newLeaf);
        } else {
            val left = ((InnerNode<H>) node).getLeft();
            val right = ((InnerNode<H>) node).getRight();

            val leftDistance = distance(key, left.getKey());
            val rightDistance = distance(key, right.getKey());

            if (leftDistance == rightDistance) {
                val newLeaf = createNode(key, value);
                val minKey = min(left.getKey(), right.getKey());

                return key < minKey
                        ? createNode(newLeaf, node)
                        : createNode(node, newLeaf);
            }

            return leftDistance < rightDistance
                    ? createNode(doInsert(left, key, value), right)
                    : createNode(left, doInsert(right, key, value));
        }
    }

    @Override
    public void remove(int key) {
        if (root == null) return;

        if (root instanceof LeafNode) {
            if (key == root.getKey()) {
                root = null;
            }
        } else {
            try {
                root = doRemove((InnerNode<H>) root, key);
            } catch (NoSuchKeyException ignored) {}
        }
    }

    @NotNull
    private Node<H> doRemove(@NotNull InnerNode<H> node, int key) throws NoSuchKeyException {
        val left = node.getLeft();
        val right = node.getRight();

        if (left instanceof LeafNode && key == left.getKey()) {
            return right;
        }

        if (right instanceof  LeafNode && key == right.getKey()) {
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
    public Proof<V, H> getProof(int key) {
        if (root == null) {
            return new NonMembershipProof<>(null, null);
        } else

        if (root instanceof LeafNode) {
            val rootProof = new MembershipProof<V, H>((LeafNode<V, H>) root, Collections.emptyList());

            if (key == root.getKey()) {
                return rootProof;
            }

            return key < root.getKey()
                    ? new NonMembershipProof<>(null, rootProof)
                    : new NonMembershipProof<>(rootProof, null);
        } else {
            val bounds = findBounds((InnerNode<H>) root, key);

            val leftBound = bounds.getFirst();
            val rightBound = bounds.getSecond();

            if (leftBound != null && leftBound.equals(rightBound)) {
                return findProof((InnerNode<H>) root, leftBound);
            }

            if (leftBound != null && rightBound != null) {
                return new NonMembershipProof<>(findProof((InnerNode<H>) root, leftBound), findProof((InnerNode<H>) root, rightBound));
            }

            if (leftBound == null) {
                return new NonMembershipProof<>(null, findProof((InnerNode<H>) root, rightBound));
            }

            return new NonMembershipProof<>(findProof((InnerNode<H>) root, leftBound), null);
        }
    }

    @NotNull
    private MembershipProof<V, H> findProof(@NotNull InnerNode<H> root, int key) {
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
            int key
    ) {
        if (node instanceof LeafNode) {
            //noinspection ArraysAsListWithZeroOrOneArgument
            return Pair.of(
                    Arrays.asList(
                            new MembershipProof.Entry<>(
                                    sibling.getHash(),
                                    direction.reverse()
                            )
                    ),
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
            proof.add(new MembershipProof.Entry<>(sibling.getHash(), direction.reverse()));
            return Pair.of(proof, result.getSecond());
        }
    }

    @NotNull
    private Pair<Integer, Integer> findBounds(@NotNull InnerNode<H> root, int key) {
        val left = root.getLeft();
        val right = root.getRight();

        val leftDistance = distance(key, left.getKey());
        val rightDistance = distance(key, right.getKey());

        if (leftDistance == rightDistance) {
            return key > root.getKey()
                    ? Pair.of(right.getKey(), null)
                    : Pair.of(null, left.getKey());
        }

        return leftDistance < rightDistance
                ? findBounds(right, LEFT, left, key)
                : findBounds(left, RIGHT, right, key);
    }

    @NotNull
    private Pair<Integer, Integer> findBounds(
            @NotNull Node<H> sibling,
            @NotNull Direction direction,
            @NotNull Node<H> node,
            int key
    ) {
        if (node instanceof LeafNode) {
            return key == node.getKey()
                    ? Pair.of(key, key)
                    : findBounds(key, node.getKey(), direction, sibling);
        } else {
            val left = ((InnerNode<H>) node).getLeft();
            val right = ((InnerNode<H>) node).getRight();

            val leftDistance = distance(key, left.getKey());
            val rightDistance = distance(key, right.getKey());

            if (leftDistance == rightDistance) {
                return findBounds(key, node.getKey(), direction, sibling);
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
    private Pair<Integer, Integer> findBounds(
            int key,
            int nodeKey,
            @NotNull Direction direction,
            @NotNull Node<H> sibling
    ) {
        if (key > nodeKey && direction == LEFT) {
            return Pair.of(nodeKey, minInSubtree(sibling));
        }
        if (key > nodeKey && direction == RIGHT) {
            return Pair.of(nodeKey, null);
        }
        if (key <= nodeKey && direction == LEFT) {
            return Pair.of(null, nodeKey);
        } else {
            return Pair.of(maxInSubtree(sibling), nodeKey);
        }
    }

    private int maxInSubtree(@NotNull Node<H> node) {
        return node.getKey();
    }

    private int minInSubtree(@NotNull Node<H> node) {
        if (node instanceof LeafNode) {
            return node.getKey();
        } else {
            return minInSubtree(((InnerNode<H>) node).getLeft());
        }
    }

    public static int distance(int key1, int key2) {
        return log2(key1 ^ key2);
    }

    private static int log2(int x) {
        var i = 0;
        while ((1 << i) <= x) i++;
        return i;
    }

    @NotNull
    private Node<H> createNode(int key, @Nullable V value) {
        return new LeafNode<>(key, value, leafHashFunction.apply(value));
    }

    @NotNull
    private Node<H> createNode(@NotNull Node<H> left, @NotNull Node<H> right) {
        return new InnerNode<>(nodeHashFunction.apply(left.getHash(), right.getHash()), left, right);
    }
}
