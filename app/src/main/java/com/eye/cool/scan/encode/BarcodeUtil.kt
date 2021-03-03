package com.eye.cool.scan.encode

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.FileOutputStream
import java.util.*

object BarcodeUtil {

  fun create(
      content: String?,
      params: BarcodeParams = BarcodeParams.Builder().build()
  ): Bitmap? {
    if (content.isNullOrEmpty()) return null
    return try {
      val hints = HashMap<EncodeHintType, Any>()
      hints[EncodeHintType.CHARACTER_SET] = "utf-8"
      hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
      hints[EncodeHintType.MARGIN] = params.margin

      val result: BitMatrix = try {
        MultiFormatWriter().encode(
            content,
            BarcodeFormat.CODE_128,
            params.width,
            params.height,
            hints
        )
      } catch (iae: IllegalArgumentException) {
        return null
      }
      val width: Int = result.width
      val height: Int = result.height
      val pixels = IntArray(width * height)
      for (y in 0 until height) {
        val offset = y * width
        for (x in 0 until width) {
          pixels[offset + x] = if (result.get(x, y)) params.qrColor else params.gapColor
        }
      }
      val qrBitmap: Bitmap = Bitmap.createBitmap(width, height, params.bitmapConfig)
      qrBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
      if (!params.showText) return qrBitmap
      val bitmap = drawContent(qrBitmap, content, width, height, params)
      if (bitmap != null && params.savePath != null) {
        bitmap.compress(
            params.saveFormat,
            params.saveQuality,
            FileOutputStream(params.savePath)
        )
      }
      bitmap
    } catch (e: Exception) {
      null
    }
  }

  private fun drawContent(
      barcodeBmp: Bitmap,
      content: String,
      width: Int,
      height: Int,
      params: BarcodeParams
  ): Bitmap {
    val bitmap: Bitmap = Bitmap.createBitmap(width, params.getTotalHeight(), params.bitmapConfig)
    val canvas = Canvas(bitmap)
    canvas.drawColor(params.gapColor)
    val srcRect = Rect(0, 0, width, height)
    val dstRect = Rect(0, 0, width, height)
    canvas.drawBitmap(barcodeBmp, srcRect, dstRect, null)
    val p = Paint()
    p.color = params.textColor
    p.isFilterBitmap = true
    p.textSize = params.textSize
    p.textAlign = Paint.Align.CENTER
    canvas.translate(width / 2f, params.textSize)
    val textOffset = height + params.textPadding
    canvas.drawText(content, 0, content.length, 0f, textOffset, p)
    return bitmap
  }
}