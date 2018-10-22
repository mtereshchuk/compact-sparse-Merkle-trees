import utils.{Node, TreeUtils}

class Tree {
  var root: Node = _

  def insert(k: BigInt, v:String):Unit = if (this.root == null) root = TreeUtils.makeNode(k, v) else root = CSMT.insert(root, k, v)

  def getProof(k:BigInt):Any = if (this.root == null) null else CSMT.getProof(root, k)

  def delete(k:BigInt): Unit = if (this.root != null) CSMT.delete(root, k)
}
