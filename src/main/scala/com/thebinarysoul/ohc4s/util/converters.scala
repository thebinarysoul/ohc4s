package com.thebinarysoul.ohc4s.util

import com.google.common.util.concurrent.ListenableFuture
import org.caffinitas.ohc.CloseableIterator

import java.util.concurrent.{CompletableFuture, ExecutionException, Executors, FutureTask, TimeUnit, Future as JFuture}
import scala.concurrent.{CanAwait, ExecutionContext, Future, Promise}
import scala.concurrent.duration.Duration
import scala.jdk.FutureConverters
import scala.util.{Failure, Success, Try}
import scala.jdk.FutureConverters.*

object converters {
  class ListenablePromise[T](jFuture: ListenableFuture[T]) extends Promise[T] {
    private val promise = Promise[T]()
    private val callback: Runnable = () =>
      try promise.success(jFuture.get)
      catch { case ex: (InterruptedException | ExecutionException) => promise.failure(ex) }

    jFuture.addListener(callback, ExecutionContext.global)

    override def future: Future[T] = promise.future
    override def isCompleted: Boolean = promise.isCompleted
    override def tryComplete(result: Try[T]): Boolean = promise.tryComplete(result)
  }

  extension[T] (jFuture: JFuture[T])
    inline def asScala: Future[T] = inline jFuture match
      case lfFuture: ListenableFuture[T] => ListenablePromise(lfFuture).future
      case _ => throw IllegalArgumentException("You should use java.util.concurrent.Future.asScala only for OHCCache API")

  extension[T] (javaIterator: CloseableIterator[T])
    def asScala: AutoCloseableIterator[T] = new AutoCloseableIterator[T] :
      override def close(): Unit = javaIterator.close()
      override def hasNext: Boolean = javaIterator.hasNext
      override def next(): T = javaIterator.next()
}

