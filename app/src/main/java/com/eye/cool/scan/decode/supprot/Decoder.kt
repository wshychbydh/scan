package com.eye.cool.scan.decode.supprot

import com.eye.cool.scan.decode.source.LuminanceSource
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.ResultPointCallback
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

/**
 *  Created by cool 2020/9/23
 */
internal object Decoder {

  suspend fun decode(
      source: LuminanceSource,
      pointCallback: ResultPointCallback
  ) = suspendCancellableCoroutine<DecodeResult?> {
    val bitmap = BinaryBitmap(HybridBinarizer(source))
    val hints = Hashtable<DecodeHintType, Any?>(3)
    hints[DecodeHintType.CHARACTER_SET] = "UTF-8"
    hints[DecodeHintType.NEED_RESULT_POINT_CALLBACK] = pointCallback
    val multiFormatReader = MultiFormatReader()
    multiFormatReader.setHints(hints)
    try {
      val result = multiFormatReader.decodeWithState(bitmap)
      val bitmap = source.renderCroppedGreyScaleBitmap()
      it.complete(DecodeResult(result, bitmap))
    } catch (e: Exception) {
      e.printStackTrace()
      it.complete(null)
    } finally {
      multiFormatReader.reset()
    }
  }

  private fun <T> CancellableContinuation<T>.complete(data: T) {
    if (isCompleted) return
    if (isActive) {
      resume(data)
    } else {
      cancel()
    }
  }
}