package org.datainnutshell

import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

object SimulatorSchemas {

  class Products(tag: Tag) extends Table[(Long, String, Int)](tag, "products") {
    def id: Rep[Long]      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name: Rep[String]  = column[String]("name")
    def quantity: Rep[Int] = column[Int]("quantity")

    def * : ProvenShape[(Long, String, Int)] = (id, name, quantity)
  }

  class ProductTransactions(tag: Tag)
      extends Table[(Long, Long, Int, java.sql.Timestamp, String)](tag, "product_transactions") {
    def id: Rep[Long]        = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def productId: Rep[Long] = column[Long]("product_id")
    def quantity: Rep[Int]   = column[Int]("quantity")
    def timestamp: Rep[java.sql.Timestamp] = column[java.sql.Timestamp](
      "timestamp",
      O.Default(new java.sql.Timestamp(System.currentTimeMillis()))
    )
    def transactionType: Rep[String] = column[String]("transaction_type")

    def * : ProvenShape[(Long, Long, Int, java.sql.Timestamp, String)] =
      (id, productId, quantity, timestamp, transactionType)
  }

  val products            = TableQuery[Products]
  val productTransactions = TableQuery[ProductTransactions]
  def seedProducts = {
    val initialProducts = Seq(
      (1L, "Product A", 10),
      (2L, "Product B", 10)
      // Add more initial products here
    )

    products ++= initialProducts

  }

}
