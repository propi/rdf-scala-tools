package com.github.rdfscalatools.sparqlquerytriples

import com.github.rdfscalatools.sparqlquery.sparql.SparqlTemplate.{Format, Sparql}
import org.apache.jena.atlas.io.StringWriterI
import org.apache.jena.graph._
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.out.{NodeFormatter, NodeFormatterNT}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

/**
  * Created by Vaclav Zeman on 15. 11. 2017.
  */
case class SparqlTriple(s: Sparql, p: Sparql, o: Sparql)

object SparqlTriple {

  implicit private def resourceToSparql(r: Node)(implicit nodeFormatter: NodeFormatter): Sparql = {
    val writer = new StringWriterI
    nodeFormatter.format(writer, r)
    Format.raw(writer.toString)
  }

  implicit def modelToSparqlTripleIterable(model: Model): Iterable[SparqlTriple] = {
    implicit val nodeFormatter: NodeFormatter = new NodeFormatterNT()
    new Iterable[SparqlTriple] {
      def iterator: Iterator[SparqlTriple] = model.listStatements().asScala.map(x => Triple.create(x.getSubject.asNode(), x.getPredicate.asNode(), x.getObject.asNode()))
    }
  }

  implicit def apply(triple: Triple)(implicit nodeFormatter: NodeFormatter): SparqlTriple = SparqlTriple(triple.getSubject, triple.getPredicate, triple.getObject)

}
