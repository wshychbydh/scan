package com.eye.cool.scan.encode

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt

/**
 *Created by ycb on 2019/10/28 0028
 */
class QRParams private constructor(
    internal val width: Int,  //width of QRCode
    internal val height: Int, //height of QRCode
    internal val margin: Int,  //the margin of QRCode to border
    internal val logo: Bitmap?,  //logo will be set on QRCode
    internal val qrColor: Int, //the color of qr
    internal val gapColor: Int, //the color of gap
    internal val savePath: String?,  //QRCode will be saved to
    internal val saveFormat: Bitmap.CompressFormat,  // The format of the compressed image
    internal val bitmapConfig: Bitmap.Config,  // The config of bitmap
    internal val saveQuality: Int,  //Hint to the compressor, 0-100.
    internal val logoScale: Float
) {

  companion object {
    inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
  }

  fun isValid(): Boolean {
    return width > 0
        && height > 0
        && margin >= 0
        && (margin < width / 2)
        && margin < height / 2
  }

  data class Builder(
      var width: Int = 500,  //width of QRCode
      var height: Int = 500, //height of QRCode
      var margin: Int = 2,  //the margin of QRCode to border
      var logo: Bitmap? = null,  //logo will be set on QRCode
      var qrColor: Int = Color.BLACK, //the color of qr
      var gapColor: Int = Color.WHITE, //the color of gap
      var savePath: String? = null,  //QRCode will be saved to
      var saveFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,  // The format of the compressed image
      var bitmapConfig: Bitmap.Config = Bitmap.Config.RGB_565,  // The config of bitmap
      var saveQuality: Int = 100,  //Hint to the compressor, 0-100.
      var logoScale: Float = 5f
  ) {

    /**
     * @param [width] the width of QRCode, default 500
     * @param [height] the height of QRCode, default 500
     */
    fun size(width: Int, height: Int) = apply {
      this.width = width
      this.height = height
    }

    /**
     * @param [margin] the margin of QRCode to border, default 2
     */
    fun margin(margin: Int) = apply { this.margin = margin }

    /**
     * @param [logo] the logo will be set on QRCode
     */
    fun logo(logo: Bitmap) = apply { this.logo = logo }

    /**
     * You may need to check permissions of
     * android.Manifest.permission.WRITE_EXTERNAL_STORAGE
     * android.Manifest.permission.WRITE_EXTERNAL_STORAGE
     * or android.permission.MANAGE_EXTERNAL_STORAGE
     *
     * @param [path] the QRCode will be saved to
     */
    fun savePath(path: String) = apply { this.savePath = path }

    /**
     * @param [format] The format for the qrcode will be saved, default PNG
     */
    fun saveFormat(format: Bitmap.CompressFormat) = apply { this.saveFormat = format }

    /**
     * @param [format] The format for the qrcode will be saved, default RGB_565
     */
    fun bitmapConfig(config: Bitmap.Config) = apply { this.bitmapConfig = config }

    /**
     * @param [quality] The quality for the qrcode will be saved, default 100
     */
    fun saveQuality(quality: Int) = apply { this.saveQuality = quality }

    /**
     * @param [color] the color of qrcode, default Color.BLACK
     */
    fun qrColor(@ColorInt color: Int) = apply { this.qrColor = color }

    /**
     * @param [color] the color of gap and background, default Color.WHITE
     */
    fun gapColor(@ColorInt color: Int) = apply { this.gapColor = color }

    /**
     * ratio = bitmap.width / logo.width / [scale]
     * @param [scale] The scale of the logo to the QR code, default 5.0
     */
    fun logoScale(scale: Float) = apply { this.logoScale = scale }

    fun build() = QRParams(
        width = width,
        height = height,
        margin = margin,
        logo = logo,
        qrColor = qrColor,
        gapColor = gapColor,
        savePath = savePath,
        saveFormat = saveFormat,
        bitmapConfig = bitmapConfig,
        saveQuality = saveQuality,
        logoScale = logoScale
    )
  }
}