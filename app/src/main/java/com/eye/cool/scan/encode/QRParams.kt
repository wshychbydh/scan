package com.eye.cool.scan.encode

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt

/**
 *Created by ycb on 2019/10/28 0028
 */
class QRParams(
    internal val content: String, //content of QRCode
    internal var width: Int = 500,  //width of QRCode
    internal var height: Int = 500, //height of QRCode
    internal var margin: Int = 2,  //the margin of QRCode to border
    internal var logo: Bitmap? = null,  //logo will be set on QRCode
    internal var qrColor: Int = Color.BLACK, //the color of qr
    internal var gapColor: Int = Color.WHITE, //the color of gap
    internal var savePath: String? = null,  //QRCode will be saved to
    internal var saveFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,  // The format of the compressed image
    internal var bitmapConfig: Bitmap.Config = Bitmap.Config.RGB_565,  // The config of bitmap
    internal var saveQuality: Int = 100,  //Hint to the compressor, 0-100.
    internal var logoScale: Float = 5f
) {
  fun isValid(): Boolean {
    return content.isNotEmpty()
        && width > 0
        && height > 0
        && margin >= 0
        && (margin < width / 2)
        && margin < height / 2
  }

  class Builder(content: String) {
    private val params = QRParams(content)

    /**
     * @param width the width of QRCode, default 500
     * @param height the height of QRCode, default 500
     */
    fun setSize(width: Int, height: Int): Builder {
      params.width = width
      params.height = height
      return this
    }

    /**
     * @param margin the margin of QRCode to border, default 2
     */
    fun setMargin(margin: Int): Builder {
      params.margin = margin
      return this
    }

    /**
     * @param logo the logo will be set on QRCode
     */
    fun setLogo(logo: Bitmap): Builder {
      params.logo = logo
      return this
    }

    /**
     * You should be check write permission
     * @param path the QRCode will be saved to
     */
    fun setSavePath(path: String): Builder {
      params.savePath = path
      return this
    }

    /**
     * @param format The format for the qrcode will be saved, default PNG
     */
    fun setSaveFormat(format: Bitmap.CompressFormat): Builder {
      params.saveFormat = format
      return this
    }

    /**
     * @param format The format for the qrcode will be saved, default RGB_565
     */
    fun setBitmapConfig(config: Bitmap.Config): Builder {
      params.bitmapConfig = config
      return this
    }

    /**
     * @param quality The quality for the qrcode will be saved, default 100
     */
    fun setSaveQuality(quality: Int): Builder {
      params.saveQuality = quality
      return this
    }

    /**
     * @param color the color of qrcode, default Color.BLACK
     */
    fun setQrColor(@ColorInt color: Int): Builder {
      params.qrColor = color
      return this
    }

    /**
     * @param color the color of gap and background, default Color.WHITE
     */
    fun setGapColor(@ColorInt color: Int): Builder {
      params.gapColor = color
      return this
    }

    /**
     * ratio = bitmap.width / logo.width / scale
     * @param scale The scale of the logo to the QR code, default 5.0
     */
    fun setLogoScale(scale: Float): Builder {
      params.logoScale = scale
      return this
    }

    fun build(): QRParams = params
  }
}