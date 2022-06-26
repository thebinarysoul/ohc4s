package com.thebinarysoul.ohc4s.cache

import com.thebinarysoul.ohc4s.codec.Codec
import org.caffinitas.ohc.OHCache

import java.nio.ByteBuffer

trait Cache[F[_], K, V] {
  protected val cache: OHCache[ByteBuffer, ByteBuffer]

  def put(key: K, value: V): F[Boolean]
  def get(key: K): F[Option[V]]
}

object Cache {
  def create[K, V](capacity: Option[Long] = None)(using Codec[K], Codec[V]): DefaultCache[K, V] = DefaultCache(capacity)
}