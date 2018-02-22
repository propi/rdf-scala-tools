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
  protected val dataset: Dataset

  def close(): Unit = dataset.close()

  def use[T](f: RepositorySession => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    Future.fromTry(Try(RepositorySession[T](dataset)(f))).flatten
  }
}

object Repository {

  case class InMemory private(id: String) extends Repository {
    protected lazy val dataset: Dataset = TDB2Factory.createDataset()
  }

  object InMemory {
    def apply(): InMemory = new InMemory(UUID.randomUUID().toString)
  }

  case class Persistent(directory: String) extends Repository {
    protected lazy val dataset: Dataset = TDB2Factory.connectDataset(directory)
  }

}