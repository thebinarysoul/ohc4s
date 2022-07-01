package com.thebinarysoul.ohc4s.cache

import com.thebinarysoul.ohc4s.codec.{Codec, Serializer}
import com.thebinarysoul.ohc4s.config.CacheConf
import org.caffinitas.ohc.{OHCache, OHCacheBuilder}

import util.chaining.scalaUtilChainingOps
import java.nio.ByteBuffer

private[cache] class DefaultCache[K, V](conf: CacheConf)(using Codec[K], Codec[V]) extends Cache[[T] =>> T, K, V](conf) {
  private implicit inline def encode[T](value: T)(using codec: Codec[T]): ByteBuffer = codec.encode(value)
  private implicit inline def decode[T](buffer: ByteBuffer)(using codec: Codec[T]): Option[T] = Option(buffer).map(codec.decode)

  override def put(key: K, value: V): Boolean = cache.put(key, value)
  override def get(key: K): Option[V] = cache.get(key)
}