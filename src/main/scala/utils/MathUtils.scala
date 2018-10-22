package utils

object MathUtils {

  def log2BigInt(value: BigInt): Int = {
    var i = 0
    val bigOne = BigInt(1)
    while ((bigOne << i) <= value)
      i+=1
    i
  }
}