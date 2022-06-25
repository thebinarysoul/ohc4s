package com.thebinarysoul.ohc4s.cache

import com.thebinarysoul.ohc4s.codec.*
import com.thebinarysoul.ohc4s.codec.given
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DefaultCacheSpec extends AnyFlatSpec with Matchers {
  private val cache = Cache.default[String, List[Int]]()

  "cache" should "put (key, value)" in {
    cache.put("key", List(1, 2, 3)) shouldBe true
  }

  "cache" should "get Some(value)" in {
    cache.put("key", List(1, 2, 3)) shouldBe true
    cache.get("key") shouldBe Some(List(1, 2, 3))
  }

  "cache" should "get None" in {
    cache.get("kee2") shouldBe None
  }

  "cache" should "replace the same keys" in {
    val key = "key"
    cache.put(key, List(1))
    cache.put(key, List(2))
    cache.get(key) shouldBe Some(List(2))
  }
}
