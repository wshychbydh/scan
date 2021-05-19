package com.eye.cool.scan.encode

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt

class BarcodeParams private constructor(
    internal val width: Int,  //width of barcode
    internal val height: Int, //height of barcode
    internal val margin: Int,  //the margin of barcode to border
    internal val qrColor: Int, //the color of qr
    internal val gapColor: Int, //the color of gap
    internal val bitmapConfig: Bitmap.Config,  // The config of bitmap
    internal val savePath: String?,  //barcode will be saved to
    internal val saveFormat: Bitmap.CompressFormat,  // The format of the compressed image
    internal val saveQuality: Int,  //Hint to the compressor, 0-100.
    internal val showText: Boolean,
    internal val textSize: Float,
    internal val textColor: Int,
    internal val textPadding: Float,
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

  fun getTotalHeight(): Int {
    return if (showText) {
      height + (textSize + textPadding).toInt()
    } else {
      height
    }
  }

  data class Builder(
      var width: Int = 480,  //width of barcode
      var height: Int = 280, //height of barcode
      var margin: Int = 10,  //the margin of barcode to border
      var qrColor: Int = Color.BLACK, //the color of qr
      var gapColor: Int = Color.WHITE, //the color of gap
      var bitmapConfig: Bitmap.Config = Bitmap.Config.RGB_565,  // The config of bitmap
      var savePath: String? = null,  //barcode will be saved to
      var saveFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,  // The format of the compressed image
      var saveQuality: Int = 100,  //Hint to the compressor, 0-100.
      var showText: Boolean = false,
      var textSize: Float = 38f,
      var textColor: Int = Color.BLACK,
      var textPadding: Float = 2f,
  ) {

    /**
     * The size will be adjusted according to the length of the content, taking the maximum value
     *
     * [width] the width of barcode, default 480
     * [height] the height of barcode, default 280
     */
    fun size(width: Int, height: Int) = apply {
      this.width = width
      this.height = height
    }

    /**
     * [margin] the margin of barcode to border, default 10
     */
    fun margin(margin: Int) = apply { this.margin = margin }

    /**
     * [color] the color of barcode, default Color.BLACK
     */
    fun barcodeColor(@ColorInt color: Int) = apply { this.qrColor = color }

    /**
     * [color] the color of gap and background, default Color.WHITE
     */
    fun gapColor(@ColorInt color: Int) = apply { this.gapColor = color }

    /**
     * [config] The format for the qrcode will be saved, default RGB_565
     */
    fun bitmapConfig(config: Bitmap.Config) = apply { this.bitmapConfig = config }

    /**
     * [shown] show content blow barcode, default false
     */
    fun showContent(shown: Boolean) = apply { this.showText = shown }

    /**
     * [textSize] the textSize of content, default 38
     */
    fun textSize(textSize: Float) = apply { this.textSize = textSize }

    /**
     * [padding] the padding between content and barcode, default 2
     */
    fun textPadding(padding: Float) = apply { this.textPadding = padding }

    /**
     * [color] the color of barcode, default Color.BLACK
     */
    fun textColor(@ColorInt color: Int) = apply { this.textColor = color }

    /**
     * You should be check permission of
     * android.Manifest.permission.WRITE_EXTERNAL_STORAGE
     * [path] the QRCode will be saved to
     */
    fun savePath(path: String) = apply { this.savePath = path }

    /**
     * [format] The format for the barcode will be saved, default PNG
     */
    fun saveFormat(format: Bitmap.CompressFormat) = apply { this.saveFormat = format }

    /**
     * [quality] The quality for the barcode will be saved, default 100
     */
    fun saveQuality(quality: Int) = apply { this.saveQuality = quality }

    fun build() = BarcodeParams(
        width = width,
        height = height,
        margin = margin,
        qrColor = qrColor,
        gapColor = gapColor,
        bitmapConfig = bitmapConfig,
        savePath = savePath,
        saveFormat = saveFormat,
        saveQuality = saveQuality,
        showText = showText,
        textSize = textSize,
        textColor = textColor,
        textPadding = textPadding
    )
  }
}