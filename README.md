# Compact Sparse Merkle trees
This is Java and Scala implementation of [Compact Sparse Merkle trees](https://eprint.iacr.org/2018/955.pdf).
Source [code](https://github.com/ZanjeerPlatform/csmt) of elixir implementation.

##Presentation
Explanation of algorithm in russian [here](https://docs.google.com/presentation/d/1iqQfiGt-56WUUiICj72P9kJhyaTxZBnv9ktJBVaoYv4/edit#slide=id.p).

###Java implementation 
interface [CSMT](https://github.com/teremax/compact-sparse-Merkle-trees/blob/master/src/main/java/model/CSMT.java)

```java
/**
* V insert value type
* H hash type
*/
public interface CSMT<V, H> {
    void insert(BigInteger key, V value);
    void remove(BigInteger key);
    Proof<V, H> getProof(BigInteger key);
}
```

###Scala implementation
[interface](https://github.com/teremax/compact-sparse-Merkle-trees/blob/master/src/main/scala/implementation/Tree.scala)

```scala
class CSMT {
  def insert(k: BigInt, v: String): Unit
  def getProof(k:BigInt): MembershipProof
  def delete(k:BigInt): Unit
```