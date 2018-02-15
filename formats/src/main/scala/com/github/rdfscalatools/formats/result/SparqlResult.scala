package com.github.rdfscalatools.formats.result

import akka.http.scaladsl.model
import com.github.rdfscalatools.common.CommonExceptions.DeserializationException
import org.apache.jena.graph.{Node, Node_Blank, Node_Literal, Node_URI}

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
sealed trait SparqlResult

object SparqlResult {

  type ResultTable = IndexedSeq[Map[String, SparqlResult]]

  def apply(node: Node): SparqlResult = node match {
    case x: Node_Literal => x.getLiteralValue match {
      case x: java.lang.Integer => IntLiteral(x.intValue())
      case x: java.lang.Double => DoubleLiteral(x.doubleValue())
      case x: java.lang.Short => IntLiteral(x.shortValue())
      case x: java.lang.Float => DoubleLiteral(x.floatValue())
      case x: java.lang.Long => DoubleLiteral(x.longValue())
      case x: java.lang.Byte => IntLiteral(x.byteValue())
      case x: java.lang.Boolean => BooleanLiteral(x.booleanValue())
      case x: java.math.BigInteger => DoubleLiteral(x.longValueExact())
      case x: java.math.BigDecimal => DoubleLiteral(x.doubleValue())
      case _ => StringLiteral(x.getLiteralLexicalForm)
    }
    case x: Node_URI => Uri(x.getURI)
    case x: Node_Blank => Uri(x.getURI)
    case _ => throw new IllegalArgumentException
  }

  case class Uri(uri: String) extends SparqlResult

  sealed trait Literal[T] extends SparqlResult {
    val value: T
  }

  case class StringLiteral(value: String) extends Literal[String]

  case class IntLiteral(value: Int) extends Literal[Int]

  case class DoubleLiteral(value: Double) extends Literal[Double]

  case class BooleanLiteral(value: Boolean) extends Literal[Boolean]

  implicit val valueToJavaUri: SparqlResult => java.net.URI = {
    case SparqlResult.Uri(value) => java.net.URI.create(value)
    case x => throw DeserializationException(s"Value '$x' is not an URI.")
  }

  implicit val valueToAkkaUri: SparqlResult => model.Uri = {
    case SparqlResult.Uri(value) => model.Uri(value)
    case x => throw DeserializationException(s"Value '$x' is not an URI.")
  }

  implicit val valueToString: SparqlResult => String = {
    case x: SparqlResult.Literal[_] => x.value.toString
    case SparqlResult.Uri(x) => x
    case x => throw DeserializationException(s"Value '$x' is not a string.")
  }

  implicit val valueToBoolean: SparqlResult => Boolean = {
    case SparqlResult.BooleanLiteral(value) => value
    case x => throw DeserializationException(s"Value '$x' is not boolean.")
  }

  implicit val valueToDouble: SparqlResult => Double = {
    case SparqlResult.DoubleLiteral(value) => value
    case SparqlResult.IntLiteral(value) => value
    case x => throw DeserializationException(s"Value '$x' is not a number.")
  }

  implicit val valueToInt: SparqlResult => Int = {
    case SparqlResult.IntLiteral(value) => value
    case x => throw DeserializationException(s"Value '$x' is not integer.")
  }

  implicit def optValueToAny[T](implicit f: SparqlResult => T): Option[SparqlResult] => T = {
    case Some(value) => f(value)
    case None => throw DeserializationException(s"Value does not exist.")
  }

  implicit def optValueToOptAny[T](implicit f: SparqlResult => T): Option[SparqlResult] => Option[T] = _.map(f)

}