package com.thebinarysoul.ohc4s.cache

import com.thebinarysoul.ohc4s.codec.{Codec, Serializer}
import com.thebinarysoul.ohc4s.config.CacheConf
import org.caffinitas.ohc.{OHCache, OHCacheBuilder}

import util.chaining.scalaUtilChainingOps
import java.nio.ByteBuffer

trait Cache[F[_], K, V](conf: CacheConf)(using Codec[K], Codec[V])  {
  protected lazy val cache: OHCache[ByteBuffer, ByteBuffer] = CacheBuilder
    .from(conf, Serializer, Serializer)
    .build

  def put(key: K, value: V): F[Boolean]
  def get(key: K): F[Option[V]]
}

object Cache {
  def create[K, V](capacity: Long)(using Codec[K], Codec[V]): DefaultCache[K, V] = DefaultCache(CacheConf(capacity))
  def create[K, V](conf: CacheConf)(using Codec[K], Codec[V]): DefaultCache[K, V] = DefaultCache(conf)
}