package com.github.rdfscalatools.sparqlquerytdb

import com.github.rdfscalatools.sparqlquery.query.TransactionBuilder

import scala.concurrent.ExecutionContext

/**
  * Created by Vaclav Zeman on 9. 2. 2018.
  */
object RepositoryTransactionBuilder {

  def apply[T](readOnly: Boolean)(implicit session: RepositorySession, ec: ExecutionContext): TransactionBuilder[RepositoryTransaction] = () => {
    val newSession = session.newChildSession
    newSession.startTransaction(readOnly)
    new RepositoryTransaction(newSession)
  }

  object Read {
    implicit def readOnlyTransactionBuilder[T](implicit session: RepositorySession, ec: ExecutionContext): TransactionBuilder[RepositoryTransaction] = apply[T](true)
  }

  object Write {
    implicit def writeTransactionBuilder[T](implicit session: RepositorySession, ec: ExecutionContext): TransactionBuilder[RepositoryTransaction] = apply[T](false)
  }

}
