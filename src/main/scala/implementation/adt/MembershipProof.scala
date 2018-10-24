package implementation.adt

import implementation.Flags.Flags

case class ProofResult(key:BigInt, value:String, hash:String, proof:List[(String,String)]) extends MembershipProof
case class NoProofList(list: List[MembershipProof]) extends MembershipProof
case class ProofPairList(list: List[(String,String)]) extends MembershipProof
case class IntIntNoProof(first: BigInt, second:BigInt) extends MembershipProof
case class FlagIntNoProof(first: Flags, second: BigInt) extends MembershipProof
case class IntFlagNoProof(first: BigInt, second: Flags) extends MembershipProof

sealed trait MembershipProof{
}