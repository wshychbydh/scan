package com.eye.cool.scan.decode

import android.view.SurfaceView
import com.eye.cool.scan.decode.listener.DecodeListener
import com.eye.cool.scan.decode.listener.PermissionChecker
import com.eye.cool.scan.decode.view.CaptureView

class DecodeParams private constructor() {

  internal var decodeListener: DecodeListener? = null
  internal var permissionChecker: PermissionChecker? = null
  internal var surfaceView: SurfaceView? = null
  internal var captureView: CaptureView? = null

  internal var scaleBitmap = false
  internal var scaleFactor = 2.0f
  internal var scaleFilter = false

  internal var vibrator = true
  internal var playBeep = true

  fun isValid(): Boolean {
    return decodeListener != null
        && captureView != null
        && surfaceView != null
        && permissionChecker != null
  }

  class Builder {

    private val params = DecodeParams()

    fun decodeListener(listener: DecodeListener): Builder {
      params.decodeListener = listener
      return this
    }

    fun surfaceView(surfaceView: SurfaceView): Builder {
      params.surfaceView = surfaceView
      return this
    }

    fun captureView(captureView: CaptureView): Builder {
      params.captureView = captureView
      return this
    }

    /**
     * {@link Bitmap.createScaledBitmap(bmp, bmp.width/factor, bmp.height/factor, filter)}
     *
     * @param factor scale factor, default 2.0
     * @param filter default false
     */
    fun scaleBitmap(factor: Float = 2.0f, filter: Boolean = false): Builder {
      if (factor <= 1.0f) return this
      params.scaleFactor = factor
      params.scaleFilter = filter
      params.scaleBitmap = true
      return this
    }

    fun permissionChecker(listener: PermissionChecker): Builder {
      params.permissionChecker = listener
      return this
    }

    /**
     * <uses-permission android:name="android.permission.VIBRATE"/>
     */
    fun vibrator(vibrator: Boolean): Builder {
      params.vibrator = vibrator
      return this
    }

    fun playBeep(playBeep: Boolean): Builder {
      params.playBeep = playBeep
      return this
    }

    fun build() = params
  }
}