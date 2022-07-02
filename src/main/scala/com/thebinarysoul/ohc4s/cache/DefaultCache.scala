package com.thebinarysoul.ohc4s.cache

import com.thebinarysoul.ohc4s.codec.{Codec, Serializer}
import org.caffinitas.ohc.{OHCache, OHCacheBuilder}

import util.chaining.scalaUtilChainingOps
import java.nio.ByteBuffer

private[cache] class DefaultCache[K, V](conf: CacheConf)(using Codec[K], Codec[V]) extends BaseCache[[T] =>> T, K, V](conf) {
  override def put(key: K, value: V): Boolean = cache.put(key, value)
  override def get(key: K): Option[V] = Option(cache.get(key))
}