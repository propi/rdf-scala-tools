package com.github.rdfscalatools.sparqlqueryrdf4j

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.stream.Materializer
import com.github.rdfscalatools.formats.BasicUnmarshallers.FromResponseWithAcceptUnmarshaller
import com.github.rdfscalatools.formats.sparql.SparqlTemplate
import com.github.rdfscalatools.sparqlquery.query.QueryOperation
import com.github.rdfscalatools.sparqlquery.query.http.HttpOneQuery
import com.github.rdfscalatools.formats.BasicMarshallers._

import scala.concurrent.ExecutionContext

/**
  * Created by Vaclav Zeman on 21. 8. 2017.
  */
class RepositoryOneQuery[O] private[sparqlqueryrdf4j](implicit actorSystem: ActorSystem, ec: ExecutionContext, materializer: Materializer, repository: Repository, unmarshaller: FromResponseWithAcceptUnmarshaller[O], connectionPoolSetting: Option[ConnectionPoolSettings] = None)
  extends HttpOneQuery[SparqlTemplate.Sparql, O] {

  protected def operationToHttpMethod(operation: QueryOperation): HttpMethod = HttpMethods.POST

  protected def operationToUri(operation: QueryOperation): Uri = {
    val repositoryUri = Uri(repository.endpoint + "/" + "repositories" + "/" + repository.name)
    operation match {
      case QueryOperation.Update => repositoryUri.withPath(repositoryUri.path / "statements")
      case _ => repositoryUri
    }
  }

  override protected def beforeRequest(operation: QueryOperation, request: HttpRequest): HttpRequest = operation match {
    case QueryOperation.Update => request.mapEntity(toSparqlUpdateWithCharset(HttpCharsets.`UTF-8`))
    case _ => request.mapEntity(toSparqlQueryWithCharset(HttpCharsets.`UTF-8`))
  }

}