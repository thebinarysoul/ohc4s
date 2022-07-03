package com.thebinarysoul.ohc4s.cache

import com.thebinarysoul.ohc4s.codec.*
import com.thebinarysoul.ohc4s.codec.given
import org.caffinitas.ohc.CacheLoader
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import scala.util.Using

class DefaultCacheSpec extends AnyFlatSpec with Matchers with BeforeAndAfterEach {
  private val cache = Cache.create[String, List[(String, Int)]](1024 * 16)

  override protected def beforeEach(): Unit = {
    cache.clear()
    cache.resetStatistics()
  }

  "cache" should "put (key, value)" in {
    cache.put("key", List(("a", 1), ("b", 2))) shouldBe true
  }

  "cache" should "put (key, value) if key absences" in {
    cache.putIfAbsent("key", List(("a", 1), ("b", 2))) shouldBe true
  }

  "cache" should "not put (key, value) if key presents" in {
    cache.put("key", Nil) shouldBe true
    cache.putIfAbsent("key", List(("a", 1), ("b", 2))) shouldBe false
  }

  "cache" should "replace old value with new one" in {
    cache.putAll(data)
    cache.addOrReplace("1", List(("1", 1)), List(("a", 1))) shouldBe true
    cache.get("1") shouldBe Some(List(("a", 1)))
  }

  "cache" should "not replace old value with new one" in {
    cache.putAll(data)
    cache.addOrReplace("1", List(("10", 1)), List(("a", 1))) shouldBe false
    cache.get("1") shouldBe Some(List(("1", 1)))
  }

  "cache" should "return keyIterator" in {
    cache.putAll(data)
    Using
      .resource(cache.keyIterator) { iterator =>
        iterator.toList.sorted shouldBe data.keys.toList.sorted
      }
  }

  "cache" should "return keyBufferIterator" in {
    cache.putAll(data)
    Using
      .resource(cache.keyBufferIterator) { iterator =>
        iterator.map(summon[Codec[String]].decoder).toList.sorted shouldBe data.keys.toList.sorted
      }
  }

  "cache" should "contain a key" in {
    cache.put("key", List(("a", 1)))
    cache.containsKey("key") shouldBe true
  }

  "cache" should "not contain a key" in {
    cache.containsKey("key") shouldBe false
  }

  "cache" should "get Some(value)" in {
    cache.put("key", List(("a", 1), ("b", 2))) shouldBe true
    cache.get("key") shouldBe Some(List(("a", 1), ("b", 2)))
  }

  "cache" should "get None" in {
    cache.get("key2") shouldBe None
  }

  "cache" should "not get DirectAccessValue" in {
    cache.getDirect("key2") shouldBe None
    cache.getDirect("key3", true) shouldBe None
  }

  "cache" should "get DirectAccessValue" in {
    cache.putAll(data)
    val raw = List(
      cache.getDirect("1"),
      cache.getDirect("2", true)
    )

    val values = raw.flatten.map { dva =>
      summon[Codec[List[(String, Int)]]].decoder(dva.buffer)
    }

    values shouldBe List(List(("1", 1)), List(("2", 2)))
  }

  "cache" should "put Map[K, V]" in {
    cache.putAll(data)

    data.foreach {
      case (key, value) => cache.get(key) shouldBe Some(value)
    }
  }

  "cache" should "remove all entries" in {
    cache.putAll(data)

    cache.size shouldBe 10
    cache.clear()
    cache.size shouldBe 0
  }

  "cache" should "remove an entry by key" in {
    cache.putAll(data)

    cache.remove("1") shouldBe true
    cache.size shouldBe 9
  }

  "cache" should "return capacity" in {
    cache.capacity shouldBe 1024 * 16
  }

  "cache" should "return loadFactor" in {
    cache.loadFactor shouldBe 0.75f
  }

  "cache" should "return freeCapacity" in {
    cache.freeCapacity shouldBe cache.capacity
    cache.put("1", Nil)
    cache.freeCapacity shouldBe cache.capacity - 80
  }

  "cache" should "return memUsed" in {
    cache.memUsed shouldBe 0
    cache.put("1", Nil)
    cache.memUsed shouldBe 80
  }

  "cache" should "return size" in {
    cache.size shouldBe 0
    cache.putAll(data)
    cache.size shouldBe 10
  }

  private def data: Map[String, List[(String, Int)]] = List
    .range(0, 10)
    .map(i => (i.toString, List((i.toString, i))))
    .toMap
}
