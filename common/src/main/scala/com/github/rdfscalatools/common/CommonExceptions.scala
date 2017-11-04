package com.github.rdfscalatools.common

/**
  * Created by Vaclav Zeman on 30. 8. 2017.
  */
object CommonExceptions {

  case class SerializationException(msg: String) extends RuntimeException(msg)

  case class DeserializationException(msg: String) extends RuntimeException(msg)

}
