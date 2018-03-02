package com.github.rdfscalatools.formats.sparql

/**
  * Created by Vaclav Zeman on 1. 3. 2018.
  */
case class Prefix(prefix: String, nameSpace: String) {
  def prefixedLocalName(localName: String): String = s"$prefix:$localName"

  def fullLocalName(localName: String): String = nameSpace + localName
}