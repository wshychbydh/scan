/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eye.cool.scan.decode.source

import android.graphics.Bitmap
import android.graphics.Rect
import com.eye.cool.scan.camera.Size

internal class PlanarYUVLuminanceSource(
    yuvData: ByteArray,
    dataSize: Size,
    previewRect: Rect
) : LuminanceSource(previewRect.width(), previewRect.height()) {
  private val yuvData: ByteArray
  private val dataSize: Size
  private val previewRect: Rect
  override fun getRow(y: Int, row: ByteArray): ByteArray {
    var row: ByteArray? = row
    require(!(y < 0 || y >= height)) { "Requested row is outside the image: $y" }
    val width = width
    if (row == null || row.size < width) {
      row = ByteArray(width)
    }
    val offset: Int = (y + previewRect.top) * dataSize.width + previewRect.left
    System.arraycopy(yuvData, offset, row, 0, width)
    return row
  }

  override fun getMatrix(): ByteArray {
    val width = width
    val height = height
    if (width == dataSize.width && height == dataSize.height) {
      return yuvData
    }
    val area = width * height
    val matrix = ByteArray(area)
    var inputOffset: Int = previewRect.top * dataSize.width + previewRect.left
    if (width == dataSize.width) {
      System.arraycopy(yuvData, inputOffset, matrix, 0, area)
      return matrix
    }
    val yuv = yuvData
    for (y in 0 until height) {
      val outputOffset = y * width
      System.arraycopy(yuv, inputOffset, matrix, outputOffset, width)
      inputOffset += dataSize.width
    }
    return matrix
  }

  override fun isCropSupported(): Boolean {
    return true
  }

  /**
   * Based on the scan results, a grayscale image is generated
   */
  override fun renderCroppedGreyScaleBitmap(): Bitmap {
    val width = width
    val height = height
    val pixels = IntArray(width * height)
    val yuv = yuvData
    var inputOffset: Int = previewRect.top * dataSize.width + previewRect.left
    for (y in 0 until height) {
      val outputOffset = y * width
      for (x in 0 until width) {
        val grey: Int = yuv[inputOffset + x].toInt() and 0xff
        pixels[outputOffset + x] = -0x1000000 or grey * 0x00010101
      }
      inputOffset += dataSize.width
    }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
  }

  init {
    require(!(previewRect.left + previewRect.width() > dataSize.width
        || previewRect.top + previewRect.height() > dataSize.height)) { "Crop rectangle does not fit within image data." }
    this.yuvData = yuvData
    this.dataSize = dataSize
    this.previewRect = previewRect
  }
}