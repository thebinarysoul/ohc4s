package com.thebinarysoul.ohc4s.cache

import com.thebinarysoul.ohc4s.codec.Codec
import com.thebinarysoul.ohc4s.config.CacheConf
import org.caffinitas.ohc.{CacheSerializer, OHCache, OHCacheBuilder}

import java.nio.ByteBuffer

private[cache] object CacheBuilder {
  extension[K, V] (builder: OHCacheBuilder[K, V])
    def set[T](param: Option[T], transition: OHCacheBuilder[K, V] => T => OHCacheBuilder[K, V]): OHCacheBuilder[K, V] =
      param
        .map(transition(builder))
        .getOrElse(builder)

  //TODO: It must be parameterized
  def from(conf: CacheConf, keySerializer: CacheSerializer[ByteBuffer], valueSerializer: CacheSerializer[ByteBuffer]): OHCacheBuilder[ByteBuffer, ByteBuffer] = OHCacheBuilder
    .newBuilder[ByteBuffer, ByteBuffer]
    .capacity(conf.capacity)
    .keySerializer(keySerializer)
    .valueSerializer(valueSerializer)
    .set(conf.timeouts, _.timeouts)
    .set(conf.timeoutsPrecision, _.timeoutsPrecision)
    .set(conf.timeoutsSlots, _.timeoutsSlots)
    .set(conf.ticker, _.ticker)
    .set(conf.edenSize, _.edenSize)
    .set(conf.chunkSize, _.chunkSize)
    .set(conf.fixedEntrySize, builder => (kSize, vSize) => builder.fixedEntrySize(kSize, vSize))
    .set(conf.eviction, _.eviction)
    .set(conf.defaultTTLmillis, _.defaultTTLmillis)
    .set(conf.executorService, _.executorService)
    .set(conf.frequencySketchSize, _.frequencySketchSize)
    .set(conf.hashMode, _.hashMode)
    .set(conf.hashTableSize, _.hashTableSize)
    .set(conf.loadFactor, _.loadFactor)
    .set(conf.maxEntrySize, _.maxEntrySize)
    .set(conf.segmentCount, _.segmentCount)
    .set(conf.unlocked, _.unlocked)
}


