package com.github.rdfscalatools.sparqlquerytdb

import com.github.rdfscalatools.sparqlquery.query.Transaction
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext

/**
  * Created by Vaclav Zeman on 9. 2. 2018.
  */
class RepositoryTransaction private[sparqlquerytdb](val session: RepositorySession)(implicit ec: ExecutionContext) extends Transaction {

  private val logger = Logger[RepositoryTransaction]

  def commit(): Unit = {
    val result = session.commit()
    result.failed.foreach(th => logger.error("Error within 'commit' query: " + th.getMessage, th))
    result.onComplete(_ => session.close())
  }

  def rollback(): Unit = {
    val result = session.rollback()
    result.failed.foreach(th => logger.error("Error within 'rollback' query: " + th.getMessage, th))
    result.onComplete(_ => session.close())
  }

}
