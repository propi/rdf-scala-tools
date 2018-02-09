package com.github.rdfscalatools.sparqlquerytdb

import org.apache.jena.query.{Dataset, QueryExecutionFactory, ReadWrite, ResultSet}
import org.apache.jena.rdf.model.Model

import scala.concurrent.{Future, Promise}
import scala.util.Try

/**
  * Created by Vaclav Zeman on 9. 2. 2018.
  */
class DatasetDispatcher(dataset: Dataset) extends Runnable {

  private var message: Message = Message.Empty

  private sealed trait Message

  private object Message {

    object Empty extends Message

    object Close extends Message

    case class Commit(result: Promise[Unit]) extends Message

    case class Rollback(result: Promise[Unit]) extends Message

    case class StartTransaction(readOnly: Boolean, result: Promise[Unit]) extends Message

    case class Select(sparql: String, result: Promise[ResultSet]) extends Message

    case class Construct(sparql: String, result: Promise[Model]) extends Message

    case class Ask(sparql: String, result: Promise[Boolean]) extends Message

    case class Describe(sparql: String, result: Promise[Model]) extends Message

  }

  def execSelect(sparql: String): Future[ResultSet] = this.synchronized {
    val result = Promise[ResultSet]
    message = Message.Select(sparql, result)
    this.notify()
    result.future
  }

  def execConstruct(sparql: String): Future[Model] = this.synchronized {
    val result = Promise[Model]
    message = Message.Construct(sparql, result)
    this.notify()
    result.future
  }

  def execDescribe(sparql: String): Future[Model] = this.synchronized {
    val result = Promise[Model]
    message = Message.Describe(sparql, result)
    this.notify()
    result.future
  }

  def execAsk(sparql: String): Future[Boolean] = this.synchronized {
    val result = Promise[Boolean]
    message = Message.Ask(sparql, result)
    this.notify()
    result.future
  }

  def startTransaction(readOnly: Boolean): Future[Unit] = this.synchronized {
    val result = Promise[Unit]
    message = Message.StartTransaction(readOnly, result)
    this.notify()
    result.future
  }

  def commit(): Future[Unit] = this.synchronized {
    val result = Promise[Unit]
    message = Message.Commit(result)
    this.notify()
    result.future
  }

  def rollback(): Future[Unit] = this.synchronized {
    val result = Promise[Unit]
    message = Message.Rollback(result)
    this.notify()
    result.future
  }

  def close(): Unit = this.synchronized {
    message = Message.Close
    this.notify()
  }

  def run(): Unit = {
    this.synchronized {
      var isOpen = true
      try {
        while (isOpen) {
          this.wait()
          message match {
            case Message.Select(sparql, result) =>
              result.complete(Try {
                val qexec = QueryExecutionFactory.create(sparql, dataset)
                qexec.execSelect()
              })
            case Message.Construct(sparql, result) =>
              result.complete(Try {
                val qexec = QueryExecutionFactory.create(sparql, dataset)
                qexec.execConstruct()
              })
            case Message.Ask(sparql, result) =>
              result.complete(Try {
                val qexec = QueryExecutionFactory.create(sparql, dataset)
                qexec.execAsk()
              })
            case Message.Describe(sparql, result) =>
              result.complete(Try {
                val qexec = QueryExecutionFactory.create(sparql, dataset)
                qexec.execDescribe()
              })
            case Message.StartTransaction(readOnly, result) =>
              result.complete(Try(dataset.begin(if (readOnly) ReadWrite.READ else ReadWrite.WRITE)))
            case Message.Commit(result) =>
              result.complete(Try(dataset.commit()))
            case Message.Rollback(result) =>
              result.complete(Try(dataset.abort()))
            case Message.Close =>
              isOpen = false
            case Message.Empty =>
          }
        }
      } finally {
        dataset.close()
      }
    }
  }

}
