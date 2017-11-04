package com.github.rdfscalatools.sparqlquery.query

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
trait Transaction {

  def commit(): Unit

  def rollback(): Unit

}

object Transaction {

  object Empty extends Transaction {
    def commit(): Unit = {}

    def rollback(): Unit = {}
  }

  def apply[O, T <: Transaction](f: TransactionOps[T] => Future[Option[O]])(implicit transactionBuilder: TransactionBuilder[T], ec: ExecutionContext): Future[Option[O]] = {
    val tx = transactionBuilder()
    val result = Future.fromTry(Try(f(new TransactionOps(tx)))).flatten
    result.onComplete {
      case Failure(_) => tx.rollback()
      case Success(x) => if (x.isDefined) tx.commit() else tx.rollback()
    }
    result
  }

}