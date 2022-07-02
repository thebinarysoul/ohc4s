package com.thebinarysoul.ohc4s.cache

import com.thebinarysoul.ohc4s.codec.{Codec, Serializer}
import com.thebinarysoul.ohc4s.util.AutoCloseableIterator
import org.caffinitas.ohc.histo.EstimatedHistogram
import org.caffinitas.ohc.{CacheLoader, CloseableIterator, DirectValueAccess, OHCache, OHCacheBuilder, OHCacheStats}

import java.io.IOException
import util.chaining.scalaUtilChainingOps
import java.nio.ByteBuffer
import java.nio.channels.{ReadableByteChannel, WritableByteChannel}
import java.util.concurrent.{ExecutionException, TimeUnit, TimeoutException}
import scala.concurrent.Future
import scala.util.Try
import scala.jdk.FutureConverters.FutureOps
import scala.jdk.CollectionConverters.IterableHasAsJava
import com.thebinarysoul.ohc4s.util.converters.*
import concurrent.ExecutionContext.Implicits.global

private[cache] class DefaultCache[K, V](conf: CacheConf)(using Codec[K], Codec[V]) extends BaseCache[[T] =>> T, Future, K, V](conf) {
  override def put(key: K, value: V): Boolean = cache.put(key, value)

  override def put(key: K, value: V, expireAt: Long): Boolean = cache.put(key, value, expireAt)

  override def putIfAbsent(key: K, value: V): Boolean = cache.putIfAbsent(key, value)

  override def putIfAbsent(key: K, value: V, expireAt: Long): Boolean = cache.putIfAbsent(key, value, expireAt)

  override def putAll(map: Map[K, V]): Unit = map.foreach { case (key, value) => put(key, value) }

  override def addOrReplace(key: K, old: V, value: V): Boolean = cache.addOrReplace(key, old, value)

  override def addOrReplace(key: K, old: V, value: V, expireAt: Long): Boolean = cache.addOrReplace(key, old, value, expireAt)

  override def remove(key: K): Boolean = cache.remove(key)

  override def removeAll(keys: Seq[K]): Unit = cache.removeAll(keys.asJava)

  override def clear(): Unit = cache.clear()

  override def get(key: K): Option[V] = Option(cache.get(key))

  override def containsKey(key: K): Boolean = cache.containsKey(key)

  override def getDirect(key: K): Option[DirectValueAccess] = Option(cache.getDirect(key))

  override def getDirect(key: K, updateLRU: Boolean): Option[DirectValueAccess] = Option(cache.getDirect(key, updateLRU))

  private def safe[T, E <: Throwable](action: => T): Either[E, T] =
    try Right(action)
    catch {
      case ex: E => Left(ex)
    }

  override def getWithLoader(key: K, loader: CacheLoader[K, V]): Either[InterruptedException | ExecutionException, V] =
    safe(cache.getWithLoader(key, loader))

  override def getWithLoader(key: K, loader: CacheLoader[K, V], timeout: Long, unit: TimeUnit): Either[InterruptedException | ExecutionException | TimeoutException, V] =
    safe(cache.getWithLoader(key, loader, timeout, unit))

  override def getWithLoaderAsync(key: K, loader: CacheLoader[K, V]): Future[V] =
    cache.getWithLoaderAsync(key, loader).asScala

  override def getWithLoaderAsync(key: K, loader: CacheLoader[K, V], expireAt: Long): Future[V] =
    cache.getWithLoaderAsync(key, loader, expireAt).asScala

  override def hotKeyIterator(n: Int): AutoCloseableIterator[K] =
    cache.hotKeyIterator(n).asScala

  override def keyIterator: AutoCloseableIterator[K] =
    cache.keyIterator().asScala

  override def hotKeyBufferIterator(n: Int): AutoCloseableIterator[ByteBuffer] =
    cache.hotKeyBufferIterator(n).asScala

  override def keyBufferIterator: AutoCloseableIterator[ByteBuffer] =
    cache.keyBufferIterator().asScala

  override def deserializeEntry(channel: ReadableByteChannel): Either[IOException, Boolean] =
    safe(cache.deserializeEntry(channel))

  override def serializeEntry(key: K, channel: WritableByteChannel): Either[IOException, Boolean] =
    safe(cache.serializeEntry(key, channel))

  override def deserializeEntries(channel: ReadableByteChannel): Either[IOException, Int] =
    safe(cache.deserializeEntries(channel))

  override def serializeHotNEntries(n: Int, channel: WritableByteChannel): Either[IOException, Int] =
    safe(cache.serializeHotNEntries(n, channel))

  override def serializeHotNKeys(n: Int, channel: WritableByteChannel): Either[IOException, Int] =
    safe(cache.serializeHotNKeys(n, channel))

  override def deserializeKeys(channel: ReadableByteChannel): Either[IOException, AutoCloseableIterator[K]] =
    safe(cache.deserializeKeys(channel).asScala)

  override def size: Long = cache.size

  override def hashTableSizes: Array[Int] = cache.hashTableSizes

  override def perSegmentSizes: Array[Long] = cache.perSegmentSizes

  override def getBucketHistogram: EstimatedHistogram = cache.getBucketHistogram

  override def segments: Int = cache.segments

  override def capacity: Long = cache.capacity

  override def memUsed: Long = cache.memUsed

  override def freeCapacity: Long = cache.freeCapacity

  override def loadFactor: Float = cache.loadFactor

  override def stats: OHCacheStats = cache.stats

  override def resetStatistics(): Unit = cache.resetStatistics()

  override def setCapacity(capacity: Long): Unit = cache.setCapacity(capacity)
}

