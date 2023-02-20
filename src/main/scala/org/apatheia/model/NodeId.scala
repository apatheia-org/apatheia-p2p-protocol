package org.apatheia.model

final case class NodeId(val value: BigInt) extends AnyVal {
  def distance(other: NodeId): BigInt = (this.value ^ other.value).abs
}