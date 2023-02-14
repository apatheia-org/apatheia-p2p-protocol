package org.adrianobrito.algorithm.store.pub

import org.adrianobrito.model.{Contact, NodeId, RoutingTable, StoreRequest, StoreSuccessThreshold}
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.concurrent.ScalaFutures
import cats.effect.IO
import cats.implicits._
import org.mockito.Mockito._

import cats.effect.unsafe.implicits.global
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers
import org.adrianobrito.error.StoreRequestError
import org.adrianobrito.algorithm.store.StoreClient

class DefaultStoreAlgorithmSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar {

  val nodeId = NodeId(123)
  val contacts =
    List(
      Contact(NodeId(2), "1.0.0.99", 1111),
      Contact(NodeId(1), "1.0.0.99", 1111),
      Contact(NodeId(0), "1.0.0.99", 1111)
    )
  val routingTable = RoutingTable(nodeId, contacts)
  val threshold    = StoreSuccessThreshold(10)
  val storeClient = new StoreClient[IO] {
    override def sendStoreRequest(storeRequest: StoreRequest): IO[StoreClient.StoreResponse] = IO(
      Right(Contact(nodeId = storeRequest.key, ip = "1.0.0.99", port = 1111))
    )
  }

  "Store Algorithm" - {
    "DefaultStoreAlgorithm" - {
      "should store data in the network" in {
        // Given
        val storeAlgorithm = new DefaultStoreAlgorithm[IO](routingTable, storeClient, threshold)

        // When
        val result = storeAlgorithm.store(nodeId, Array[Byte](1, 3, 4))().unsafeRunSync()

        // Then
        result shouldBe contacts.toSet
      }

      "should stop the store operation if the maximum number of iterations is reached" in {
        val targetContact   = Contact(NodeId(0), "1.0.0.1", 9999)
        val mockStoreClient = mock[StoreClient[IO]]

        when(mockStoreClient.sendStoreRequest(ArgumentMatchers.any[StoreRequest]))
          .thenReturn(IO.pure(Left(StoreRequestError(targetContact))))

        val storeAlgorithm = new DefaultStoreAlgorithm[IO](routingTable, mockStoreClient, maxIterations = 1)
        val result         = storeAlgorithm.store(targetContact.nodeId, Array[Byte](1))().unsafeRunSync()

        // if algorithm wont inf-loop here it means it's f*cking fine
        result shouldBe Set.empty[Contact]
      }

      "should return a list of contacts if at least one successful store response was received" in {
        val mockStoreClient = mock[StoreClient[IO]]
        when(mockStoreClient.sendStoreRequest(ArgumentMatchers.any[StoreRequest]))
          .thenReturn(IO.pure(Right(Contact(NodeId(22), "localhost", 8081))))

        val storeAlgorithm = new DefaultStoreAlgorithm[IO](routingTable, mockStoreClient)
        val result         = storeAlgorithm.store(NodeId(99), Array[Byte]())().unsafeRunSync()

        result shouldBe Set(Contact(NodeId(22), "localhost", 8081))
      }

    }
  }
}
