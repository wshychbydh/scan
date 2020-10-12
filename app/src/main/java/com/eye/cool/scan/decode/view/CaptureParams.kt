package com.eye.cool.scan.decode.view

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.eye.cool.scan.R

/**
 * Created by cool 2020/9/22 16:28
 */
class CaptureParams private constructor() {

  internal var maskColor = CaptureView.MASK_COLOR
  internal var possiblePointColor = CaptureView.POSSIBLE_POINT_COLOR
  internal var possiblePointAliveDuration = CaptureView.POSSIBLE_POINT_ALIVE_DURATION
  internal var scanDuration = CaptureView.SCAN_DURATION
  internal var frameResId = R.drawable.scan_frame_shape
  internal var frameOffset = 10
  internal var scannerResId = R.drawable.scan_scanner_shape
  internal var frameWidthRatio = 0.6f
    set(value) {
      field = when {
        value > 1.0 -> {
          1.0f
        }
        value < 0.1f -> {
          0.1f
        }
        else -> {
          value
        }
      }
    }
  internal var frameHeightRatio = 0.4f
    set(value) {
      field = when {
        value > 1.0 -> {
          1.0f
        }
        value < 0.1f -> {
          0.1f
        }
        else -> {
          value
        }
      }
    }
  internal var descOffsetXRatio = 0f
  internal var descOffsetYRatio = 0.23f
  internal var scanDesc: String? = null
  internal var descTextColor = Color.WHITE
  internal var descTextSize = 15f

  class Builder {

    private val params = CaptureParams()

    fun maskColor(maskColor: Int): Builder {
      params.maskColor = maskColor
      return this
    }

    fun possiblePointAliveDuration(possiblePointAliveDuration: Int): Builder {
      params.possiblePointAliveDuration = possiblePointAliveDuration
      return this
    }

    fun possiblePointColor(color: Int): Builder {
      params.possiblePointColor = color
      return this
    }

    fun scanDuration(duration: Int): Builder {
      params.scanDuration = duration
      return this
    }

    fun frameDrawable(@DrawableRes resId: Int): Builder {
      params.frameResId = resId
      return this
    }

    fun frameOffset(offset: Int): Builder {
      params.frameOffset = offset
      return this
    }

    fun scannerDrawableRes(@DrawableRes resId: Int): Builder {
      params.scannerResId = resId
      return this
    }

    fun scanDesc(desc: String): Builder {
      params.scanDesc = desc
      return this
    }

    fun frameWidthRatio(ratio: Float): Builder {
      params.frameWidthRatio = ratio
      return this
    }

    fun frameHeightRatio(ratio: Float): Builder {
      params.frameHeightRatio = ratio
      return this
    }

    fun descOffsetXRatio(ratio: Float): Builder {
      params.descOffsetXRatio = ratio
      return this
    }

    fun descOffsetYRatio(ratio: Float): Builder {
      params.descOffsetYRatio = ratio
      return this
    }

    fun descTextColor(@ColorInt textColor: Int): Builder {
      params.descTextColor = textColor
      return this
    }

    fun descTextSize(textSize: Float): Builder {
      params.descTextSize = textSize
      return this
    }

    fun build() = params
  }
}