package com.github.rdfscalatools.sparqlquerytdb

import java.util.UUID

import org.apache.jena.query.Dataset
import org.apache.jena.tdb2.TDB2Factory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Created by Vaclav Zeman on 21. 8. 2017.
  */
sealed trait Repository {
  protected def createDataset: Dataset

  def use[T](f: RepositorySession => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val dataset = createDataset
    val result = Future.fromTry(Try(RepositorySession[T](dataset)(f))).flatten
    result.onComplete(_ => dataset.close())
    result
  }
}

object Repository {

  case class InMemory private(id: String) extends Repository {
    protected def createDataset: Dataset = TDB2Factory.createDataset()
  }

  object InMemory {
    def apply(): InMemory = new InMemory(UUID.randomUUID().toString)
  }

  case class Persistent(directory: String) extends Repository {
    protected def createDataset: Dataset = TDB2Factory.connectDataset(directory)
  }

}