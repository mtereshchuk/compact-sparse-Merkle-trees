import model.Flags._
import model.adt._
import model.exceptions.{KeyExistsException, NoSuchKeyException}
import model.utils.{Node, TreeUtils}

import scala.util.Random

object Main extends App {
  val tree = new Tree
  val big = BigInt("36893488147419103232")
  for (i <- 1 to 1000) {
    val big = BigInt(i)
    tree.insert(big, Random.alphanumeric.take(10).mkString)
  }
  for (i <- 1 to 1000) {
    val big = BigInt(i)
    println(tree.getProof(big))
  }
  for (i <- 1000 to 2000) {
    val big = BigInt(i)
    println(tree.getProof(big))
  }
}

object CSMT {

  def insert(root: Node, k: BigInt, v: String): Node = root match {
    case Node(_, _, _, null, null) =>
      leaf_insert(root, k, v)
    case _ =>
      var left = root.left
      var right = root.right
      val (l_dist, r_dist) = (TreeUtils.distance(k, left.key), TreeUtils.distance(k, right.key))
      val cmp = l_dist - r_dist
      cmp match {
        case c if c == 0 =>
          val new_leaf = TreeUtils.makeNode(k, v)
          val min_key = BigInt(left.key.min(right.key).toString())
          if (k < min_key)
            TreeUtils.makeNode(new_leaf, root)
          else
            TreeUtils.makeNode(root, new_leaf)
        case c if c < 0 =>
          left = insert(left, k, v)
          TreeUtils.makeNode(left, right)
        case c if c > 0 =>
          right = insert(right, k, v)
          TreeUtils.makeNode(left, right)
      }
  }

  def leaf_insert(leaf: Node, k: BigInt, v: String): Node = {
    val new_leaf = TreeUtils.makeNode(k, v)
    val cmp = k - leaf.key
    cmp match {
      case c if c == 0 => throw new KeyExistsException
      case c if c > 0 =>
        TreeUtils.makeNode(leaf, new_leaf)
      case c if c < 0 =>
        TreeUtils.makeNode(new_leaf, leaf)
    }
  }

  def getProof(root: Node, k: BigInt): MembershipProof = {
    val result = getProofImpl(root, k)
    result match {
      case ProofPairList((a, b) :: tail) =>
        val (value, hash) :: proof = ((a, b) :: tail).reverse
        ProofResult(Map("key" -> k.toString(), "value" -> value, "hash" -> hash, "proof" -> proof.toString()))
      case IntFlagNoProof(key, MINRS) => NoProofList(List(getProof(root, key.toString.toInt), null))
      case FlagIntNoProof(MAXLS, key) => NoProofList(List(null, getProof(root, key.toString.toInt)))
      case IntIntNoProof(key1, key2) => NoProofList(List(getProof(root, key1.toString.toInt), getProof(root, key2.toString.toInt)))

    }
  }

  def getProofImpl(root: Node, k: BigInt): MembershipProof = root match {
    case Node(key, value, hash, null, null) => ProofPairList(List((null, null), (value, root.hash)))
    case _ =>
      val left = root.left
      val right = root.right
      val (l_dist, r_dist) = (TreeUtils.distance(k, left.key), TreeUtils.distance(k, right.key))
      val cmp = l_dist - r_dist
      cmp match {
        case 0 =>
          if (k > root.key)
            IntFlagNoProof(right.key, MINRS)
          else
            FlagIntNoProof(MAXLS, left.key)
        case c if c < 0 => getProofImpl(right, "L", left, k)
        case c if c > 0 => getProofImpl(left, "R", right, k)
      }
  }

  def getProofImpl(sibling: Node, direction: String, root: Node, k: BigInt): MembershipProof = root match {
    case Node(key, value, hash, null, null) =>
      if (key == k)
        ProofPairList(List((sibling.hash, reverse(direction)), (value, root.hash)))
      else
        nonMemberShipProof(k, key, direction, sibling)
    case _ =>
      val left = root.left
      val right = root.right
      val (l_dist, r_dist) = (TreeUtils.distance(k, left.key), TreeUtils.distance(k, right.key))
      val cmp = l_dist - r_dist
      cmp match {
        case c if c == 0 => nonMemberShipProof(k, root.key, direction, sibling)
        case c if c < 0 =>
          val result = getProofImpl(right, "L", left, k)
          resultDirectionMatcher(result, direction, sibling, k)
        case c if c > 0 =>
          val result = getProofImpl(left, "R", right, k)
          resultDirectionMatcher(result, direction, sibling, k)
      }
  }

  def resultDirectionMatcher(result: MembershipProof, direction: String, sibling: Node, k: BigInt): MembershipProof = (result, direction) match {
    case (ProofPairList((a, b) :: tail), _) => ProofPairList((sibling.hash, reverse(direction)) :: (a, b) :: tail)
    case (IntFlagNoProof(key, MINRS), "L") => IntIntNoProof(key, minInSubtree(sibling))
    case (FlagIntNoProof(MAXLS, key), "R") => IntIntNoProof(maxInSubtree(sibling), key)
    case _ => result
  }

  def nonMemberShipProof(k: BigInt, key: BigInt, direction: String, sibling: Node): MembershipProof = {
    List(k > key, direction) match {
      case List(true, "L") => IntIntNoProof(key,minInSubtree(sibling))
      case List(true, "R") => IntFlagNoProof(key, MINRS)
      case List(false, "L") => FlagIntNoProof(MAXLS, key)
      case List(false, "R") => IntIntNoProof(maxInSubtree(sibling), key)
    }
  }

  def minInSubtree(root: Node): BigInt = root match {
    case Node(_, _, _, null, null) => root.key
    case _ => minInSubtree(root.left)
  }

  def maxInSubtree(root: Node): BigInt = root.key

  def reverse(direction: String): String = direction match {
    case "R" => "L"
    case "L" => "R"
    case s => s
  }

  def delete(root: Node, k: BigInt): Node = {
    var left = root.left
    var right = root.right
    if (checkForLeaf(left, right, k))
      if (left.key == k) right else left
    else {
      val (l_dist, r_dist) = (TreeUtils.distance(k, left.key), TreeUtils.distance(k, right.key))
      val cmp = l_dist - r_dist
      cmp match {
        case c if c == 0 => throw new NoSuchKeyException
        case c if c < 0 =>
          left match {
            case Node(_, _, _, null, null) => throw new NoSuchKeyException
            case _ =>
              left = delete(left, k)
              TreeUtils.makeNode(left, right)
          }
        case c if c > 0 =>
          right match {
            case Node(_, _, _, null, null) => throw new NoSuchKeyException
            case _ =>
              right = delete(right, k)
              TreeUtils.makeNode(left, right)
          }
      }
    }
  }

  def checkForLeaf(left: Node, right: Node, k: BigInt): Boolean = (left.left == null && left.right == null && left.key == k) ||
    (right.left == null && right.right == null && right.key == k)

}