package com.github.rdfscalatools.sparqlquerytdb

import org.apache.jena.query.ResultSet
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

    trait Describe[O] extends DatasetReadOps[Model, O]

    trait Ask[O] extends DatasetReadOps[Boolean, O]

    trait Select[O] extends DatasetReadOps[ResultSet, O]

  }

  trait DatasetWriteOps[O] extends DatasetOps[O] {
    def convert(source: Option[Throwable]): Try[O]
  }

  object DatasetWriteOps {

    object DefaultBoolean extends DatasetWriteOps[Boolean] {
      def convert(source: Option[Throwable]): Try[Boolean] = source match {
        case Some(th) => Failure(th)
        case None => Success(true)
      }
    }

  }

}