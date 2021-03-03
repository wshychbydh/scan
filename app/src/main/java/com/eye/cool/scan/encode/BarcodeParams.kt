package com.eye.cool.scan.encode

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt

class BarcodeParams(
    internal var width: Int = 480,  //width of barcode
    internal var height: Int = 280, //height of barcode
    internal var margin: Int = 10,  //the margin of barcode to border
    internal var qrColor: Int = Color.BLACK, //the color of qr
    internal var gapColor: Int = Color.WHITE, //the color of gap
    internal var bitmapConfig: Bitmap.Config = Bitmap.Config.RGB_565,  // The config of bitmap
    internal var savePath: String? = null,  //barcode will be saved to
    internal var saveFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,  // The format of the compressed image
    internal var saveQuality: Int = 100,  //Hint to the compressor, 0-100.
    internal var showText: Boolean = true,
    internal var textSize: Float = 38f,
    internal var textColor: Int = Color.BLACK,
    internal var textPadding: Float = 2f,
) {

  fun getTotalHeight(): Int {
    return if (showText) {
      height + (textSize + textPadding).toInt()
    } else {
      height
    }
  }

  class Builder() {
    private val params = BarcodeParams()

    /**
     * The width will be adjusted according to the length of the content, taking the maximum value
     *
     * [width] the width of barcode, default 480
     */
    fun width(width: Int): Builder {
      params.width = width
      return this
    }

    /**
     * [height] the height of barcode, default 280
     */
    fun height(height: Int): Builder {
      params.height = height
      return this
    }

    /**
     * [margin] the margin of barcode to border, default 10
     */
    fun margin(margin: Int): Builder {
      params.margin = margin
      return this
    }

    /**
     * [color] the color of barcode, default Color.BLACK
     */
    fun barcodeColor(@ColorInt color: Int): Builder {
      params.qrColor = color
      return this
    }

    /**
     * [color] the color of gap and background, default Color.WHITE
     */
    fun gapColor(@ColorInt color: Int): Builder {
      params.gapColor = color
      return this
    }

    /**
     * [config] The format for the qrcode will be saved, default RGB_565
     */
    fun bitmapConfig(config: Bitmap.Config): Builder {
      params.bitmapConfig = config
      return this
    }

    /**
     * [shown] show content blow barcode, default false
     */
    fun showContent(shown: Boolean): Builder {
      params.showText = shown
      return this
    }

    /**
     * [textSize] the textSize of content, default 38
     */
    fun textSize(textSize: Float): Builder {
      params.textSize = textSize
      return this
    }

    /**
     * [padding] the padding between content and barcode, default 2
     */
    fun textPadding(padding: Float): Builder {
      params.textPadding = padding
      return this
    }

    /**
     * [color] the color of barcode, default Color.BLACK
     */
    fun textColor(@ColorInt color: Int): Builder {
      params.textColor = color
      return this
    }

    /**
     * You should be check permission of
     * android.Manifest.permission.WRITE_EXTERNAL_STORAGE
     * [path] the QRCode will be saved to
     */
    fun savePath(path: String): Builder {
      params.savePath = path
      return this
    }

    /**
     * [format] The format for the barcode will be saved, default PNG
     */
    fun saveFormat(format: Bitmap.CompressFormat): Builder {
      params.saveFormat = format
      return this
    }

    /**
     * [quality] The quality for the barcode will be saved, default 100
     */
    fun saveQuality(quality: Int): Builder {
      params.saveQuality = quality
      return this
    }

    fun build(): BarcodeParams = params
  }
}