package com.thebinarysoul.ohc4s.cache

import com.thebinarysoul.ohc4s.codec.Serializer
import org.caffinitas.ohc.{Eviction, HashAlgorithm, Ticker}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.ByteBuffer
import java.util.concurrent.Executors
import com.thebinarysoul.ohc4s.codec.given

class CacheBuilderSpec extends AnyFlatSpec with Matchers {
  "CacheBuilder" should "create OHCCacheBuilder from CacheConf" in {
    val conf = CacheConf(
      64 * 1024 * 1024,
      Some(true),
      Some(2),
      Some(3),
      Some(Ticker.DEFAULT),
      Some(4.0),
      Some(16),
      Some(6L),
      Some((7, 8)),
      Some(Eviction.LRU),
      Some(9L),
      Some(Executors.newScheduledThreadPool(1)),
      Some(10),
      Some(HashAlgorithm.CRC32),
      Some(11),
      Some(0.5f),
      Some(12),
      Some(true)
    )

    val builder = CacheBuilder.from[Int, Int](conf)

    conf.capacity shouldBe builder.getCapacity
    conf.unlocked shouldBe Some(builder.isUnlocked)
    conf.segmentCount shouldBe Some(builder.getSegmentCount)
    conf.maxEntrySize shouldBe Some(builder.getMaxEntrySize)
    conf.ticker shouldBe Some(builder.getTicker)
    conf.loadFactor shouldBe Some(builder.getLoadFactor)
    conf.hashTableSize shouldBe Some(builder.getHashTableSize)
    conf.frequencySketchSize shouldBe Some(builder.getFrequencySketchSize)
    conf.executorService shouldBe Some(builder.getExecutorService)
    conf.defaultTTLmillis shouldBe Some(builder.getDefaultTTLmillis)
    conf.eviction shouldBe Some(builder.getEviction)
    conf.chunkSize shouldBe Some(builder.getChunkSize)
    conf.edenSize shouldBe Some(builder.getEdenSize)
    conf.fixedEntrySize.foreach {
      case (keySize, valueSize) =>
          keySize shouldBe builder.getFixedKeySize
          valueSize shouldBe builder.getFixedValueSize
    }
    conf.timeouts shouldBe Some(builder.isTimeouts)
    conf.timeoutsSlots shouldBe Some(builder.getTimeoutsSlots)
    conf.timeoutsPrecision shouldBe Some(builder.getTimeoutsPrecision)
  }
}
