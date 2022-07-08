package com.thebinarysoul.ohc4s.codec

import scala.compiletime.constValue

object Num {
  type BaseNum = 0 | 1 | 2 | 4 | 8

  inline def int[T <: BaseNum]: Int = constValue[T]
  inline def byte[T <: BaseNum]: Byte = int[T].toByte
}
