package model.utils

object TreeUtils {

  def makeNode(key: BigInt, value: String) = Node(key, value, HashingUtils.countLeafHash(value), null, null)

  def makeNode(key: BigInt, value: String, hash: String, left: Node, right: Node): Node = Node(key, value, hash, left, right)

  def makeNode(left: Node, right: Node): Node = makeNode(BigInt(left.key.max(right.key).toString()), "", HashingUtils.countHash(left.hash, right.hash), left, right)

  def distance(x: BigInt, y: BigInt): Int = if ((x ^ y) == 0) -1 else (MathUtils.logBigInt(x ^ y) / MathUtils.LOG2).toInt + 1

}
