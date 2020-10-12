package com.eye.cool.scan.decode.supprot

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

internal fun <T> CancellableContinuation<T>.complete(data: T) {
  if (isCompleted) return
  if (isActive) {
    resume(data)
  } else {
    cancel()
  }
}