package com.thebinarysoul.ohc4s.codec

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import Codec.*

import java.nio.ByteBuffer
import java.time.{Instant, LocalDate}
import scala.util.Random

class CodecSpec extends AnyFlatSpec with Matchers {
  "Codec" should "derive Codec[Byte]" in {
    check(Byte.MinValue)
    check(Byte.MaxValue)
  }

  "Codec" should "derive Codec[Short]" in {
    check(Short.MinValue)
    check(Short.MaxValue)
  }

  "Codec" should "derive Codec[Int]" in {
    check(Int.MinValue)
    check(Int.MaxValue)
  }

  "Codec" should "derive Codec[Long]" in {
    check(Long.MinValue)
    check(Long.MaxValue)
  }

  "Codec" should "derive Codec[Float]" in {
    check(Float.MinValue)
    check(Float.MaxValue)
  }

  "Codec" should "derive Codec[Double]" in {
    check(Double.MinValue)
    check(Double.MaxValue)
  }

  "Codec" should "derive Codec[Boolean]" in {
    check(true)
    check(false)
  }

  "Codec" should "derive Codec[String]" in {
    check("")
    check("abc")
    check(Random.nextString(256))
  }

  "Codec" should "derive Codec[Option[T]" in {
    check(Option.empty[Byte])
    check(Option(1))
    check(Option(Option("abc")))
  }

  "Codec" should "derive Codec[List[T]" in {
    check(List.empty[Byte])
    check(List(1))
    check(List(1, 2, 3))
    check(List(List(Some(1), Some(2), None)))
  }

  "Codec" should "derive Codec[Map[K, V]]" in {
    check(Map.empty[Long, String])
    check(Map(1 -> "a"))
    check(Map(
      "a" -> Map(1 -> List(Option(1))),
      "b" -> Map.empty,
      "c" -> Map(2 -> List(None)),
      "d" -> Map(3 -> Nil)
    ))
  }

  "Codec" should "derive custom Codec[T]" in {
    given userCodec: Codec[LocalDate] = new Codec[LocalDate]:
      override def encoder: Encoder[LocalDate] = buffer => value => buffer.putLong(value.toEpochDay)
      override def decoder: Decoder[LocalDate] = buffer => LocalDate.ofEpochDay(buffer.getLong)
      override def sizeEstimator: Estimator[LocalDate] = _ => 8

    check(LocalDate.now)
  }

  "Codec" should "derive Codec[TupleN]" in {
    check(("abc", 1))
    check(true, 1.toByte, 2.toShort, 3, 4L, 5.0f, 5.0, "abc")
    check(Some(1), List("a", "b", "c"), Map.empty[String, String])
  }

  "Codec" should "derive Codec[ProductN]" in {
    case class User(name: String, age: Int)

    val user = User("Luna", 7)

    check(user)
    check(List.fill(10)(user))
  }

  private def check[T : Codec](initValue: T): Unit = {
    val codec = summon[Codec[T]]
    val size = codec.sizeEstimator(initValue)
    val initBuffer = ByteBuffer.allocate(size)
    val buffer = codec
      .encoder
      .apply(initBuffer)
      .apply(initValue)
      .flip

    val decodedValue = codec
      .decoder
      .apply(buffer)

    initValue shouldBe decodedValue
  }
}
