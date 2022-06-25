package com.thebinarysoul.ohc4s.codec

import java.nio.ByteBuffer
import org.caffinitas.ohc.CacheSerializer

object Serializer extends CacheSerializer[ByteBuffer] {
  override def serialize(value: ByteBuffer, buf: ByteBuffer): Unit = buf.put(value)
  override def deserialize(buf: ByteBuffer): ByteBuffer =
    if buf != null
    then buf.duplicate
    else buf
  override def serializedSize(value: ByteBuffer): Int = value.limit()
}
