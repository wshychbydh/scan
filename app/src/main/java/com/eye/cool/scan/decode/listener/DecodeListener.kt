package com.eye.cool.scan.decode.listener

import android.graphics.Bitmap
import com.eye.cool.scan.decode.supprot.DecodeException

interface DecodeListener {

  /**
   * UI Thread
   *
   * Call when preview succeed
   */
  fun onPreviewSucceed() {}

  /**
   * UI Thread
   *
   * Call when scan succeed with bitmap and content
   * @param [bitmap]
   * @param [content]
   */
  fun onScanSucceed(bitmap: Bitmap, content: String)

  /**
   * UI Thread
   *
   * @param [error]
   */
  fun onScanFailed(error: DecodeException)
}