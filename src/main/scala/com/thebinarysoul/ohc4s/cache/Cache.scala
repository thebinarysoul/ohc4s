package com.thebinarysoul.ohc4s.cache

import com.thebinarysoul.ohc4s.codec.{Codec, Serializer}
import com.thebinarysoul.ohc4s.util.AutoCloseableIterator
import org.caffinitas.ohc.histo.EstimatedHistogram
import org.caffinitas.ohc.{CacheLoader, CloseableIterator, DirectValueAccess, OHCache, OHCacheBuilder, OHCacheStats}

import java.io.IOException
import util.chaining.scalaUtilChainingOps
import java.nio.ByteBuffer
import java.nio.channels.{ReadableByteChannel, WritableByteChannel}
import java.util.concurrent.{ExecutionException, Future, TimeUnit, TimeoutException}

/**
 * It's just a wrapper trait for [[OHCache]]
 * You should see [[OHCache]] for detail
 * @tparam F is a wrapper for the results of the original [[OHCache]] methods
 * @tparam A is a representation of an async effect
 * @tparam K is the type of keys
 * @tparam V is the type of values
 */
trait Cache[F[_], A[_], K, V] {
  def put(key: K, value: V): F[Boolean]
  def put(key: K, value: V, expireAt: Long): F[Boolean]

  def addOrReplace(key: K, old: V, value: V): F[Boolean]
  def addOrReplace(key: K, old: V, value: V, expireAt: Long): F[Boolean]

  def putIfAbsent(key: K, value: V): F[Boolean]
  def putIfAbsent(key: K, value: V, expireAt: Long): F[Boolean]
  def putAll(map: Map[K, V]): F[Unit]

  def remove(key: K): F[Boolean]
  def removeAll(keys: Seq[K]): F[Unit]
  def clear(): F[Unit]

  def get(key: K): F[Option[V]]
  def containsKey(key: K): F[Boolean]
  def getDirect(key: K): F[Option[DirectValueAccess]]
  def getDirect(key: K, updateLRU: Boolean): F[Option[DirectValueAccess]]

  def getWithLoader(key: K, loader: CacheLoader[K, V]): F[Either[InterruptedException | ExecutionException, V]]
  def getWithLoader(
      key: K,
      loader: CacheLoader[K, V],
      timeout: Long,
      unit: TimeUnit
  ): F[Either[InterruptedException | ExecutionException | TimeoutException, V]]
  def getWithLoaderAsync(key: K, loader: CacheLoader[K, V]): F[A[V]]
  def getWithLoaderAsync(key: K, loader: CacheLoader[K, V], expireAt: Long): F[A[V]]

  def hotKeyIterator(n: Int): F[AutoCloseableIterator[K]]
  def keyIterator: F[AutoCloseableIterator[K]]
  def hotKeyBufferIterator(n: Int): F[AutoCloseableIterator[ByteBuffer]]
  def keyBufferIterator: F[AutoCloseableIterator[ByteBuffer]]

  def deserializeEntry(channel: ReadableByteChannel): F[Either[IOException, Boolean]]
  def serializeEntry(key: K, channel: WritableByteChannel): F[Either[IOException, Boolean]]
  def deserializeEntries(channel: ReadableByteChannel): F[Either[IOException, Int]]
  def serializeHotNEntries(n: Int, channel: WritableByteChannel): F[Either[IOException, Int]]
  def serializeHotNKeys(n: Int, channel: WritableByteChannel): F[Either[IOException, Int]]
  def deserializeKeys(channel: ReadableByteChannel): F[Either[IOException, AutoCloseableIterator[K]]]

  def size: F[Long]
  def hashTableSizes: F[Array[Int]]
  def perSegmentSizes: F[Array[Long]]
  def getBucketHistogram: F[EstimatedHistogram]
  def segments: F[Int]
  def capacity: F[Long]
  def memUsed: F[Long]
  def freeCapacity: F[Long]
  def loadFactor: F[Float]
  def stats: F[OHCacheStats]

  def resetStatistics(): F[Unit]
  def setCapacity(capacity: Long): F[Unit]
}

object Cache {
  def create[K, V](capacity: Long)(using Codec[K], Codec[V]): DefaultCache[K, V] = DefaultCache(CacheConf(capacity))
  def create[K, V](conf: CacheConf)(using Codec[K], Codec[V]): DefaultCache[K, V] = DefaultCache(conf)
}
