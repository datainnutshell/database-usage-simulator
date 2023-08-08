package org.datainnutshell

import akka.actor.{
  Actor,
  ActorLogging,
  ActorSystem,
  Cancellable,
  OneForOneStrategy,
  Props,
  SupervisorStrategy
}
import akka.event.Logging
import org.datainnutshell.SimulatorSchemas
import org.datainnutshell.ProductTransactionSimulator.SimulateTransaction
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object ProductTransactionSimulator {

  case object SimulateTransaction

  def props(db: Database): Props = Props(new ProductTransactionSimulator(db))
}

case class ProductTransactionSimulator(db: Database) extends Actor with ActorLogging {
  private val random = new scala.util.Random()

  // Load existing product IDs from the database
  private var productIds: Seq[Long] = Seq.empty

  // Generate random product quantity changes
  private def generateQuantityChange(): Int = random.nextInt(20) - 10

  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() { case ex: Exception =>
    log.error(ex, "Error during actor initialization")
    SupervisorStrategy.Stop // Stop the actor if initialization fails
  }

  // Get the current product quantity from the database
  private def getCurrentProductQuantity(productId: Long): Future[Int] =
    db.run(SimulatorSchemas.products.filter(_.id === productId).map(_.quantity).result.headOption)
      .map(_.getOrElse(0)) // Default initial product quantity

  // Update the product quantity in the database and record the transaction
  private def updateProductQuantity(productId: Long): Unit = {
    val quantityChange = generateQuantityChange()

    val updatedFuture = for {
      currentQuantity <- getCurrentProductQuantity(productId)
      newQuantity = (currentQuantity + quantityChange).max(0)
      _ <- db.run(
        SimulatorSchemas.products.filter(_.id === productId).map(_.quantity).update(newQuantity)
      )
      _ <- db.run(
        SimulatorSchemas.productTransactions += (0L, productId, quantityChange, new java.sql.Timestamp(
          System.currentTimeMillis()
        ), "simulated")
      )
    } yield newQuantity

    updatedFuture.onComplete {
      case Success(newQuantity) =>
        log.info(s"Product $productId quantity updated to: $newQuantity")
      case Failure(ex) =>
        log.error(s"Failed to update product $productId quantity: ${ex.getMessage}")
    }
  }

  // Schedule periodic product transactions
  private val updateSchedule: Cancellable =
    context.system.scheduler.scheduleWithFixedDelay(0.seconds, 5.seconds) { () =>
      productIds.foreach(updateProductQuantity)
    }

  override def preStart(): Unit = {
    // Load existing product IDs from the database on startup
    productIds = Await.result(db.run(SimulatorSchemas.products.map(_.id).result), 5.seconds)
  }

  override def postStop(): Unit = {
    updateSchedule.cancel()
  }

  override def receive: Receive = { case SimulateTransaction =>
    productIds.foreach(updateProductQuantity)
  }
}
