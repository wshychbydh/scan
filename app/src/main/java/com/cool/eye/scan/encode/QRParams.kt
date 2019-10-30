package com.cool.eye.scan.encode

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt

/**
 *Created by ycb on 2019/10/28 0028
 */
class QRParams(
    val content: String, //content of QRCode
    val width: Int = 500,  //width of QRCode
    val height: Int = 500, //height of QRCode
    val margin: Int = 2,  //the margin of QRCode to border
    val logo: Bitmap? = null,  //logo will be set on QRCode
    @ColorInt val qrColor: Int = Color.BLACK, //the color of qr
    @ColorInt val gapColor: Int = Color.WHITE, //the color of gap
    val savePath: String? = null,  //QRCode will be saved to
    val saveFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,  // The format of the compressed image
    val saveQuality: Int = 100  //Hint to the compressor, 0-100.
) {
  fun isValid(): Boolean {
    return content.isNotEmpty()
        && width > 0
        && height > 0
        && margin >= 0
        && (margin < width / 2)
        && margin < height / 2
  }

  class Builder(private val content: String) {
    var width: Int = 500  //width of QRCode
    var height: Int = 500 //height of QRCode
    var margin: Int = 2  //the margin of QRCode to border
    var logo: Bitmap? = null  //logo will be set on QRCode
    var savePath: String? = null  //QRCode will be saved to
    var saveFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG  // The format of the compressed image
    var quality: Int = 100  //Hint to the compressor, 0-100.
    var qrColor: Int = Color.BLACK //the color of qr
    var gapColor: Int = Color.WHITE //the color of gap

    /**
     * @param width the width of QRCode
     */
    fun setWidth(width: Int): Builder {
      this.width = width
      return this
    }

    /**
     * @param height the height of QRCode
     */
    fun setHeight(height: Int): Builder {
      this.height = height
      return this
    }

    /**
     * @param margin the margin of QRCode to border
     */
    fun setMargin(margin: Int): Builder {
      this.margin = margin
      return this
    }

    /**
     * @param logo the logo will be set on QRCode
     */
    fun setLogo(logo: Bitmap): Builder {
      this.logo = logo
      return this
    }

    /**
     * @param path the QRCode will be saved to
     */
    fun setSavePath(path: String): Builder {
      this.savePath = path
      return this
    }

    /**
     * @param format The format for the qrcode will be saved
     */
    fun setSaveFormat(format: Bitmap.CompressFormat): Builder {
      this.saveFormat = format
      return this
    }

    /**
     * @param quality The quality for the qrcode will be saved
     */
    fun setSaveQuality(quality: Int): Builder {
      this.quality = quality
      return this
    }

    /**
     * @param color the color of qrcode
     */
    fun setQrCorlor(@ColorInt color: Int): Builder {
      this.qrColor = color
      return this
    }

    /**
     * @param color the color of gap and background
     */
    fun setGapColor(@ColorInt color: Int): Builder {
      this.gapColor = color
      return this
    }

    fun build(): QRParams {
      return QRParams(
          content = content,
          width = width,
          height = height,
          margin = margin,
          logo = logo,
          savePath = savePath,
          saveFormat = saveFormat,
          saveQuality = quality,
          qrColor = qrColor,
          gapColor = gapColor
      )
    }
  }
}