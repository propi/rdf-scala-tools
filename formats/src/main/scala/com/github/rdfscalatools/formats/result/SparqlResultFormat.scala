package com.github.rdfscalatools.formats.result

import com.github.rdfscalatools.common.CommonExceptions.DeserializationException
import com.github.rdfscalatools.formats.result.SparqlResult.ResultTable

import scala.language.implicitConversions
import scala.reflect.ClassTag

/**
  * Created by Vaclav Zeman on 24. 8. 2017.
  */
object SparqlResultFormat {

  implicit def tableToOpt[T](implicit tableToSeq: ResultTable => IndexedSeq[T]): ResultTable => Option[T] = table => tableToSeq(table).headOption

  implicit def anyToKeyValueTransformer[T](x: T): KeyValueTransformer[T] = KeyValueTransformer.Mapped(x)

  sealed trait KeyValueTransformer[T] {
    def +[A](kvt: KeyValueTransformer[A]) = new KeyValueTransformer.Composer(this, kvt)
  }

  object KeyValueTransformer {

    case class Basic[T](key: String, tf: Option[SparqlResult] => T) extends KeyValueTransformer[T] {
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

      def flatMap[A](f: (T1, T2) => KeyValueTransformer[A]): KeyValueTransformer[A] = Composed({ implicit mapper =>
        vfromt(vfromt(map(f)))
      })
    }

  }

  implicit class PimpedKey(key: String) {
    def as[T](implicit tf: Option[SparqlResult] => T): KeyValueTransformer.Basic[T] = KeyValueTransformer.Basic(key, tf)

    def asO[T <: SparqlResult](implicit tag: ClassTag[T]): KeyValueTransformer.Basic[T] = KeyValueTransformer.Basic(
      key,
      x => tag.unapply(x).orElse(x.flatMap(x => tag.unapply(x))).getOrElse(throw DeserializationException(s"Value '$x' is not class of ${tag.toString()}."))
    )
  }

  private def vfromt[T](kvt: KeyValueTransformer[T])(implicit tr: Map[String, SparqlResult]): T = kvt match {
    case KeyValueTransformer.Basic(key, tf) => tf(tr.get(key))
    case KeyValueTransformer.Mapped(v) => v
    case KeyValueTransformer.Composed(tf) => tf(tr)
  }

  implicit def mergeMappers2[T1, T2](implicit f1: SparqlResult.ResultTable => IndexedSeq[T1],
                                     f2: SparqlResult.ResultTable => IndexedSeq[T2]): SparqlResult.ResultTable => IndexedSeq[(T1, T2)] = rt => f1(rt).zip(f2(rt))

  implicit def mergeMappers3[T1, T2, T3](implicit f1: SparqlResult.ResultTable => IndexedSeq[T1],
                                         f2: SparqlResult.ResultTable => IndexedSeq[T2],
                                         f3: SparqlResult.ResultTable => IndexedSeq[T3]): SparqlResult.ResultTable => IndexedSeq[(T1, T2, T3)] = rt => f1(rt).zip(f2(rt)).zip(f3(rt)).map(x => (x._1._1, x._1._2, x._2))

  implicit def mergeMappers4[T1, T2, T3, T4](implicit f1: SparqlResult.ResultTable => IndexedSeq[T1],
                                             f2: SparqlResult.ResultTable => IndexedSeq[T2],
                                             f3: SparqlResult.ResultTable => IndexedSeq[T3],
                                             f4: SparqlResult.ResultTable => IndexedSeq[T4]): SparqlResult.ResultTable => IndexedSeq[(T1, T2, T3, T4)] = rt => f1(rt).zip(f2(rt)).zip(f3(rt).zip(f4(rt))).map(x => (x._1._1, x._1._2, x._2._1, x._2._2))

  implicit def mergeMappers5[T1, T2, T3, T4, T5](implicit f1: SparqlResult.ResultTable => IndexedSeq[T1],
                                                 f2: SparqlResult.ResultTable => IndexedSeq[T2],
                                                 f3: SparqlResult.ResultTable => IndexedSeq[T3],
                                                 f4: SparqlResult.ResultTable => IndexedSeq[T4],
                                                 f5: SparqlResult.ResultTable => IndexedSeq[T5]): SparqlResult.ResultTable => IndexedSeq[(T1, T2, T3, T4, T5)] = rt => f1(rt).zip(f2(rt)).zip(f3(rt).zip(f4(rt))).zip(f5(rt)).map(x => (x._1._1._1, x._1._1._2, x._1._2._1, x._1._2._2, x._2))

  private class MappedKeysMap[T](kmap: Map[String, String], hmap: Map[String, T]) extends Map[String, T] {
    def +[V1 >: T](kv: (String, V1)): Map[String, V1] = {
      val key = kmap.getOrElse(kv._1, kv._1)
      new MappedKeysMap(kmap, hmap + (key -> kv._2))
    }

    def get(key: String): Option[T] = kmap.get(key).map(hmap.get).getOrElse(hmap.get(key))

    def iterator: Iterator[(String, T)] = {
      val ikmap = kmap.iterator.map(_.swap).toMap
      hmap.iterator.map(x => ikmap.getOrElse(x._1, x._1) -> x._2)
    }

    def -(key: String): Map[String, T] = {
      val mkey = kmap.getOrElse(key, key)
      new MappedKeysMap(kmap, hmap - mkey)
    }
  }

  def mapKeys[T, A](km: (String, String), kmo: (String, String)*)(_f: ResultTable => IndexedSeq[T])(f: (ResultTable => IndexedSeq[T]) => A): A = f { rt =>
    val kmap = Map(km +: kmo: _*)
    _f(new IndexedSeq[Map[String, SparqlResult]] {
      def length: Int = rt.length

      def apply(idx: Int): Map[String, SparqlResult] = new MappedKeysMap(kmap, rt(idx))
    })
  }

  def mapOneVariable[T, A](k: KeyValueTransformer[T])(f: (ResultTable => IndexedSeq[T]) => A): A = mapVariable(k)(k => k)(f)

  def mapVariable[T1, A, B](k1: KeyValueTransformer[T1])
                           (mapper: T1 => A)
                           (f: (ResultTable => IndexedSeq[A]) => B): B =
    f(_.map(implicit x => mapper(vfromt(k1))))

  def mapVariable[T1, T2, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2])
                               (mapper: (T1, T2) => A)
                               (f: (ResultTable => IndexedSeq[A]) => B): B =
    f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2))))

  def mapVariable[T1, T2, T3, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3])
                                   (mapper: (T1, T2, T3) => A)
                                   (f: (ResultTable => IndexedSeq[A]) => B): B =
    f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3))))

  def mapVariable[T1, T2, T3, T4, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4])
                                       (mapper: (T1, T2, T3, T4) => A)
                                       (f: (ResultTable => IndexedSeq[A]) => B): B =
    f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4))))

  def mapVariable[T1, T2, T3, T4, T5, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5])
                                           (mapper: (T1, T2, T3, T4, T5) => A)
                                           (f: (ResultTable => IndexedSeq[A]) => B): B =
    f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5))))

  def mapVariable[T1, T2, T3, T4, T5, T6, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6])(mapper: (T1, T2, T3, T4, T5, T6) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7])(mapper: (T1, T2, T3, T4, T5, T6, T7) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, T9, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8], k9: KeyValueTransformer[T9])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8), vfromt(k9))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8], k9: KeyValueTransformer[T9], k10: KeyValueTransformer[T10])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8), vfromt(k9), vfromt(k10))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8], k9: KeyValueTransformer[T9], k10: KeyValueTransformer[T10], k11: KeyValueTransformer[T11])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8), vfromt(k9), vfromt(k10), vfromt(k11))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8], k9: KeyValueTransformer[T9], k10: KeyValueTransformer[T10], k11: KeyValueTransformer[T11], k12: KeyValueTransformer[T12])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8), vfromt(k9), vfromt(k10), vfromt(k11), vfromt(k12))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8], k9: KeyValueTransformer[T9], k10: KeyValueTransformer[T10], k11: KeyValueTransformer[T11], k12: KeyValueTransformer[T12], k13: KeyValueTransformer[T13])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8), vfromt(k9), vfromt(k10), vfromt(k11), vfromt(k12), vfromt(k13))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8], k9: KeyValueTransformer[T9], k10: KeyValueTransformer[T10], k11: KeyValueTransformer[T11], k12: KeyValueTransformer[T12], k13: KeyValueTransformer[T13], k14: KeyValueTransformer[T14])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8), vfromt(k9), vfromt(k10), vfromt(k11), vfromt(k12), vfromt(k13), vfromt(k14))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8], k9: KeyValueTransformer[T9], k10: KeyValueTransformer[T10], k11: KeyValueTransformer[T11], k12: KeyValueTransformer[T12], k13: KeyValueTransformer[T13], k14: KeyValueTransformer[T14], k15: KeyValueTransformer[T15])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8), vfromt(k9), vfromt(k10), vfromt(k11), vfromt(k12), vfromt(k13), vfromt(k14), vfromt(k15))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8], k9: KeyValueTransformer[T9], k10: KeyValueTransformer[T10], k11: KeyValueTransformer[T11], k12: KeyValueTransformer[T12], k13: KeyValueTransformer[T13], k14: KeyValueTransformer[T14], k15: KeyValueTransformer[T15], k16: KeyValueTransformer[T16])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8), vfromt(k9), vfromt(k10), vfromt(k11), vfromt(k12), vfromt(k13), vfromt(k14), vfromt(k15), vfromt(k16))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8], k9: KeyValueTransformer[T9], k10: KeyValueTransformer[T10], k11: KeyValueTransformer[T11], k12: KeyValueTransformer[T12], k13: KeyValueTransformer[T13], k14: KeyValueTransformer[T14], k15: KeyValueTransformer[T15], k16: KeyValueTransformer[T16], k17: KeyValueTransformer[T17])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8), vfromt(k9), vfromt(k10), vfromt(k11), vfromt(k12), vfromt(k13), vfromt(k14), vfromt(k15), vfromt(k16), vfromt(k17))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8], k9: KeyValueTransformer[T9], k10: KeyValueTransformer[T10], k11: KeyValueTransformer[T11], k12: KeyValueTransformer[T12], k13: KeyValueTransformer[T13], k14: KeyValueTransformer[T14], k15: KeyValueTransformer[T15], k16: KeyValueTransformer[T16], k17: KeyValueTransformer[T17], k18: KeyValueTransformer[T18])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8), vfromt(k9), vfromt(k10), vfromt(k11), vfromt(k12), vfromt(k13), vfromt(k14), vfromt(k15), vfromt(k16), vfromt(k17), vfromt(k18))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8], k9: KeyValueTransformer[T9], k10: KeyValueTransformer[T10], k11: KeyValueTransformer[T11], k12: KeyValueTransformer[T12], k13: KeyValueTransformer[T13], k14: KeyValueTransformer[T14], k15: KeyValueTransformer[T15], k16: KeyValueTransformer[T16], k17: KeyValueTransformer[T17], k18: KeyValueTransformer[T18], k19: KeyValueTransformer[T19])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8), vfromt(k9), vfromt(k10), vfromt(k11), vfromt(k12), vfromt(k13), vfromt(k14), vfromt(k15), vfromt(k16), vfromt(k17), vfromt(k18), vfromt(k19))))

  def mapVariable[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, A, B](k1: KeyValueTransformer[T1], k2: KeyValueTransformer[T2], k3: KeyValueTransformer[T3], k4: KeyValueTransformer[T4], k5: KeyValueTransformer[T5], k6: KeyValueTransformer[T6], k7: KeyValueTransformer[T7], k8: KeyValueTransformer[T8], k9: KeyValueTransformer[T9], k10: KeyValueTransformer[T10], k11: KeyValueTransformer[T11], k12: KeyValueTransformer[T12], k13: KeyValueTransformer[T13], k14: KeyValueTransformer[T14], k15: KeyValueTransformer[T15], k16: KeyValueTransformer[T16], k17: KeyValueTransformer[T17], k18: KeyValueTransformer[T18], k19: KeyValueTransformer[T19], k20: KeyValueTransformer[T20])(mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => A)(f: (ResultTable => IndexedSeq[A]) => B): B = f(_.map(implicit x => mapper(vfromt(k1), vfromt(k2), vfromt(k3), vfromt(k4), vfromt(k5), vfromt(k6), vfromt(k7), vfromt(k8), vfromt(k9), vfromt(k10), vfromt(k11), vfromt(k12), vfromt(k13), vfromt(k14), vfromt(k15), vfromt(k16), vfromt(k17), vfromt(k18), vfromt(k19), vfromt(k20))))

}