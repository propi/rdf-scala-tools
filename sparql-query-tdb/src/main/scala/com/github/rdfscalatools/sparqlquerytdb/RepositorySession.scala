package com.github.rdfscalatools.sparqlquerytdb

import com.github.rdfscalatools.formats.result.ResultSetOps._
import com.github.rdfscalatools.formats.result.SparqlResult.ResultTable
import com.typesafe.scalalogging.Logger
import org.apache.jena.query.{Dataset, QueryExecutionFactory, ReadWrite}
import org.apache.jena.rdf.model.Model
import org.apache.jena.update.UpdateAction

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

/**
  * Created by Vaclav Zeman on 9. 2. 2018.
  */
class RepositorySession private(dataset: Dataset) extends Runnable {

  private val logger = Logger[RepositorySession]

  private val children = ListBuffer.empty[RepositorySession]

  private val messages = collection.mutable.Queue.empty[Message]

  private sealed trait Message

  private object Message {

    object Close extends Message

    case class Commit(result: Promise[Unit]) extends Message

    case class Rollback(result: Promise[Unit]) extends Message

    case class StartTransaction(readOnly: Boolean, result: Promise[Unit]) extends Message

    case class Select(sparql: String, result: Promise[ResultTable]) extends Message

    case class Construct(sparql: String, result: Promise[Model]) extends Message

    case class Ask(sparql: String, result: Promise[Boolean]) extends Message

    case class Describe(sparql: String, result: Promise[Model]) extends Message

    case class Update(sparql: String, result: Promise[Unit]) extends Message

  }

  def newChildSession: RepositorySession = this.synchronized {
    val session = new RepositorySession(dataset)
    new Thread(session).start()
    children += session
    session
  }

  def execUpdate(sparql: String): Future[Unit] = this.synchronized {
    val result = Promise[Unit]()
    messages enqueue Message.Update(sparql, result)
    this.notify()
    result.future
  }

  def execSelect(sparql: String): Future[ResultTable] = this.synchronized {
    val result = Promise[ResultTable]()
    messages enqueue Message.Select(sparql, result)
    this.notify()
    result.future
  }

  def execConstruct(sparql: String): Future[Model] = this.synchronized {
    val result = Promise[Model]()
    messages enqueue Message.Construct(sparql, result)
    this.notify()
    result.future
  }

  def execDescribe(sparql: String): Future[Model] = this.synchronized {
    val result = Promise[Model]()
    messages enqueue Message.Describe(sparql, result)
    this.notify()
    result.future
  }

  def execAsk(sparql: String): Future[Boolean] = this.synchronized {
    val result = Promise[Boolean]()
    messages enqueue Message.Ask(sparql, result)
    this.notify()
    result.future
  }

  def startTransaction(readOnly: Boolean): Future[Unit] = this.synchronized {
    val result = Promise[Unit]()
    messages enqueue Message.StartTransaction(readOnly, result)
    this.notify()
    result.future
  }

  def commit(): Future[Unit] = this.synchronized {
    val result = Promise[Unit]()
    messages enqueue Message.Commit(result)
    this.notify()
    result.future
  }

  def rollback(): Future[Unit] = this.synchronized {
    val result = Promise[Unit]()
    messages enqueue Message.Rollback(result)
    this.notify()
    result.future
  }

  def close(): Unit = this.synchronized {
    children.foreach(_.close())
    messages enqueue Message.Close
    this.notify()
  }

  def run(): Unit = this.synchronized {
    var isOpen = true
    while (isOpen) {
      if (messages.isEmpty) this.wait()
      while (isOpen && messages.nonEmpty) {
        val message = messages.dequeue()
        message match {
          case Message.Update(sparql, result) =>
            logger.trace("Update query.")
            result.complete(Try(UpdateAction.parseExecute(sparql, dataset)))
          case Message.Select(sparql, result) =>
            logger.trace("Select query.")
            result.complete(Try {
              val qexec = QueryExecutionFactory.create(sparql, dataset)
              qexec.execSelect().toResultTable
            })
          case Message.Construct(sparql, result) =>
            logger.trace("Construct query.")
            result.complete(Try {
              val qexec = QueryExecutionFactory.create(sparql, dataset)
              qexec.execConstruct()
            })
          case Message.Ask(sparql, result) =>
            logger.trace("Ask query.")
            result.complete(Try {
              val qexec = QueryExecutionFactory.create(sparql, dataset)
              qexec.execAsk()
            })
          case Message.Describe(sparql, result) =>
            logger.trace("Describe query.")
            result.complete(Try {
              val qexec = QueryExecutionFactory.create(sparql, dataset)
              qexec.execDescribe()
            })
          case Message.StartTransaction(readOnly, result) =>
            if (readOnly) logger.trace("Start read-only transaction.") else logger.trace("Start writable transaction.")
            result.complete(Try(dataset.begin(if (readOnly) ReadWrite.READ else ReadWrite.WRITE)))
          case Message.Commit(result) =>
            logger.trace("Commit query.")
            result.complete(Try {
              dataset.commit()
              dataset.end()
            })
          case Message.Rollback(result) =>
            logger.trace("Rollback query.")
            result.complete(Try {
              dataset.abort()
              dataset.end()
            })
          case Message.Close =>
            isOpen = false
        }
      }
    }
    logger.trace("Dataset session closed")
  }

}

object RepositorySession {

  def apply[T](dataset: Dataset)(f: RepositorySession => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val session = new RepositorySession(dataset)
    session.logger.trace("New dataset session opened: " + session)
    val thread = new Thread(session)
    thread.start()
    val result = Future.fromTry(Try(f(session))).flatten
    result.onComplete { _ =>
      session.logger.trace("Try to close dataset session: " + thread)
      session.close()
    }
    result
  }

}