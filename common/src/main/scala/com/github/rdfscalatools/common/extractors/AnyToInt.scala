package com.github.rdfscalatools.common.extractors

/**
  * Created by Vaclav Zeman on 24. 8. 2017.
  */
object AnyToInt {

  def unapply(s: Any): Option[Int] = try {
    if (s == null)
      None
    else
      Some(s match {
        case x: Int => x
        case x: Short => x.toInt
        case x: Byte => x.toInt
        case x => x.toString.toInt
      })
  } catch {
    case _: java.lang.NumberFormatException => None
  }

}
