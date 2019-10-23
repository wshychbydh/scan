package com.cool.eye.scan.listener

import android.graphics.Bitmap

interface CaptureListener {
  /**
   * Call when preview succeed
   */
  fun onPreviewSucceed() {}

  /**
   * Call when scan succeed with bitmap and content
   * @param bitmap
   * @param content
   */
  fun onScanSucceed(bitmap: Bitmap, content: String)

  fun onScanFailed(throwable: Throwable)
}