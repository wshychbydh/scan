package com.cool.eye.scan

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.view.SurfaceHolder
import androidx.annotation.RequiresPermission
import androidx.core.content.PermissionChecker
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

@TargetApi(Build.VERSION_CODES.KITKAT)
class CaptureExecutor(private val activity: FragmentActivity, private val params: CaptureParams)
  : SurfaceHolder.Callback, PreviewFrameShotListener, DecodeListener, LifecycleObserver {

  private var beepManager: BeepManager? = null
  private var cameraManager: CameraManager? = null
  @Volatile
  private var decodeThread: DecodeThread? = null
  private var previewFrameRect: Rect? = null
  @Volatile
  private var isDecoding = false

  private var vibrator = true
  private var playBeep = true

  init {
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    activity.lifecycle.addObserver(this)
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
  fun onCreate() {
    params.getSurfaceView().holder.addCallback(this)
  }

  override fun surfaceCreated(holder: SurfaceHolder) {
    params.checkPermission(object : PermissionListener {
      override fun onPermissionGranted() {
        cameraManager = CameraManager(activity)
        cameraManager!!.setPreviewFrameShotListener(this@CaptureExecutor)
        cameraManager!!.initCamera(holder)
        if (!cameraManager!!.isCameraAvailable) {
          params.getCaptureListener().onScanFailed(IllegalStateException(activity.getString(R.string.capture_camera_failed)))
          return
        }
        if (playBeep) {
          beepManager = BeepManager(activity)
          beepManager!!.updatePrefs()
        }
        cameraManager!!.startPreview()
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
    cameraManager?.stopPreview()
    decodeThread?.cancel()
    cameraManager?.release()
  }

  override fun onPreviewFrame(data: ByteArray, dataSize: Size) {
    decodeThread?.cancel()
    previewFrameRect = previewFrameRect
        ?: cameraManager?.getPreviewFrameRect(params.getCaptureView().frameRect) ?: return
    val luminanceSource = PlanarYUVLuminanceSource(data, dataSize, previewFrameRect)
    decodeThread = DecodeThread(luminanceSource, this@CaptureExecutor)
    isDecoding = true
    decodeThread!!.execute()
  }

  override fun onDecodeSuccess(result: Result, source: LuminanceSource, bitmap: Bitmap) {
    var bitmap = bitmap
    beepManager?.playBeepSound()
    if (vibrator) {
      vibrator()
    }
    isDecoding = false
    if (bitmap.width > 100 || bitmap.height > 100) {
      val matrix = Matrix()
      matrix.postScale(100f / bitmap.width, 100f / bitmap.height)
      val resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap
          .height, matrix, true)
      bitmap.recycle()
      bitmap = resizeBmp
    }
    params.getCaptureListener().onScanSucceed(bitmap, result.text)
  }

  override fun onDecodeFailed(source: LuminanceSource) {
    if (source is RGBLuminanceSourcePixels) {
      params.getCaptureListener().onScanFailed(IllegalStateException(activity.getString(R.string.capture_decode_failed)))
    }
    isDecoding = false
    cameraManager!!.requestPreviewFrameShot()
  }

  override fun foundPossibleResultPoint(point: ResultPoint) {
    params.getCaptureView()?.addPossibleResultPoint(point)
  }

  /**
   * Call after CaptureListener.onPreviewSucceed
   */
  fun isFlashEnable(): Boolean {
    return cameraManager?.isFlashlightAvailable ?: false
  }

  /**
   * Call after CaptureListener.onPreviewSucceed
   */
  fun disableFlashlight() {
    cameraManager?.disableFlashlight()
  }

  /**
   * Call after CaptureListener.onPreviewSucceed
   */
  fun enableFlashlight() {
    cameraManager?.enableFlashlight()
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  fun parseImage(uri: Uri) {
    GlobalScope.launch {
      val path = withContext(Dispatchers.IO) {
        DocumentUtil.getPath(activity, uri)
      }
      if (path.isNullOrEmpty()) {
        params.getCaptureListener().onScanFailed(java.lang.IllegalStateException(activity.getString(R.string.image_path_error)))
      } else {
        parseImage(path)
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  fun parseImage(path: String) {
    GlobalScope.launch {
      val cameraBitmap = withContext(Dispatchers.IO) { DocumentUtil.getBitmap(path) }
      if (cameraBitmap == null) {
        params.getCaptureListener().onScanFailed(java.lang.IllegalStateException(activity.getString(R.string.image_path_error)))
      } else {
        parseImage(cameraBitmap)
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
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
  fun vibrator() {
    if (PermissionChecker.checkSelfPermission(activity, android.Manifest.permission.VIBRATE)
        == PermissionChecker.PERMISSION_GRANTED) {
      val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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