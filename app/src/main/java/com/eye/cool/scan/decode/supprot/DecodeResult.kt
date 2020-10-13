package com.eye.cool.scan.decode.supprot

import android.graphics.Bitmap
import com.google.zxing.Result

/**
 * Created by cool 2020/9/23
 */
internal data class DecodeResult(
    val result: Result? = null,
    val bitmap: Bitmap? = null
) {
  fun isValid(): Boolean {
    return !result?.text.isNullOrEmpty() && bitmap != null
  }
}