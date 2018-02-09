package com.github.rdfscalatools.sparqlquerytdb

import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import com.github.rdfscalatools.sparqlquery.query.{OneQuery, QueryOperation}
import org.apache.jena.query.{Dataset, QueryExecutionFactory}
import org.apache.jena.update.UpdateAction

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Try}

/**
  * Created by Vaclav Zeman on 21. 8. 2017.
  */
class RepositoryOneQuery[O](implicit dataset: Dataset, datasetOps: DatasetOps[O], ec: ExecutionContext) extends OneQuery[SparqlTemplate.Sparql, O] {

  private def update(input: SparqlTemplate.Sparql, writeOps: DatasetOps.DatasetWriteOps[O]) = {
    val result = Try(UpdateAction.parseExecute(input.toString(), dataset)) match {
      case Failure(th) => Some(th)
      case _ => None
    }
    writeOps.convert(result)
  }

  private def select(input: SparqlTemplate.Sparql, readOps: DatasetOps.DatasetReadOps[_, O]) = {
    val `Select` = implicitly[ClassTag[DatasetOps.DatasetReadOps.Select[O]]]
    val `Contruct` = implicitly[ClassTag[DatasetOps.DatasetReadOps.Contruct[O]]]
    val `Ask` = implicitly[ClassTag[DatasetOps.DatasetReadOps.Ask[O]]]
    val `Describe` = implicitly[ClassTag[DatasetOps.DatasetReadOps.Describe[O]]]
    val qexec = QueryExecutionFactory.create(input.toString(), dataset)
    try {
      readOps match {
        case `Select`(x) => x.convert(Try(qexec.execSelect()))
        case `Contruct`(x) => x.convert(Try(qexec.execConstruct()))
        case `Ask`(x) => x.convert(Try(qexec.execAsk()))
        case `Describe`(x) => x.convert(Try(qexec.execDescribe()))
      }
    } finally {
      qexec.close()
    }
  }

  def execute(operation: QueryOperation, input: SparqlTemplate.Sparql): Future[O] = {
    val `DatasetReadOps` = implicitly[ClassTag[DatasetOps.DatasetReadOps[_, O]]]
    val `DatasetWriteOps` = implicitly[ClassTag[DatasetOps.DatasetWriteOps[O]]]
    val result = (operation, datasetOps) match {
      case (QueryOperation.Read, `DatasetReadOps`(readOps)) => Future(select(input, readOps))
      case (QueryOperation.Insert | QueryOperation.Update | QueryOperation.Delete, `DatasetWriteOps`(writeOps)) => Future(update(input, writeOps))
      case _ => throw new IllegalArgumentException
    }
    result.flatMap(Future.fromTry)
  }

}