package com.github.rdfscalatools.sparqlquerytdb

import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import com.github.rdfscalatools.sparqlquery.query.{OneQuery, QueryOperation}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

/**
  * Created by Vaclav Zeman on 21. 8. 2017.
  */
class RepositoryOneQuery[O] private[sparqlquerytdb](session: RepositorySession)(implicit datasetOps: DatasetOps[O], ec: ExecutionContext) extends OneQuery[SparqlTemplate.Sparql, O] {

  private def update(input: SparqlTemplate.Sparql, writeOps: DatasetOps.DatasetWriteOps[O]) = session.execUpdate(input.toString()).transform {
    case Success(_) => writeOps.convert(None)
    case Failure(th) => writeOps.convert(Some(th))
  }

  private def select(input: SparqlTemplate.Sparql, readOps: DatasetOps.DatasetReadOps[_, O]) = {
    val `Select` = implicitly[ClassTag[DatasetOps.DatasetReadOps.Select[O]]]
    val `Contruct` = implicitly[ClassTag[DatasetOps.DatasetReadOps.Contruct[O]]]
    val `Ask` = implicitly[ClassTag[DatasetOps.DatasetReadOps.Ask[O]]]
    val `Describe` = implicitly[ClassTag[DatasetOps.DatasetReadOps.Describe[O]]]
    readOps match {
      case `Select`(x) => session.execSelect(input.toString()).transform(x.convert)
      case `Contruct`(x) => session.execConstruct(input.toString()).transform(x.convert)
      case `Ask`(x) => session.execAsk(input.toString()).transform(x.convert)
      case `Describe`(x) => session.execDescribe(input.toString()).transform(x.convert)
    }
  }

  def execute(operation: QueryOperation, input: SparqlTemplate.Sparql): Future[O] = {
    val `DatasetReadOps` = implicitly[ClassTag[DatasetOps.DatasetReadOps[_, O]]]
    val `DatasetWriteOps` = implicitly[ClassTag[DatasetOps.DatasetWriteOps[O]]]
    (operation, datasetOps) match {
      case (QueryOperation.Read, `DatasetReadOps`(readOps)) => select(input, readOps)
      case (QueryOperation.Insert | QueryOperation.Update | QueryOperation.Delete, `DatasetWriteOps`(writeOps)) => update(input, writeOps)
      case _ => throw new IllegalArgumentException
    }
  }

}