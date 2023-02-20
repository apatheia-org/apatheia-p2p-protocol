package org.adrianobrito.algorithm.findnode.pub

import org.adrianobrito.model.RoutingTable
import org.adrianobrito.model.NodeId
import org.adrianobrito.model.Contact
import cats.effect.kernel.Async
import cats.implicits._
import cats.Applicative
import scala.annotation.tailrec
import org.adrianobrito.algorithms.findnode.pub.FindNodeAlgorithm

import org.adrianobrito.algorithm.findnode.FindNodeClient

case class DefaultFindNodeAlgorithm[F[_]: Async: Applicative](
  findNodeClient: FindNodeClient[F]
) extends FindNodeAlgorithm[F] {

  override def findNode(
    routingTable: RoutingTable,
    targetId: NodeId,
    maxIterations: Int
  ): F[Set[Contact]] = {
    val closestContacts = routingTable.findClosestContacts(targetId)
    retryFindNodeRequest(
      iteration = maxIterations,
      routingTable = routingTable,
      closestContacts = closestContacts,
      targetId = targetId
    )
  }

  private[findnode] def retryFindNodeRequest(
    iteration: Int,
    routingTable: RoutingTable,
    closestContacts: List[Contact],
    targetId: NodeId
  ): F[Set[Contact]] =
    if (iteration == 0) {
      Async[F].pure(Set.empty)
    } else {
      (for {
        contacts        <- filterFromTargetNode(closestContacts, targetId)
        requestContacts <- sendFindNodeRequests(contacts)
      } yield (requestContacts.toList
        .sortBy(
          _.nodeId.distance(targetId)
        )
        .take(routingTable.k))).flatMap { foundContacts =>
        if (foundContacts.isEmpty) {
          retryFindNodeRequest(
            iteration = iteration - 1,
            routingTable = routingTable,
            closestContacts = closestContacts,
            targetId = targetId
          )
        } else {
          Async[F].pure(foundContacts.toSet)
        }
      }
    }

  private def filterFromTargetNode(
    closestContacts: List[Contact],
    targetId: NodeId
  ): F[Set[Contact]] =
    Async[F]
      .pure(
        closestContacts
          .filter(_.nodeId.value != targetId.value)
          .toSet[Contact]
      )

  private def sendFindNodeRequests(
    nodeContacts: Set[Contact]
  ): F[Set[Contact]] =
    nodeContacts.toList
      .map(contact => findNodeClient.requestContacts(contact))
      .flatTraverse(a => a)
      .flatMap(a => Async[F].pure((a.toSet[Contact])))

}