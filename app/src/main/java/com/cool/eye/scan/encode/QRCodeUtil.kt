package com.cool.eye.scan.encode

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
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
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

object QRCodeUtil {

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
   *  @return bitmap on UI thread
   */
  @JvmStatic
  suspend fun createQRImage(params: QRParams): Bitmap? {
    val bitmap = withContext(Dispatchers.IO) {
      createQRImageAsync(params)
    }
    return withContext(Dispatchers.Main) { bitmap }
  }

  @WorkerThread
  @JvmStatic
  fun createQRImageAsync(params: QRParams): Bitmap? {
    check(Looper.myLooper() != Looper.getMainLooper()) { "Call on sub thread or call @createQRImageReturnOnUI instead" }
    if (!params.isValid()) return null
    var bitmap: Bitmap? = null
    try {
      //配置参数
      val hints = HashMap<EncodeHintType, Any>()
      hints[EncodeHintType.CHARACTER_SET] = "utf-8"
      //容错级别
      hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
      //设置空白边距的宽度
      // hints.put(EncodeHintType.MARGIN, 2); //default is

      // 图像数据转换，使用了矩阵转换
      val bitMatrix = QRCodeWriter()
          .encode(params.content, BarcodeFormat.QR_CODE, params.width, params.height, hints)
      val pixels = IntArray(params.width * params.height)
      // 下面这里按照二维码的算法，逐个生成二维码的图片，
      // 两个for循环是图片横列扫描的结果
      for (y in 0 until params.height) {
        for (x in 0 until params.width) {
          if (bitMatrix.get(x, y)) {
            pixels[y * params.width + x] = -0x1000000
          } else {
            pixels[y * params.width + x] = -0x1
          }
        }
      }

      // 生成二维码图片的格式，使用ARGB_8888
      bitmap = Bitmap.createBitmap(params.width, params.height, params.bmpConfig)
      bitmap!!.setPixels(pixels, 0, params.width, 0, 0, params.width, params.height)

      if (params.logo != null) {
        bitmap = createLogo(bitmap, params.logo)
      }
      if (!params.savePath.isNullOrEmpty()) {
        //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
        bitmap?.compress(params.format, params.quality, FileOutputStream(params.savePath))
      }
    } catch (e: WriterException) {
      e.printStackTrace()
    } catch (e: IOException) {
      e.printStackTrace()
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
    } finally {
      return bitmap
    }
  }

  private fun createLogo(src: Bitmap?, logo: Bitmap?): Bitmap? {
    if (src == null || logo == null) return null

    //获取图片的宽高
    val srcWidth = src.width
    val srcHeight = src.height
    val logoWidth = logo.width
    val logoHeight = logo.height

    if (srcWidth == 0 || srcHeight == 0) return null

    if (logoWidth == 0 || logoHeight == 0) return src

    //logo大小为二维码整体大小的1/5
    val scaleFactor = srcWidth * 1.0f / 5f / logoWidth.toFloat()
    var bitmap: Bitmap? = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
    try {
      val canvas = Canvas(bitmap!!)
      canvas.drawBitmap(src, 0f, 0f, null)
      canvas.scale(scaleFactor, scaleFactor, srcWidth / 2f, srcHeight / 2f)
      canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2f, (srcHeight - logoHeight) / 2f, null)

      canvas.save()
      canvas.restore()
    } catch (e: Exception) {
      bitmap = null
      e.stackTrace
    }

    return bitmap
  }
}