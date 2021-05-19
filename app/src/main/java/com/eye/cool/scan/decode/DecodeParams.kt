package com.eye.cool.scan.decode

import android.view.SurfaceView
import com.eye.cool.scan.decode.listener.DecodeListener
import com.eye.cool.scan.decode.listener.PermissionChecker
import com.eye.cool.scan.decode.view.CaptureView

class DecodeParams private constructor(
    internal val surfaceView: SurfaceView?,
    internal val captureView: CaptureView?,

    internal val scaleBitmap: Boolean,
    internal val scaleFactor: Float,
    internal val scaleFilter: Boolean,

    internal val vibrator: Boolean,
    internal val playBeep: Boolean,

    internal val decodeListener: DecodeListener?,
    internal var permissionChecker: PermissionChecker?,
) {

  companion object {
    inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
  }

  fun isValid(): Boolean {
    return decodeListener != null
        && captureView != null
        && surfaceView != null
        && permissionChecker != null
  }

  data class Builder(
      var surfaceView: SurfaceView? = null,
      var captureView: CaptureView? = null,

      var scaleBitmap: Boolean = false,
      var scaleFactor: Float = 2.0f,
      var scaleFilter: Boolean = false,

      var vibrator: Boolean = true,
      var playBeep: Boolean = true,

      var decodeListener: DecodeListener? = null,
      var permissionChecker: PermissionChecker? = null,
  ) {

    fun decodeListener(listener: DecodeListener) = apply { this.decodeListener = listener }

    fun surfaceView(surfaceView: SurfaceView) = apply { this.surfaceView = surfaceView }

    fun captureView(captureView: CaptureView) = apply { this.captureView = captureView }

    /**
     * {@link Bitmap.createScaledBitmap(bmp, bmp.width/factor, bmp.height/factor, filter)}
     *
     * @param [factor] scale factor, default 2.0
     * @param [filter] default false
     */
    fun scaleBitmap(factor: Float = 2.0f, filter: Boolean = false) = apply {
      if (factor <= 1.0f)
        this.scaleFactor = factor
      this.scaleFilter = filter
      this.scaleBitmap = true
    }

    fun permissionChecker(listener: PermissionChecker) = apply { this.permissionChecker = listener }

    /**
     * <uses-permission android:name="android.permission.VIBRATE"/>
     */
    fun vibrator(vibrator: Boolean) = apply { this.vibrator = vibrator }

    fun playBeep(playBeep: Boolean) = apply { this.playBeep = playBeep }

    fun build() = DecodeParams(
        surfaceView = surfaceView,
        captureView = captureView,
        scaleBitmap = scaleBitmap,
        scaleFactor = scaleFactor,
        scaleFilter = scaleFilter,
        vibrator = vibrator,
        playBeep = playBeep,
        decodeListener = decodeListener,
        permissionChecker = permissionChecker
    )
  }
}