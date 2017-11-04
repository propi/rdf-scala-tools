package com.github.rdfscalatools.sparqlquery.result

import com.github.rdfscalatools.sparqlquery.result.SparqlResult.ResultTable

import scala.language.implicitConversions

/**
  * Created by Vaclav Zeman on 24. 8. 2017.
  */
object SparqlResultFormat {

  implicit def tableToOpt[T](implicit tableToSeq: ResultTable => IndexedSeq[T]): (ResultTable) => Option[T] = table => tableToSeq(table).headOption

  implicit def anyToKeyValueTransformer[T](x: T): KeyValueTransformer[T] = KeyValueTransformer.Mapped(x)

  sealed trait KeyValueTransformer[T] {
    def +[A](kvt: KeyValueTransformer[A]) = new KeyValueTransformer.Composer(this, kvt)
  }

  object KeyValueTransformer {

    case class Basic[T](key: String, tf: SparqlResult => T) extends KeyValueTransformer[T] {
      def map[A](f: T => A): KeyValueTransformer[A] = new Basic[A](key, tf andThen f)
    }

    case class Mapped[T](value: T) extends KeyValueTransformer[T]

    case class Composed[T](tf: Map[String, SparqlResult] => T) extends KeyValueTransformer[T]

    class Composer[T1, T2](kvt1: KeyValueTransformer[T1], kvt2: KeyValueTransformer[T2]) {
      def map[A](f: (T1, T2) => A): KeyValueTransformer[A] = Composed({ implicit mapper =>
        val x1 = vfromt(kvt1)
        val x2 = vfromt(kvt2)
        f(x1, x2)
      })
    }

  }

  implicit class PimpedKey(key: String) {
    def as[T](implicit tf: SparqlResult => T): KeyValueTransformer.Basic[T] = KeyValueTransformer.Basic(key, tf)
  }

  private def vfromt[T](kvt: KeyValueTransformer[T])(implicit tr: Map[String, SparqlResult]): T = kvt match {
    case KeyValueTransformer.Basic(key, tf) => tf(tr(key))
    case KeyValueTransformer.Mapped(v) => v
    case KeyValueTransformer.Composed(tf) => tf(tr)
  }

  def mapVariable[T1, A, B](k1: KeyValueTransformer[T1])
                           (mapper: T1 => A)
                           (f: (ResultTable => IndexedSeq[A]) => B) =
    f(_.map(implicit x => mapper(vfromt(k1))))

  def mapVariable[T1, T2, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2])
                               (mapper: (T1, T2) => A)
                               (f: (ResultTable => IndexedSeq[A]) => B) =
    f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2))))

  def mapVariable[T1, T2, T3, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3])
                                   (mapper: (T1, T2, T3) => A)
                                   (f: (ResultTable => IndexedSeq[A]) => B) =
    f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3))))

  def mapVariable[T1, T2, T3, T4, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4])
                                       (mapper: (T1, T2, T3, T4) => A)
                                       (f: (ResultTable => IndexedSeq[A]) => B) =
    f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4))))


}