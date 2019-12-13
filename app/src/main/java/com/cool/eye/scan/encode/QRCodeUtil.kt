package com.cool.eye.scan.encode

import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.Camera
import android.os.Looper
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

object QRCodeUtil {

  /**
   * Check camera is available below 6.0
   */
  @JvmStatic
  internal fun isCameraAvailable(): Boolean {
    var camera: Camera? = null
    return try {
      camera = Camera.open()
      // setParameters is Used for MeiZu MX5.
      camera!!.parameters = camera!!.parameters
      true
    } catch (e: Exception) {
      Log.e("scan", e.message)
      false
    } finally {
      try {
        camera?.release()
      } catch (ignore: Exception) {
      }
    }
  }

  /**
   * @param params configs of QRCode
   * @return bitmap on UI thread
   */
  @JvmStatic
  fun createQRImage(params: QRParams, callback: ((Bitmap?) -> Unit)) {
    GlobalScope.launch {
      val bitmap = withContext(Dispatchers.IO) {
        createQRImageAsync(params)
      }
      withContext(Dispatchers.Main) {
        callback.invoke(bitmap)
      }
    }
  }

  /**
   *  @param params configs of QRCode
   *  @return bitmap on IO thread
   */
  @JvmStatic
  suspend fun createQRImage(params: QRParams): Bitmap? {
    return withContext(Dispatchers.IO) {
      createQRImageAsync(params)
    }
  }

  /**
   *  @param params configs of QRCode
   *  @return bitmap on calling thread
   */
  @JvmStatic
  @WorkerThread
  fun createQRImageAsync(params: QRParams): Bitmap? {

    check(Looper.myLooper() != Looper.getMainLooper()) { "You must be call this on sub thread" }

    if (!params.isValid()) return null

    try {
      val hints = HashMap<EncodeHintType, Any>()
      hints[EncodeHintType.CHARACTER_SET] = "utf-8"
      hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
      hints[EncodeHintType.MARGIN] = params.margin
      val widthPix = params.width
      val heightPix = params.height
      val bitMatrix = QRCodeWriter().encode(
          params.content,
          BarcodeFormat.QR_CODE,
          widthPix,
          heightPix,
          hints
      )
      val pixels = IntArray(widthPix * heightPix)
      for (y in 0 until heightPix) {
        for (x in 0 until widthPix) {
          if (bitMatrix.get(x, y)) {
            pixels[y * widthPix + x] = params.qrColor
          } else {
            pixels[y * widthPix + x] = params.gapColor
          }
        }
      }

      var bitmap: Bitmap? = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888)
      bitmap!!.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix)

      if (params.logo != null) {
        bitmap = addLogo(bitmap, params.logo)
      }

      //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
      if (bitmap != null && params.savePath != null) {
        bitmap.compress(
            params.saveFormat,
            params.saveQuality,
            FileOutputStream(params.savePath)
        )
      }
      return bitmap
    } catch (e: WriterException) {
      e.printStackTrace()
    } catch (e: IOException) {
      e.printStackTrace()
    }
    return null
  }

  private fun addLogo(src: Bitmap?, logo: Bitmap?): Bitmap? {
    if (src == null) {
      return null
    }

    if (logo == null) {
      return src
    }

    val srcWidth = src.width
    val srcHeight = src.height
    val logoWidth = logo.width
    val logoHeight = logo.height

    if (srcWidth == 0 || srcHeight == 0) {
      return null
    }

    if (logoWidth == 0 || logoHeight == 0) {
      return src
    }

    val scaleFactor = srcWidth * 1.0f / 5f / logoWidth.toFloat()
    var bitmap: Bitmap? = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
    try {
      val canvas = Canvas(bitmap!!)
      canvas.drawBitmap(src, 0f, 0f, null)
      canvas.scale(
          scaleFactor,
          scaleFactor,
          srcWidth / 2f,
          srcHeight / 2f
      )
      canvas.drawBitmap(
          logo,
          (srcWidth - logoWidth) / 2f,
          (srcHeight - logoHeight) / 2f,
          null
      )

      canvas.save()
      canvas.restore()
    } catch (e: Exception) {
      bitmap = null
      e.stackTrace
    }

    return bitmap
  }
}