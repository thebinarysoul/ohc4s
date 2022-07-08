package com.thebinarysoul.ohc4s.util

import org.caffinitas.ohc.CloseableIterator
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.function.Consumer
import scala.util.Using
import converters.*

import java.util.concurrent.{ExecutionException, Future}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ConvertersSpec extends AnyFlatSpec with Matchers {
  "CloseableIterator.asScala" should "convert CloseableIterator to AutoCloseableIterator" in {
    class CloseableIteratorException extends RuntimeException

    val closeableIterator = new CloseableIterator[Int] {
      private var int = 0

      override def next(): Int =
        if int >= 0
        then
          int += 1
          int
        else throw new CloseableIteratorException

      override def hasNext: Boolean = true
      override def close(): Unit = int = -1
    }

    Using.resource(closeableIterator.asScala) { iterator =>
      iterator.next() shouldBe 1
      iterator.next() shouldBe 2
    }

    assertThrows[CloseableIteratorException](closeableIterator.next())
  }

  "Future.asScala" should "convert ListenableFuture to scala.concurrent.Future(success)" in {
    val future = com.google.common.util.concurrent.Futures.immediateFuture("value")
    val scalaFuture = future.asScala
    Await.result(scalaFuture, Duration.Inf) shouldBe "value"
  }

  "Future.asScala" should "convert ListenableFuture to scala.concurrent.Future(failure)" in {
    val future = com.google.common.util.concurrent.Futures.immediateFailedFuture(new RuntimeException("error"))
    val scalaFuture = future.asScala
    assertThrows[ExecutionException](Await.result(scalaFuture, Duration.Inf))
  }
}
