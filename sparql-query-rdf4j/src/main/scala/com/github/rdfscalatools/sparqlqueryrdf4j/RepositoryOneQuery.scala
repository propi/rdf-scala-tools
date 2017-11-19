package com.github.rdfscalatools.sparqlqueryrdf4j

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.github.rdfscalatools.formats.BasicUnmarshallers.FromResponseWithAcceptUnmarshaller
import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import com.github.rdfscalatools.sparqlquery.query.QueryOperation
import com.github.rdfscalatools.sparqlquery.query.http.HttpOneQuery
import com.github.rdfscalatools.formats.BasicMarshallers._

/**
  * Created by Vaclav Zeman on 21. 8. 2017.
  */
abstract class RepositoryOneQuery[O](implicit actorSystem: ActorSystem, materializer: Materializer, repository: Repository, unmarshaller: FromResponseWithAcceptUnmarshaller[O])
  extends HttpOneQuery[SparqlTemplate.Sparql, O] {

  protected def operationToHttpMethod(operation: QueryOperation): HttpMethod = HttpMethods.POST

  protected def operationToUri(operation: QueryOperation): Uri = {
    val repositoryUri = Uri(repository.endpoint + "/" + "repositories" + "/" + repository.name)
    operation match {
      case QueryOperation.Insert | QueryOperation.Update | QueryOperation.Delete => repositoryUri.withPath(repositoryUri.path / "statements")
      case QueryOperation.Read => repositoryUri
    }
  }

  override protected def beforeRequest(operation: QueryOperation, request: HttpRequest): HttpRequest = operation match {
    case QueryOperation.Insert | QueryOperation.Update | QueryOperation.Delete => request.mapEntity(toSparqlUpdateWithCharset(HttpCharsets.`UTF-8`))
    case QueryOperation.Read => request.mapEntity(toSparqlQueryWithCharset(HttpCharsets.`UTF-8`))
  }

}