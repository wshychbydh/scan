package com.eye.cool.scan.decode

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Vibrator
import android.view.SurfaceHolder
import androidx.activity.ComponentActivity
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.eye.cool.scan.R
import com.eye.cool.scan.camera.CameraManager
import com.eye.cool.scan.camera.PreviewFrameShotListener
import com.eye.cool.scan.camera.Size
import com.eye.cool.scan.decode.supprot.DecodeResult
import com.eye.cool.scan.decode.source.LuminanceSource
import com.eye.cool.scan.decode.source.PlanarYUVLuminanceSource
import com.eye.cool.scan.decode.source.RGBLuminanceSourcePixels
import com.eye.cool.scan.decode.supprot.DecodeException
import com.eye.cool.scan.decode.supprot.DecodeException.Companion.CAMERA_FAILED
import com.eye.cool.scan.decode.supprot.DecodeException.Companion.DECODE_FAILED
import com.eye.cool.scan.decode.supprot.DecodeException.Companion.IMAGE_PATH_ERROR
import com.eye.cool.scan.decode.supprot.DecodeException.Companion.PARAMS_INVALID
import com.eye.cool.scan.decode.supprot.DecodeException.Companion.PARSE_FAILED
import com.eye.cool.scan.decode.supprot.DecodeException.Companion.PERMISSION_FAILED
import com.eye.cool.scan.decode.supprot.Decoder
import com.eye.cool.scan.util.BeepManager
import com.eye.cool.scan.util.DocumentUtil
import kotlinx.coroutines.*

class DecodeExecutor : SurfaceHolder.Callback, PreviewFrameShotListener, LifecycleObserver {

  private val context: Context
  private val params: DecodeParams
  private val scope = MainScope()

  private var beepManager: BeepManager? = null

  private var cameraManager: CameraManager? = null

  @Volatile
  private var isDecoding = false

  constructor(fragment: Fragment, params: DecodeParams) {
    this.context = fragment.requireContext()
    this.params = params
    fragment.lifecycle.addObserver(this)
    checkParams(params)
  }

  constructor(context: Context, params: DecodeParams) {
    this.context = context
    this.params = params
    if (context is ComponentActivity) {
      context.lifecycle.addObserver(this)
    }
    checkParams(params)
  }

  private fun checkParams(params: DecodeParams) {
    if (!params.isValid()) {
      scope.launch {
        onScanFailed(DecodeException(PARAMS_INVALID, context.getString(R.string.scan_params_invalid)))
      }
      return
    }
    params.surfaceView?.holder?.addCallback(this)
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  private fun onDestroy() {
    params.surfaceView?.holder?.removeCallback(this)
    cameraManager?.stopPreview()
    cameraManager?.release()
    cameraManager = null
    isDecoding = false
    scope.cancel()
  }

  override fun surfaceCreated(holder: SurfaceHolder) {
    scope.launch(Dispatchers.Default) {
      val result = params.permissionChecker?.checkPermission(
          arrayOf(android.Manifest.permission.CAMERA)
      ) ?: false
      if (result) {
        withContext(Dispatchers.Main) {
          if (!execute(holder)) {
            onScanFailed(DecodeException(CAMERA_FAILED, context.getString(R.string.scan_camera_failed)))
          }
        }
      } else {
        onScanFailed(DecodeException(PERMISSION_FAILED, context.getString(R.string.scan_permission_failed)))
      }
    }
  }

  override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    //ignore
  }

  override fun surfaceDestroyed(holder: SurfaceHolder) {
    //ignore
  }

  override fun onPreviewFrame(data: ByteArray, dataSize: Size) {
    if (isDecoding) return
    scope.launch(Dispatchers.IO) {
      val rect = cameraManager?.getPreviewFrameRect(
          params.captureView?.frameRect ?: return@launch
      ) ?: return@launch
      val luminanceSource = PlanarYUVLuminanceSource(data, dataSize, rect)
      decode(luminanceSource)
    }
  }

  private fun execute(holder: SurfaceHolder): Boolean {
    CameraManager(context).apply {
      setPreviewFrameShotListener(this@DecodeExecutor)
      initCamera(holder)
      cameraManager = this
      if (!isCameraAvailable) {
        cameraManager = null
        return false
      }
      if (params.playBeep) {
        beepManager = BeepManager(context)
        beepManager!!.updatePrefs()
      }
      startPreview()
      params.decodeListener?.onPreviewSucceed()
      if (!isDecoding) requestPreviewFrameShot()
    }
    return true
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
  fun disableFlashlight(): Boolean {
    return cameraManager?.disableFlashlight() ?: false
  }

  /**
   * Call after CaptureListener.onPreviewSucceed
   */
  fun enableFlashlight(): Boolean {
    return cameraManager?.enableFlashlight() ?: false
  }

  fun toggleFlashlight(): Boolean {
    return cameraManager?.toggleFlashlight() ?: false
  }

  fun parseImage(uri: Uri) {
    scope.launch(Dispatchers.IO) {
      val path = DocumentUtil.getPath(context, uri)
      if (path.isNullOrEmpty()) {
        onScanFailed(DecodeException(IMAGE_PATH_ERROR, context.getString(R.string.scan_image_path_error)))
      } else {
        parseImage(path)
      }
    }
  }

  fun parseImage(path: String) {
    scope.launch(Dispatchers.IO) {
      val cameraBitmap = DocumentUtil.getBitmap(path)
      if (cameraBitmap == null) {
        onScanFailed(DecodeException(IMAGE_PATH_ERROR, context.getString(R.string.scan_image_path_error)))
      } else {
        parseImage(cameraBitmap)
      }
    }
  }

  fun parseImage(bitmap: Bitmap) {
    if (isDecoding) return
    isDecoding = true
    scope.launch(Dispatchers.IO) {
      val width = bitmap.width
      val height = bitmap.height
      val pixels = IntArray(width * height)
      bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
      val luminanceSource = RGBLuminanceSourcePixels(pixels, Size(width, height))
      decode(luminanceSource, true)
    }
  }

  private suspend fun decode(source: LuminanceSource, isParseImage: Boolean = false) {
    isDecoding = true
    val result = Decoder.decode(source) {
      params.captureView?.addPossibleResultPoint(it)
    }
    if (result == null) {
      if (isParseImage) {
        onScanFailed(DecodeException(PARSE_FAILED, context.getString(R.string.scan_parse_failed)))
      } else {
        cameraManager?.requestPreviewFrameShot()
      }
    } else {
      if (result.isValid()) {
        onDecodeSuccess(result)
      } else {
        onScanFailed(DecodeException(DECODE_FAILED, context.getString(R.string.scan_decode_failed)))
        if (!isParseImage) cameraManager?.requestPreviewFrameShot()
      }
    }
    isDecoding = false
  }

  private suspend fun onScanFailed(exception: DecodeException) {
    withContext(Dispatchers.Main) {
      params.decodeListener?.onScanFailed(exception)
    }
  }

  private suspend fun onDecodeSuccess(result: DecodeResult) {
    beepManager?.playBeepSound()
    if (params.vibrator) vibrator()

    val bmp = result.bitmap!!
    val bitmap = if (params.scaleBitmap) {
      scaleBitmap(bmp, params.scaleFactor, params.scaleFilter)
    } else bmp
    params.decodeListener?.onScanSucceed(bitmap, result.result!!.text)
  }

  private fun scaleBitmap(bmp: Bitmap, factor: Float, filter: Boolean) = try {
    val width = (bmp.width / factor).toInt()
    val height = (bmp.height / factor).toInt()
    Bitmap.createScaledBitmap(bmp, width, height, filter)
  } finally {
    bmp.recycle()
  }

  private fun vibrator() {
    if (PermissionChecker.checkSelfPermission(context, android.Manifest.permission.VIBRATE)
        == PermissionChecker.PERMISSION_GRANTED) {
      val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      vibrator.vibrate(200L)
    }
  }
}