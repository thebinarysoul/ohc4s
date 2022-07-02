package com.thebinarysoul.ohc4s.codec

import java.nio.ByteBuffer
import org.caffinitas.ohc.CacheSerializer
import com.thebinarysoul.ohc4s.codec.Codec

private[ohc4s] class Serializer[T](using codec: Codec[T]) extends CacheSerializer[T] {
  override def serialize(value: T, buf: ByteBuffer): Unit = codec
    .encoder
    .apply(buf)
    .apply(value)
  
  override def deserialize(buf: ByteBuffer): T = codec.decoder(buf)
  override def serializedSize(value: T): Int = codec.sizeEstimator(value)
}
