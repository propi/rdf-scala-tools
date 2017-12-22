package com.github.rdfscalatools.formats.sparql

import com.github.rdfscalatools.formats.RdfMediaTypes
import play.twirl.api.{BufferedContent, Formats}

import scala.collection.immutable
import scala.language.implicitConversions

/**
  * Created by Vaclav Zeman on 13. 8. 2017.
  */
object SparqlTemplate {

  class InvalidSparqlFormat(msg: String) extends Exception(msg)

  object Sparql {

    implicit private def escapeChar(char: Char): Seq[Char] = List('\\', char)

    implicit private def stringToSparql(string: String): Sparql = new Sparql(string)

    private def prefixedUriIsValid(string: String): Boolean = {
      val PN_CHARS_BASE = "[A-Z]|[a-z]|[\\u00C0-\\u00D6]|[\\u00D8-\\u00F6]|[\\u00F8-\\u02FF]|[\\u0370-\\u037D]|[\\u037F-\\u1FFF]|[\\u200C-\\u200D]|[\\u2070-\\u218F]|[\\u2C00-\\u2FEF]|[\\u3001-\\uD7FF]|[\\uF900-\\uFDCF]|[\\uFDF0-\\uFFFD]"
      val PN_CHARS_U = PN_CHARS_BASE + "|_"
      val PN_CHARS = PN_CHARS_U + "|-|[0-9]|[\\u00B7]|[\\u0300-\\u036F]|[\\u203F-\\u2040]"
      val PN_PREFIX = s"($PN_CHARS_BASE)(($PN_CHARS|[.])*($PN_CHARS))?"
      val PLX = "(%([0-9]|[A-F]|[a-f]){2})|(\\\\[-_~.!$&'()*+,;=/?#@%])"
      val PN_LOCAL = s"($PN_CHARS_U|[0-9]|$PLX)(($PN_CHARS|.|:|$PLX)*($PN_CHARS|$PLX))?"
      val (p1, p2) = string.span(_ != ':')
      p1.matches(PN_PREFIX) && p2.matches(s":$PN_LOCAL")
    }

    def curi(string: String): Sparql = uri(s"<$string>")

    def uri(string: String): Sparql = if ((string.matches("<[^<>\"{}|^`\\\\]+>") && string.matches("<[^\\u0000-\\u0020]+>")) || prefixedUriIsValid(string)) {
      string
    } else {
      throw new InvalidSparqlFormat("Invalid uri in sparql query: " + string)
    }

    def literal(string: String): Sparql = "\"" + string.flatMap {
      case '\n' => 'n'
      case '\t' => 't'
      case '\b' => 'b'
      case '\r' => 'r'
      case '\f' => 'f'
      case '"' => '"'
      case '\'' => '\''
      case '\\' => '\\'
      case x => List(x)
    } + "\""

    def literal(boolean: Boolean): Sparql = if (boolean) "true" else "false"

    def literal(number: Int): Sparql = number.toString

    def literal(number: Double): Sparql = if (number.isInfinity) {
      throw new InvalidSparqlFormat("Infinity number is not allowed in sparql query")
    } else if (number.isNaN) {
      throw new InvalidSparqlFormat("NaN is not allowed in sparql query")
    } else {
      number.toString
    }

    def langLiteral(string: String, lang: String): Sparql = literal(string).body + (if (lang.matches("[a-zA-Z]+(-[a-zA-Z0-9]+)*")) "@" + lang else "")

    def typedLiteral(string: String, _uri: String): Sparql = literal(string).body + "^^" + uri(_uri).body

    def in(col: Traversable[Sparql]): Sparql = col.reduceLeftOption((a, b) => a.toString() + ", " + b.toString()).getOrElse("")

  }

  class Sparql private(elements: immutable.Seq[Sparql], text: String) extends BufferedContent[Sparql](elements, text) {
    def this(text: String) = this(Nil, Formats.safe(text))

    def this(elements: immutable.Seq[Sparql]) = this(elements, "")

    def contentType: String = RdfMediaTypes.`application/sparql-query`.value

    override def body: String = super.body.replaceAll("^\\s+|\\s+$", "")

    override def toString(): String = body
  }

  object Format extends play.twirl.api.Format[Sparql] {
    def raw(text: String): Sparql = new Sparql(text)

    def escape(text: String): Sparql = Sparql.literal(text)

    def empty: Sparql = new Sparql("")

    def fill(elements: immutable.Seq[Sparql]): Sparql = new Sparql(elements)
  }

}
