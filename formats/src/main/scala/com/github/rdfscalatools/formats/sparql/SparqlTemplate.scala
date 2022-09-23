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

    lazy val dummyUri: Sparql = curi("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")

    implicit private def escapeChar(char: Char): Seq[Char] = List('\\', char)

    implicit private def stringToSparql(string: String): Sparql = new Sparql(string)

    private val IRI_REF1 = "<[^<>\"{}|^`\\\\]+>"
    private val IRI_REF2 = "<[^\\u0000-\\u0020]+>"
    private val PN_CHARS_BASE = "[A-Z]|[a-z]|[\\u00C0-\\u00D6]|[\\u00D8-\\u00F6]|[\\u00F8-\\u02FF]|[\\u0370-\\u037D]|[\\u037F-\\u1FFF]|[\\u200C-\\u200D]|[\\u2070-\\u218F]|[\\u2C00-\\u2FEF]|[\\u3001-\\uD7FF]|[\\uF900-\\uFDCF]|[\\uFDF0-\\uFFFD]"
    private val PN_CHARS_U = PN_CHARS_BASE + "|_"
    private val PN_CHARS = PN_CHARS_U + "|-|[0-9]|[\\u00B7]|[\\u0300-\\u036F]|[\\u203F-\\u2040]"
    private val PN_PREFIX = s"($PN_CHARS_BASE)(($PN_CHARS|[.])*($PN_CHARS))?"
    private val PLX = "(%([0-9]|[A-F]|[a-f]){2})|(\\\\[-_~.!$&'()*+,;=/?#@%])"
    private val PN_LOCAL = s"($PN_CHARS_U|[0-9]|$PLX)(($PN_CHARS|.|:|$PLX)*($PN_CHARS|$PLX))?"

    private def prefixedUriIsValid(string: String): Boolean = {
      val (p1, p2) = string.span(_ != ':')
      (p1.isEmpty || p1.matches(PN_PREFIX)) && p2.matches(s":$PN_LOCAL")
    }

    def prefix(prefixName: String, nameSpace: String): Sparql = if (prefixName.isEmpty || prefixName.matches(PN_PREFIX)) {
      s"PREFIX $prefixName: ${curi(nameSpace).toString()}"
    } else {
      throw new InvalidSparqlFormat(s"Invalid prefix name in sparql query: $prefixName")
    }

    def prefix(prefixObject: Prefix): Sparql = prefix(prefixObject.prefix, prefixObject.nameSpace)

    def curi(string: String): Sparql = uri(s"<$string>")

    def uri(string: String): Sparql = if ((string.matches(IRI_REF1) && string.matches(IRI_REF2)) || prefixedUriIsValid(string)) {
      string
    } else {
      throw new InvalidSparqlFormat("Invalid uri in sparql query: " + string)
    }

    def puri(prefixObject: Prefix, localName: String): Sparql = uri(s"${prefixObject.prefix}:$localName")

    def literal(string: String): Sparql = "\"" + string.iterator.flatMap {
      case '\n' => 'n'
      case '\t' => 't'
      case '\b' => 'b'
      case '\r' => 'r'
      case '\f' => 'f'
      case '"' => '"'
      case '\'' => '\''
      case '\\' => '\\'
      case x => List(x)
    }.mkString + "\""

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

    def in(col: Iterable[Sparql]): Sparql = col.view.map(_.toString()).reduceLeftOption((a, b) => s"$a, $b").map(stringToSparql).getOrElse(dummyUri)

    def variable(x: Char): Sparql = if (x >= 'a' && x <= 'z') "?" + x else throw new InvalidSparqlFormat("Invalid variable character in sparql query")

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
