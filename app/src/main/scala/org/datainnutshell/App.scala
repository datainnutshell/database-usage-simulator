package org.datainnutshell

import akka.actor.ActorSystem
import akka.actor.TypedActor.{context, self}
import org.datainnutshell.App.system.log
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContextExecutor}
import scala.concurrent.duration.{DurationInt}
import scala.util.{Failure, Success}

object App extends App {
  implicit val system: ActorSystem = ActorSystem("ProductStockSimulation")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Read database configuration from environment variables
  private val mysqlHost = sys.env.getOrElse("MYSQL_HOST", "localhost")
  private val mysqlPort = sys.env.getOrElse("MYSQL_PORT", "3306").toInt
  private val mysqlDb = sys.env.getOrElse("MYSQL_DB", "product_stock_db")
  private val mysqlUser = sys.env.getOrElse("MYSQL_USER", "dbuser")
  private val mysqlPassword = sys.env.getOrElse("MYSQL_PASSWORD", "dbpassword")

  val dbUrl = s"jdbc:mysql://$mysqlHost:$mysqlPort/$mysqlDb"
  val db = Database.forURL(url = dbUrl, user = mysqlUser, password = mysqlPassword)

  // Define the database schema structure
  val setup = DBIO.seq(
    SimulatorSchemas.products.schema.createIfNotExists,
    SimulatorSchemas.productTransactions.schema.createIfNotExists
  )

  //  Perform setup and seed with initial products
  val setupAndStartFuture = db.run(setup andThen SimulatorSchemas.seedProducts)

  // Shutdown the system after a while (for demonstration purposes)
  setupAndStartFuture.onComplete {
    case Success(_) => {
      log.info("Database setup and seeding completed successfully.")
      system.actorOf(ProductTransactionSimulator.props(db), "productTransactionSimulator")

      system.scheduler.scheduleOnce(30.minutes) {
        system.terminate()
        db.close()
      }
    }
    case Failure(ex) => {
      log.error(ex, "Error during database setup and seeding.")
      // Stop the actor if setup fails
      context.stop(self)
    }
  }
}
