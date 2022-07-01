package com.thebinarysoul.ohc4s.config

import com.thebinarysoul.ohc4s.codec.{Codec, Serializer}
import org.caffinitas.ohc.{CacheSerializer, Eviction, HashAlgorithm, OHCache, OHCacheBuilder, Ticker}

import java.nio.ByteBuffer
import java.util.concurrent.{ExecutorService, Executors, ScheduledExecutorService}
import scala.util.chaining.*

final case class CacheConf
(
  capacity: Long,
  timeouts: Option[Boolean] = None,
  timeoutsPrecision: Option[Int] = None,
  timeoutsSlots: Option[Int] = None,
  ticker: Option[Ticker] = None,
  edenSize: Option[Double] = None,
  chunkSize: Option[Int] = None,
  maxEntrySize: Option[Long] = None,
  fixedEntrySize: Option[(Int, Int)] = None,
  eviction: Option[Eviction] = None,
  defaultTTLmillis: Option[Long] = None,
  executorService: Option[ScheduledExecutorService] = None,
  frequencySketchSize: Option[Int] = None,
  hashMode: Option[HashAlgorithm] = None,
  hashTableSize: Option[Int] = None,
  loadFactor: Option[Float] = None,
  segmentCount: Option[Int] = None,
  unlocked: Option[Boolean] = None
)

