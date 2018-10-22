package utils

object MathUtils {
  protected val LOG2: Double = Math.log(2.0)
  protected val LOG10: Double = Math.log(10.0)
  protected val MAX_DIGITS_EXP = 677
  protected val MAX_DIGITS_10 = 294
  protected val MAX_DIGITS_2 = 977

  def logBigInt(value: BigInt): Double = {
    var curVal = value
    if (curVal.signum < 1)
      if (curVal.signum < 0) Double.NaN else Double.NegativeInfinity
    val blex = curVal.bitLength - MAX_DIGITS_2
    if (blex > 0)
      curVal >>= blex
    val res = math.log(value.doubleValue())
    if (blex > 0)
      res + blex * LOG2
    else
      res
  }
}
