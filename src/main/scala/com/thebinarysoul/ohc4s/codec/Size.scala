package com.thebinarysoul.ohc4s.codec

import scala.compiletime.constValue

object Size {
  type BaseSize = 0 | 1 | 2 | 4 | 8

  inline def int[T <: BaseSize]: Int = constValue[T]
  inline def byte[T <: BaseSize]: Byte = int[T].toByte
}
