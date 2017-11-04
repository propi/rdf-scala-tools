package com.github.rdfscalatools.common.extractors

/**
  * Created by Vaclav Zeman on 24. 8. 2017.
  */
object AnyToBoolean {

  def unapply(s: Any): Option[Boolean] = if (s == null) {
    None
  } else {
    s match {
      case "true" => Some(true)
      case "false" => Some(false)
      case "1" => Some(true)
      case "0" => Some(false)
      case x: Int if x == 1 => Some(true)
      case x: Int if x == 0 => Some(false)
      case x: Short if x == 1 => Some(true)
      case x: Short if x == 0 => Some(false)
      case x: Byte if x == 1 => Some(true)
      case x: Byte if x == 0 => Some(false)
      case x: Long if x == 1 => Some(true)
      case x: Long if x == 0 => Some(false)
      case x: Float if x == 1 => Some(true)
      case x: Float if x == 0 => Some(false)
      case x: Double if x == 1 => Some(true)
      case x: Double if x == 0 => Some(false)
      case _ => None
    }
  }

}
