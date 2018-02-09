package com.github.rdfscalatools.sparqlquerytdb

import com.github.rdfscalatools.sparqlquery.query.TransactionBuilder
import org.apache.jena.query.Dataset

/**
  * Created by Vaclav Zeman on 9. 2. 2018.
  */
object RepositoryTransactionBuilder {

  implicit def repositoryBuilder(implicit dataset: Dataset): TransactionBuilder[RepositoryTransaction] = new TransactionBuilder[RepositoryTransaction] {
    def apply(): RepositoryTransaction = ???
  }

}
