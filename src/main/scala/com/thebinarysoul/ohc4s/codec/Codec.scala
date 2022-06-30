package com.thebinarysoul.ohc4s.codec

import com.thebinarysoul.ohc4s.codec.Codec.*
import com.thebinarysoul.ohc4s.codec.Num.*

import java.nio.ByteBuffer as Buffer
import scala.deriving.Mirror.ProductOf
import scala.deriving.Mirror
import scala.util.chaining.*

trait Codec[T]:
  def encode(value: T): Buffer
  def decode(buffer: Buffer): T

object Codec {
  private[codec] inline def encodeWith(size: Int)(put: Buffer => Buffer): Buffer = Buffer
    .allocate(size)
    .pipe(put)
    .flip

  private[codec] inline def merge(left: Buffer, right: Buffer): Buffer = Buffer
    .allocate(left.limit() + right.limit())
    .put(left)
    .put(right)
    .flip
}

inline given Codec[Byte] with
  override def encode(value: Byte): Buffer = encodeWith(int[1])(_.put(value))
  override def decode(buffer: Buffer): Byte = buffer.get

inline given Codec[Short] with
  override def encode(value: Short): Buffer = encodeWith(int[2])(_.putShort(value))
  override def decode(buffer: Buffer): Short = buffer.getShort

inline given Codec[Int] with
  override def encode(value: Int): Buffer = encodeWith(int[4])(_.putInt(value))
  override def decode(buffer: Buffer): Int = buffer.getInt

inline given Codec[Long] with
  override def encode(value: Long): Buffer = encodeWith(int[8])(_.putLong(value))
  override def decode(buffer: Buffer): Long = buffer.getLong

inline given Codec[Float] with
  override def encode(value: Float): Buffer = encodeWith(int[4])(_.putFloat(value))
  override def decode(buffer: Buffer): Float = buffer.getFloat

inline given Codec[Double] with
  override def encode(value: Double): Buffer = encodeWith(int[8])(_.putDouble(value))
  override def decode(buffer: Buffer): Double = buffer.getDouble

inline given Codec[Boolean] with
  override def encode(value: Boolean): Buffer = encodeWith(int[1])(_.put(if value then byte[1] else byte[0]))
  override def decode(buffer: Buffer): Boolean = buffer.get == int[1]

inline given Codec[String] with
  override def encode(value: String): Buffer =
    val bytes = value.getBytes
    merge(
      encodeWith(int[4])(_.putInt(bytes.length)),
      encodeWith(bytes.length)(_.put(bytes))
    )

  override def decode(buffer: Buffer): String =
    val size = buffer.getInt
    val array = new Array[Byte](size)
    buffer.get(array)
    String(array)

inline given optCodec[T](using codec: Codec[T]): Codec[Option[T]] = new Codec[Option[T]]:
  override def encode(value: Option[T]): Buffer = value match {
    case Some(value) =>
      merge(
        encodeWith(int[1])(_.put(byte[1])),
        codec.encode(value)
      )
    case _ => encodeWith(int[1])(_.put(byte[0]))
  }

  override def decode(buffer: Buffer): Option[T] =
    if buffer.get == int[1]
    then Some(codec.decode(buffer))
    else None

inline given listCodec[T](using codec: Codec[T]): Codec[List[T]] = new Codec[List[T]]:
  override def encode(value: List[T]): Buffer =
    val sizeBuffer = encodeWith(int[4])(_.putInt(value.size))
    value
      .map(codec.encode)
      .foldLeft(sizeBuffer)((a, b) => merge(a, b))

  override def decode(buffer: Buffer): List[T] =
    val size = buffer.getInt
    List.fill(size)(codec.decode(buffer))

inline given mapCodec[K, V](using keyCodec: Codec[K], valueCodec: Codec[V]): Codec[Map[K, V]] = new Codec[Map[K, V]]:
  override def encode(value: Map[K, V]): Buffer = {
    val sizeBuffer = encodeWith(int[4])(_.putInt(value.size))
    value
      .map { case (key, value) => merge(keyCodec.encode(key), valueCodec.encode(value)) }
      .foldLeft(sizeBuffer)((a, b) => merge(a, b))
  }

  override def decode(buffer: Buffer): Map[K, V] =
    val size = buffer.getInt
    Seq
      .fill(size)(keyCodec.decode(buffer) -> valueCodec.decode(buffer))
      .toMap

inline given [H: Codec, T <: Tuple: Codec]: Codec[H *: T] with
  override def encode(value: H *: T): Buffer = value match
    case head *: tail => merge(
      summon[Codec[H]].encode(head),
      summon[Codec[T]].encode(tail)
    )

  override def decode(buffer: Buffer): H *: T = summon[Codec[H]].decode(buffer) *: summon[Codec[T]].decode(buffer)

inline given Codec[EmptyTuple] with
  override def encode(value: EmptyTuple): Buffer = Buffer.allocate(0)
  override def decode(buffer: Buffer): EmptyTuple = EmptyTuple

inline given productCodec[P <: Product](using m: Mirror.ProductOf[P], metCodec: Codec[m.MirroredElemTypes]): Codec[P] = new Codec[P]:
  override def encode(value: P): Buffer = metCodec.encode(Tuple.fromProductTyped(value))
  override def decode(buffer: Buffer): P = m.fromProduct(metCodec.decode(buffer))