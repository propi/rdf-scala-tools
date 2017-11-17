package com.github.rdfscalatools.sparqlquerytriples

import java.io.ByteArrayOutputStream

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaType
import com.github.rdfscalatools.sparqlquery.RdfMediaTypes
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.{Lang, RDFDataMgr}

/**
  * Created by Vaclav Zeman on 14. 11. 2017.
  */
object TriplesMarshallers {

  implicit def fromJenaModelToEntityMarshaller(implicit mediaTypeToJenaLang: MediaType => Lang, additionalMediaTypes: List[MediaType.WithFixedCharset] = Nil): ToEntityMarshaller[Model] = {
    Marshaller.oneOf(RdfMediaTypes.`text/turtle` :: RdfMediaTypes.`application/ld+json` :: additionalMediaTypes: _*) { mediaType =>
      Marshaller.byteArrayMarshaller(mediaType).compose[Model] { model =>
        val bos = new ByteArrayOutputStream()
        RDFDataMgr.write(bos, model, mediaType)
        bos.toByteArray
      }
    }
  }


}
