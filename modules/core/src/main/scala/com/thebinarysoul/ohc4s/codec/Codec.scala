package com.thebinarysoul.ohc4s.codec

import com.thebinarysoul.ohc4s.codec.Codec.*
import com.thebinarysoul.ohc4s.codec.Num.*

import java.nio.ByteBuffer as Buffer
import scala.deriving.Mirror.ProductOf
import scala.deriving.Mirror
import scala.util.chaining.*

/** A type class that provides functions for encoding, decoding of values of type 'T' via [[Buffer`]] provided by
  * [[org.caffinitas.ohc.CacheSerializer]].
  *
  * It also provides a function to estimate the size of value of type 'T'
  */
trait Codec[T]:
  def encoder: Encoder[T]
  def decoder: Decoder[T]
  def sizeEstimator: Estimator[T]

object Codec {
  type Encoder[T] = Buffer => T => Buffer
  type Decoder[T] = Buffer => T
  type Estimator[T] = T => Int

  private[codec] inline def encodeIterable[T](iterable: Iterable[T], buffer: Buffer, encoder: Encoder[T]) = iterable
    .foldLeft(buffer.putInt(iterable.size)) { (buffer, next) =>
      encoder
        .apply(buffer)
        .apply(next)
    }

  private[codec] inline def decodeIterable[T](buffer: Buffer, decoder: Decoder[T]) = buffer.getInt
    .pipe(size => Iterable.fill(size)(decoder apply buffer))

  private[codec] inline def estimateIterableSize[T](iterable: Iterable[T], estimator: Estimator[T]): Int =
    int[8] + iterable.map(estimator).sum
}

inline given Codec[Byte] with
  override val encoder: Encoder[Byte] = _.put
  override val decoder: Decoder[Byte] = _.get
  override val sizeEstimator: Estimator[Byte] = _ => int[1]

inline given Codec[Short] with
  override val encoder: Encoder[Short] = _.putShort
  override val decoder: Decoder[Short] = _.getShort
  override val sizeEstimator: Estimator[Short] = _ => int[2]

inline given Codec[Int] with
  override val encoder: Encoder[Int] = _.putInt
  override val decoder: Decoder[Int] = _.getInt
  override val sizeEstimator: Estimator[Int] = _ => int[4]

inline given Codec[Long] with
  override val encoder: Encoder[Long] = _.putLong
  override val decoder: Decoder[Long] = _.getLong
  override val sizeEstimator: Estimator[Long] = _ => int[8]

inline given Codec[Float] with
  override val encoder: Encoder[Float] = _.putFloat
  override val decoder: Decoder[Float] = _.getFloat
  override val sizeEstimator: Estimator[Float] = _ => int[4]

inline given Codec[Double] with
  override val encoder: Encoder[Double] = _.putDouble
  override val decoder: Decoder[Double] = _.getDouble
  override val sizeEstimator: Estimator[Double] = _ => int[8]

inline given Codec[Boolean] with
  override val encoder: Encoder[Boolean] = buffer => value => buffer.put(if value then byte[1] else byte[0])
  override val decoder: Decoder[Boolean] = buffer => buffer.get == byte[1]
  override val sizeEstimator: Estimator[Boolean] = _ => int[1]

inline given Codec[Array[Byte]] with
  override def encoder: Encoder[Array[Byte]] = buffer =>
    array =>
      buffer
        .putInt(array.length)
        .put(array)

  override def decoder: Decoder[Array[Byte]] = buffer =>
    buffer.getInt
      .pipe { size =>
        val bytes = new Array[Byte](size)
        buffer.get(bytes)
        bytes
      }

  override def sizeEstimator: Estimator[Array[Byte]] = _.length + int[4]

inline given Codec[String] with
  override val encoder: Encoder[String] = buffer =>
    _.getBytes
      .pipe { bytes =>
        buffer
          .putInt(bytes.length)
          .put(bytes)
      }

  override val decoder: Decoder[String] = buffer =>
    buffer.getInt
      .pipe { size =>
        val bytes = new Array[Byte](size)
        buffer.get(bytes)
        String(bytes)
      }

  override val sizeEstimator: Estimator[String] = string => int[4] + string.getBytes.length

inline given optCodec[T](using codec: Codec[T]): Codec[Option[T]] = new Codec[Option[T]]:
  override val encoder: Encoder[Option[T]] = buffer => {
    case None => buffer.put(byte[0])
    case Some(value) =>
      codec.encoder
        .apply(buffer.put(byte[1]))
        .apply(value)
  }

  override val decoder: Decoder[Option[T]] = buffer => {
    if buffer.get == byte[1]
    then Some(codec.decoder(buffer))
    else None
  }

  override val sizeEstimator: Estimator[Option[T]] = value => int[1] + value.map(codec.sizeEstimator).getOrElse(0)

inline given listCodec[T](using codec: Codec[T]): Codec[List[T]] = new Codec[List[T]]:
  override val encoder: Encoder[List[T]] = buffer => list => encodeIterable(list, buffer, codec.encoder)
  override val decoder: Decoder[List[T]] = buffer => decodeIterable(buffer, codec.decoder).toList
  override val sizeEstimator: Estimator[List[T]] = list => estimateIterableSize(list, codec.sizeEstimator)

inline given mapCodec[K, V](using codec: Codec[(K, V)]): Codec[Map[K, V]] = new Codec[Map[K, V]]:
  override val encoder: Encoder[Map[K, V]] = buffer => map => encodeIterable(map, buffer, codec.encoder)
  override val decoder: Decoder[Map[K, V]] = buffer => decodeIterable(buffer, codec.decoder).toMap
  override val sizeEstimator: Estimator[Map[K, V]] = map => estimateIterableSize(map, codec.sizeEstimator)

inline given [H, T <: Tuple](using headCodec: Codec[H], tailCodec: Codec[T]): Codec[H *: T] with
  override val encoder: Encoder[H *: T] = buffer => { case head *: tail =>
    headCodec.encoder
      .apply(buffer)
      .andThen(tailCodec.encoder)
      .apply(head)
      .apply(tail)
  }

  override val decoder: Decoder[H *: T] = buffer => headCodec.decoder(buffer) *: tailCodec.decoder(buffer)

  override val sizeEstimator: Estimator[H *: T] = { case head *: tail =>
    headCodec.sizeEstimator(head) + tailCodec.sizeEstimator(tail)
  }

inline given Codec[EmptyTuple] with
  override val encoder: Encoder[EmptyTuple] = buffer => _ => buffer
  override val decoder: Decoder[EmptyTuple] = _ => EmptyTuple
  override val sizeEstimator: Estimator[Any] = _ => int[0]

inline given productCodec[P <: Product](using m: Mirror.ProductOf[P], metCodec: Codec[m.MirroredElemTypes]): Codec[P] =
  new Codec[P]:
    override val encoder: Encoder[P] = buffer =>
      value =>
        metCodec.encoder
          .apply(buffer)
          .apply(Tuple.fromProductTyped(value))

    override val decoder: Decoder[P] = buffer =>
      metCodec.decoder
        .apply(buffer)
        .pipe(m.fromProduct)

    override val sizeEstimator: Estimator[P] = value => metCodec.sizeEstimator(Tuple.fromProductTyped(value))
