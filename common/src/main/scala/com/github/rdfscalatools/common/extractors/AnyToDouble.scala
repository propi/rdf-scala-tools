package com.github.rdfscalatools.common.extractors

/**
  * Created by Vaclav Zeman on 24. 8. 2017.
  */
object AnyToDouble {

  def unapply(s: Any): Option[Double] = try {
    if (s == null)
      None
    else
      Some(s match {
        case x: Int => x.toDouble
        case x: Double => x
        case x: Float => x.toDouble
        case x: Long => x.toDouble
        case x: Short => x.toDouble
        case x: Byte => x.toDouble
        case x => x.toString.toDouble
      })
  } catch {
    case _: java.lang.NumberFormatException => None
  }

}
