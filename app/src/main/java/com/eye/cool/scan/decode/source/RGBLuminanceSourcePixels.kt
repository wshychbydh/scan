package com.eye.cool.scan.decode.source

import android.graphics.Bitmap
import com.eye.cool.scan.camera.Size

internal class RGBLuminanceSourcePixels(
    rgbPixels: IntArray,
    imageSize: Size
) : LuminanceSource(imageSize.width, imageSize.height) {
  private val luminances: ByteArray
  override fun getMatrix(): ByteArray {
    return luminances
  }

  override fun getRow(y: Int, row: ByteArray): ByteArray {
    var row: ByteArray? = row
    require(!(y < 0 || y >= height)) { "Requested row is outside the image: $y" }
    val width = width
    if (row == null || row.size < width) {
      row = ByteArray(width)
    }
    System.arraycopy(luminances, y * width, row, 0, width)
    return row
  }

  override fun renderCroppedGreyScaleBitmap(): Bitmap {
    val width = width
    val height = height
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
      for (x in 0 until width) {
        val grey: Int = luminances[y * width + x].toInt() and 0xff
        pixels[y * width + x] = -0x1000000 or grey * 0x00010101
      }
    }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
  }

  init {
    val width = imageSize.width
    val height = imageSize.height
    luminances = ByteArray(width * height)
    for (y in 0 until height) {
      val offset = y * width
      for (x in 0 until width) {
        val pixel = rgbPixels[offset + x]
        val r = pixel shr 16 and 0xff
        val g = pixel shr 8 and 0xff
        val b = pixel and 0xff
        if (r == g && g == b) {
          // Image is already greyscale, so pick any channel.
          luminances[offset + x] = r.toByte()
        } else {
          // Calculate luminance cheaply, favoring green.
          luminances[offset + x] = (r + g + g + b shr 2).toByte()
        }
      }
    }
  }
}