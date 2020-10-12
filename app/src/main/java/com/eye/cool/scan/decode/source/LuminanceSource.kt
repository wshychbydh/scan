package com.eye.cool.scan.decode.source

import android.graphics.Bitmap
import com.google.zxing.LuminanceSource

internal abstract class LuminanceSource protected constructor(
    width: Int,
    height: Int
) : LuminanceSource(width, height) {
  abstract fun renderCroppedGreyScaleBitmap(): Bitmap?
}