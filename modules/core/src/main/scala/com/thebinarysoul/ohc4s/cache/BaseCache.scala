package com.thebinarysoul.ohc4s.cache

import com.thebinarysoul.ohc4s.codec.Codec
import org.caffinitas.ohc.OHCache

abstract class BaseCache[F[_], A[_], K: Codec, V: Codec](conf: CacheConf) extends Cache[F, A, K, V] {
  protected lazy val cache: OHCache[K, V] = CacheBuilder
    .from(conf)
    .build
}
