package com.github.rdfscalatools.sparqlquerytdb

import com.github.rdfscalatools.sparqlquery.query.Transaction
import org.apache.jena.query.Dataset

/**
  * Created by Vaclav Zeman on 9. 2. 2018.
  */
class RepositoryTransaction(implicit dataset: Dataset) extends Transaction {
  def commit(): Unit = dataset.commit()

  def rollback(): Unit = dataset.abort()
}
