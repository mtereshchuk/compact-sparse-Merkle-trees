package model.tester

import java.util.*
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.stream.Collectors
import java.util.ArrayList
import kotlin.collections.HashMap

class Merkle<V, H>(private val high: Int, input: Array<V>,
                   leafConverter: Function<V?, H>,
                   private val nodeConverter: BinaryOperator<H?>) {

    private val numberOfNodesInLevel = IntArray(high + 1)
    private val levelNode = IntArray(high + 1)
    private val nodes = HashMap<Int, H?>()

    fun getProof(id: Int): MerkleProof<H> {
        val targetHash = nodes[id]
        val list = ArrayList<H?>()

        setProofList(id, 0, list)
        return MerkleProof(targetHash, list)
    }

    private fun setProofList(id: Int, currLevel: Int, list: ArrayList<H?>) {
        val neighborId = if (id % 2 == 0) id + 1 else id - 1
        list.add(nodes[neighborId])

        if (currLevel < high) {
            setProofList(getParentId(id, currLevel), currLevel + 1, list)
        }
    }

    init {
        for (i in 0 until numberOfNodesInLevel.size) {
            numberOfNodesInLevel[i] = Math.pow(2.0, (high - i).toDouble()).toInt()
        }

        levelNode[0] = numberOfNodesInLevel[0]
        for (i in 1 until levelNode.size) {
            levelNode[i] = levelNode[i - 1] + numberOfNodesInLevel[i]
        }

        for (i in 0 until input.size) {
            nodes[i] = leafConverter.apply(input[i])
        }

        buildTree(0, nodes.keys.stream().sorted().collect(Collectors.toSet()))
    }

    private fun buildTree(currLevel: Int, currIdes: Set<Int>) {
        if (currLevel < high) {
            val nextIdes = TreeSet<Int>()

            for (id in currIdes) {
                val parent = getParentId(id, currLevel)
                if (!nextIdes.contains(parent)) {
                    var hash1: H?
                    var hash2: H?

                    if (id % 2 == 0) {
                        hash1 = nodes[id]
                        hash2 = nodes[id + 1]
                    } else {
                        hash2 = nodes[id]
                        hash1 = nodes[id - 1]
                    }

                    val hashParent = nodeConverter.apply(hash1, hash2)!!
                    nodes[parent] = hashParent
                    nextIdes.add(parent)
                }
            }

            buildTree(currLevel + 1, nextIdes)
        }
    }

    private fun getParentId(id: Int, level: Int): Int {
        val newId = id + numberOfNodesInLevel[level]
        val count = newId - levelNode[level]
        return newId - Math.ceil(count / 2.0).toInt()
    }
}