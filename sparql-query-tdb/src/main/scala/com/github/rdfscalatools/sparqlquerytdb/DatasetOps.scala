package com.github.rdfscalatools.sparqlquerytdb

import com.github.rdfscalatools.formats.result.SparqlResult.ResultTable
import org.apache.jena.rdf.model.Model

import scala.util.{Failure, Success, Try}

/**
  * Created by Vaclav Zeman on 9. 2. 2018.
  */
sealed trait DatasetOps[O]

object DatasetOps {

  sealed trait DatasetReadOps[I, O] extends DatasetOps[O] {
    def convert(source: Try[I]): Try[O]
  }

  object DatasetReadOps {

    trait Contruct[O] extends DatasetReadOps[Model, O]

    object Contruct {

      implicit object ContructModel extends DatasetReadOps.Contruct[Model] {
        def convert(source: Try[Model]): Try[Model] = source
      }

    }

    trait Describe[O] extends DatasetReadOps[Model, O]

    object Describe {

      implicit object DescribeModel extends DatasetReadOps.Describe[Model] {
        def convert(source: Try[Model]): Try[Model] = source
      }

    }

    trait Ask[O] extends DatasetReadOps[Boolean, O]

    object Ask {

      implicit object AskBoolean extends DatasetReadOps.Ask[Boolean] {
        def convert(source: Try[Boolean]): Try[Boolean] = source
      }

    }

    trait Select[O] extends DatasetReadOps[ResultTable, O]

    object Select {

      implicit object SelectResultTable extends DatasetReadOps.Select[ResultTable] {
        def convert(source: Try[ResultTable]): Try[ResultTable] = source
      }

    }

  }

  trait DatasetWriteOps[O] extends DatasetOps[O] {
    def convert(source: Option[Throwable]): Try[O]
  }

  object DatasetWriteOps {

    implicit object DefaultBoolean extends DatasetWriteOps[Boolean] {
      def convert(source: Option[Throwable]): Try[Boolean] = source match {
        case Some(th) => Failure(th)
        case None => Success(true)
      }
    }

  }

}