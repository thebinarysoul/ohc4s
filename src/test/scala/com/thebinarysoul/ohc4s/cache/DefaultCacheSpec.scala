package com.thebinarysoul.ohc4s.cache

import com.thebinarysoul.ohc4s.codec.*
import com.thebinarysoul.ohc4s.codec.given
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DefaultCacheSpec extends AnyFlatSpec with Matchers {
  private val cache = Cache.create[String, List[(String, Int)]](64 * 1024 * 1024)

  "cache" should "put (key, value)" in {
    cache.put("key", List(("a", 1), ("b", 2))) shouldBe true
  }

  "cache" should "get Some(value)" in {
    cache.put("key", List(("a", 1), ("b", 2))) shouldBe true
    cache.get("key") shouldBe Some(List(("a", 1), ("b", 2)))
  }

  "cache" should "get None" in {
    cache.get("key2") shouldBe None
  }

  "cache" should "replace the same keys" in {
    val key = "key"
    cache.put(key, List(("a", 1)))
    cache.put(key, List(("b", 2)))
    cache.get(key) shouldBe Some(List(("b", 2)))
  }
}
