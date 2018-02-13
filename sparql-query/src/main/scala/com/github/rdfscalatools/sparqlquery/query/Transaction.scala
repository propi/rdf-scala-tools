package com.github.rdfscalatools.sparqlquery.query

import scala.concurrent.{ExecutionContext, Future, Promise}
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

  def apply[O, T <: Transaction](f: TransactionOps[T] => Future[O])(implicit transactionBuilder: TransactionBuilder[T], ec: ExecutionContext): Future[O] = {
    val tx = transactionBuilder()
    val result = Promise[O]
    Future.fromTry(Try(f(new TransactionOps(tx)))).flatten.onComplete {
      case Success(x) => result.complete(Try(tx.commit()).map(_ => x))
      case Failure(th) => Try(tx.rollback()); result.failure(th)
    }
    result.future
  }

}