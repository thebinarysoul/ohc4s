package com.thebinarysoul.ohc4s.util

import org.caffinitas.ohc.CloseableIterator

trait AutoCloseableIterator[+T] extends Iterator[T] with AutoCloseable