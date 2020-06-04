package com.cool.eye.scan

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.os.Vibrator
import android.view.SurfaceHolder
import androidx.annotation.RequiresPermission
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cool.eye.scan.camera.CameraManager
import com.cool.eye.scan.camera.PreviewFrameShotListener
import com.cool.eye.scan.camera.Size
import com.cool.eye.scan.decode.*
import com.cool.eye.scan.listener.CaptureParams
import com.cool.eye.scan.listener.PermissionListener
import com.cool.eye.scan.util.BeepManager
import com.cool.eye.scan.util.DocumentUtil
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CaptureExecutor : SurfaceHolder.Callback, PreviewFrameShotListener, DecodeListener, LifecycleObserver {

  private val context: Context
  private val params: CaptureParams

  constructor(activity: Activity, params: CaptureParams) {
    this.context = activity
    this.params = params
    if (activity is FragmentActivity) {
      activity.lifecycle.addObserver(this)
    }
  }

  constructor(fragment: Fragment, params: CaptureParams) {
    this.context = fragment.requireContext()
    this.params = params
    fragment.lifecycle.addObserver(this)
  }

  constructor(context: Context, params: CaptureParams) {
    this.context = context
    this.params = params
    if (context is FragmentActivity) {
      context.lifecycle.addObserver(this)
    } else {
      onCreate()
    }
  }

  private var beepManager: BeepManager? = null
  private var cameraManager: CameraManager? = null

  @Volatile
  private var decodeThread: DecodeThread? = null
  private var previewFrameRect: Rect? = null

  @Volatile
  private var isDecoding = false

  private var vibrator = true
  private var playBeep = true

  @Volatile
  private var isPreviewed = false

  @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
  private fun onCreate() {
    params.getSurfaceView().holder.addCallback(this)
  }

  override fun surfaceCreated(holder: SurfaceHolder) {
    params.checkPermission(object : PermissionListener {
      override fun onPermissionGranted() {
        cameraManager = CameraManager(context)
        cameraManager!!.setPreviewFrameShotListener(this@CaptureExecutor)
        cameraManager!!.initCamera(holder)
        if (!cameraManager!!.isCameraAvailable) {
          params.getCaptureListener().onScanFailed(IllegalStateException(context.getString(R.string.capture_camera_failed)))
          cameraManager = null
          return
        }
        if (playBeep) {
          beepManager = BeepManager(context)
          beepManager!!.updatePrefs()
        }
        cameraManager!!.startPreview()
        isPreviewed = true
        params.getCaptureListener().onPreviewSucceed()
        if (!isDecoding) {
          cameraManager!!.requestPreviewFrameShot()
        }
      }
    })
  }

  override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    //unsupported
  }

  override fun surfaceDestroyed(holder: SurfaceHolder) {
    isPreviewed = false
    holder.removeCallback(this)
    cameraManager?.stopPreview()
    decodeThread?.cancel()
    cameraManager?.release()
    cameraManager = null
    decodeThread = null
  }

  override fun onPreviewFrame(data: ByteArray, dataSize: Size) {
    decodeThread?.cancel()
    previewFrameRect = previewFrameRect
        ?: cameraManager?.getPreviewFrameRect(params.getCaptureView().frameRect) ?: return
    val luminanceSource = PlanarYUVLuminanceSource(data, dataSize, previewFrameRect)
    decodeThread = DecodeThread(luminanceSource, this)
    isDecoding = true
    decodeThread!!.execute()
  }

  override fun onDecodeSuccess(result: Result, source: LuminanceSource, bitmap: Bitmap) {
    var bmp = bitmap
    beepManager?.playBeepSound()
    if (vibrator) {
      vibrator()
    }
    isDecoding = false
    if (bmp.width > 100 || bmp.height > 100) {
      val matrix = Matrix()
      matrix.postScale(100f / bmp.width, 100f / bmp.height)
      val resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp
          .height, matrix, true)
      bmp.recycle()
      bmp = resizeBmp
    }
    params.getCaptureListener().onScanSucceed(bmp, result.text)
  }

  override fun onDecodeFailed(source: LuminanceSource) {
    if (source is RGBLuminanceSourcePixels) {
      params.getCaptureListener().onScanFailed(IllegalStateException(context.getString(R.string.capture_decode_failed)))
    }
    isDecoding = false
    cameraManager?.requestPreviewFrameShot()
  }

  override fun foundPossibleResultPoint(point: ResultPoint) {
    params.getCaptureView().addPossibleResultPoint(point)
  }

  /**
   * Call after CaptureListener.onPreviewSucceed
   */
  fun isFlashEnable(): Boolean {
    if (!isPreviewed) return false
    return cameraManager?.isFlashlightAvailable ?: false
  }

  /**
   * Call after CaptureListener.onPreviewSucceed
   */
  fun disableFlashlight() {
    if (!isPreviewed) return
    cameraManager?.disableFlashlight()
  }

  /**
   * Call after CaptureListener.onPreviewSucceed
   */
  fun enableFlashlight() {
    if (!isPreviewed) return
    cameraManager?.enableFlashlight()
  }

  fun parseImage(uri: Uri) {
    GlobalScope.launch {
      val path = withContext(Dispatchers.IO) {
        DocumentUtil.getPath(context, uri)
      }
      if (path.isNullOrEmpty()) {
        params.getCaptureListener().onScanFailed(java.lang.IllegalStateException(context.getString(R.string.image_path_error)))
      } else {
        parseImage(path)
      }
    }
  }

  fun parseImage(path: String) {
    GlobalScope.launch {
      val cameraBitmap = withContext(Dispatchers.IO) { DocumentUtil.getBitmap(path) }
      if (cameraBitmap == null) {
        params.getCaptureListener().onScanFailed(java.lang.IllegalStateException(context.getString(R.string.image_path_error)))
      } else {
        parseImage(cameraBitmap)
      }
    }
  }

  fun parseImage(bitmap: Bitmap) {
    decodeThread?.cancel()
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    val luminanceSource = RGBLuminanceSourcePixels(pixels, Size(width, height))
    decodeThread = DecodeThread(luminanceSource, this)
    isDecoding = true
    decodeThread!!.execute()
  }

  @RequiresPermission(android.Manifest.permission.VIBRATE)
  private fun vibrator() {
    if (PermissionChecker.checkSelfPermission(context, android.Manifest.permission.VIBRATE)
        == PermissionChecker.PERMISSION_GRANTED) {
      val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      vibrator.vibrate(200L)
    }
  }

  fun vibrator(vibrator: Boolean) {
    this.vibrator = vibrator
  }

  fun playBeep(playBeep: Boolean) {
    this.playBeep = playBeep
  }
}