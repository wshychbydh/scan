package com.cool.eye.scan.encode

import android.graphics.Bitmap
import android.graphics.Color

/**
 *Created by ycb on 2019/10/28 0028
 */
class QRParams(
    val content: String, //content of QRCode
    val width: Int = 500,  //width of QRCode
    val height: Int = 500, //height of QRCode
    val margin: Int = 2,  //the margin of QRCode to border
    val logo: Bitmap? = null,  //logo will be set on QRCode
    val savePath: String? = null,  //QRCode will be saved to
    val saveFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,  // The format of the compressed image
    val quality: Int = 100,  //Hint to the compressor, 0-100.
    val qrColor: Int = Color.BLACK, //the color of qr
    val gapColor: Int = Color.WHITE //the color of gap
) {
  fun isValid(): Boolean {
    return content.isNotEmpty()
        && width > 0
        && height > 0
        && margin >= 0
        && (margin < width / 2)
        && margin < height / 2
  }
}