package com.cool.eye.scan.encode

import android.graphics.Bitmap

/**
 *Created by ycb on 2019/10/28 0028
 */
class QRParams(
    val content: String, //content of QRCode
    val width: Int,  //width of QRCode
    val height: Int, //height of QRCode
    val logo: Bitmap?,  //logo will be set on QRCode
    val bmpConfig: Bitmap.Config = Bitmap.Config.ARGB_8888,  //The bitmap config to create.
    val savePath: String?,  //QRCode will be saved to
    val format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,  // The format of the compressed image
    val quality: Int = 100  //Hint to the compressor, 0-100.
) {
  fun isValid(): Boolean {
    return content.isNotEmpty() && width > 0 && height > 0
  }
}