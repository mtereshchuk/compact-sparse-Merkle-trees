package model.utils

import java.security.MessageDigest
import java.util.Base64

object HashingUtils {
  val zero: Array[Byte] = Array(0.toByte)
  val one: Array[Byte] = Array(1.toByte)
  val two: Array[Byte] = Array(0.toByte)

  val encoder: Base64.Encoder = Base64.getEncoder
  val decoder: Base64.Decoder = Base64.getDecoder
  val digest: MessageDigest = MessageDigest.getInstance("SHA-256")

  def countHash(left: String, right: String): String = {
    if (left == "null" && !(right == "null")) {
      val bytes = digest.digest(concatenateByteArrays(one, two, decoder.decode(right)))
      return encoder.encodeToString(bytes)
    }
    else if (!(left == "null") && right == "null") {
      val bytes = digest.digest(concatenateByteArrays(one, decoder.decode(left), two))
      return encoder.encodeToString(bytes)
    }
    else if (!(left == "null") && !(right == "null")) {
      val bytes = digest.digest(concatenateByteArrays(one, decoder.decode(left), two, decoder.decode(right)))
      return encoder.encodeToString(bytes)
    }
    "null"
  }

  def countLeafHash(value:String):String = encoder.encodeToString(digest.digest(concatenateByteArrays(zero, value.getBytes())))

  private def concatenateByteArrays(bytes: Array[Byte]*) = {
    var length = 0
    for (seg <- bytes) {
      length += seg.length
    }
    val concatenated = new Array[Byte](length)
    var index = 0
    for (seq <- bytes) {
      for (b <- seq) {
        concatenated(index) = b
        index += 1
      }
    }
    concatenated
  }

}
