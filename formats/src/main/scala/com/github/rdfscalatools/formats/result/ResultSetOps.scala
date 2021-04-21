package com.github.rdfscalatools.formats.result

import com.github.rdfscalatools.formats.result.SparqlResult.ResultTable
import org.apache.jena.query.ResultSet

import scala.jdk.CollectionConverters._

/**
  * Created by Vaclav Zeman on 12. 2. 2018.
  */
object ResultSetOps {

  implicit class PimpedResultSet(resultSet: ResultSet) {
    def toResultTable: ResultTable = resultSet.asScala.map { qs =>
      qs.varNames().asScala.map(varName => varName -> SparqlResult(qs.get(varName).asNode())).toMap
    }.toVector
  }

}