package org.adrianobrito.algorithms.store.pub

import org.adrianobrito.model.StoreRequest
import org.adrianobrito.error.StoreRequestError
import org.adrianobrito.model.Contact
import org.adrianobrito.model.NodeId

trait StoreAlgorithm[F[_]] {

  def store(key: NodeId, value: Array[Byte]): F[Unit]

}
