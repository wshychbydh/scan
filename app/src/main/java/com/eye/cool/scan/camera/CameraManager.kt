package com.eye.cool.scan.camera

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.Camera
import android.hardware.Camera.AutoFocusCallback
import android.hardware.Camera.PreviewCallback
import android.view.SurfaceHolder
import android.view.WindowManager
import kotlin.math.abs

internal class CameraManager(context: Context) : AutoFocusCallback, PreviewCallback {
  private enum class CameraState {
    CLOSED, OPEN, PREVIEW
  }

  private var mCamera: Camera? = null
  private val screenSize: Size
  private var cameraSize: Size? = null
  private var mState: CameraState? = null
  private var mFrameShotListener: PreviewFrameShotListener? = null

  fun initCamera(holder: SurfaceHolder?) {
    mCamera = Camera.open() ?: return
    mState = CameraState.OPEN
    mCamera!!.setDisplayOrientation(90)
    val parameters = mCamera!!.parameters
    cameraSize = getBestPreviewSize(parameters, screenSize)
    parameters.setPreviewSize(cameraSize!!.height, cameraSize!!.width)
    parameters.previewFormat = ImageFormat.NV21 //Default
    mCamera!!.parameters = parameters
    mCamera!!.setPreviewDisplay(holder)
  }

  val isCameraAvailable: Boolean
    get() = mCamera != null
  val isFlashlightAvailable: Boolean
    get() {
      if (mCamera == null) {
        return false
      }
      val parameters = mCamera!!.parameters
      val flashModes = parameters.supportedFlashModes
      var isFlashOnAvailable = false
      var isFlashOffAvailable = false
      for (flashMode in flashModes) {
        if (Camera.Parameters.FLASH_MODE_TORCH == flashMode) {
          isFlashOnAvailable = true
        }
        if (Camera.Parameters.FLASH_MODE_OFF == flashMode) {
          isFlashOffAvailable = true
        }
        if (isFlashOnAvailable && isFlashOffAvailable) {
          return true
        }
      }
      return false
    }

  fun enableFlashlight(): Boolean {
    if (mCamera == null) {
      return false
    }
    val parameters = mCamera!!.parameters
    parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
    return try {
      mCamera!!.parameters = parameters
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  fun disableFlashlight(): Boolean {
    if (mCamera == null) {
      return false
    }
    val parameters = mCamera!!.parameters
    parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
    return try {
      mCamera!!.parameters = parameters
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  fun toggleFlashlight(): Boolean {
    if (mCamera == null) {
      return false
    }
    val parameters = mCamera!!.parameters
    if (Camera.Parameters.FLASH_MODE_TORCH == parameters.flashMode) {
      parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
    } else {
      parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
    }
    return try {
      mCamera!!.parameters = parameters
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  fun startPreview() {
    if (mCamera != null) {
      mState = CameraState.PREVIEW
      mCamera!!.startPreview()
      mCamera!!.autoFocus(this@CameraManager)
    }
  }

  fun stopPreview() {
    if (mCamera != null) {
      mCamera!!.stopPreview()
      mState = CameraState.OPEN
    }
  }

  fun release() {
    if (mCamera != null) {
      mCamera!!.setOneShotPreviewCallback(null)
      mCamera!!.setPreviewCallback(null)
      mCamera!!.release()
      mState = CameraState.CLOSED
    }
  }

  fun requestPreviewFrameShot() {
    try {
      mCamera?.setOneShotPreviewCallback(this)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun onPreviewFrame(data: ByteArray, camera: Camera) {
    var data = data
    if (mFrameShotListener != null) {
      data = rotateYUVdata90(data)
      mFrameShotListener!!.onPreviewFrame(data, cameraSize!!)
    }
  }

  /**
   * Gets the preview image size closest to the screen size
   */
  private fun getBestPreviewSize(parameters: Camera.Parameters, screenSize: Size): Size {
    val size = Size(screenSize)
    var diff = Int.MAX_VALUE
    val previewList = parameters.supportedPreviewSizes
    for (previewSize in previewList) {
      // Rotate 90 degrees
      val previewWidth = previewSize.height
      val previewHeight = previewSize.width
      val newDiff = (abs(previewWidth - screenSize.width) * abs(previewWidth - screenSize.width)
          + abs(previewHeight - screenSize.height) * abs(previewHeight - screenSize.height))
      if (newDiff == 0) {
        size.width = previewWidth
        size.height = previewHeight
        return size
      } else if (newDiff < diff) {
        diff = newDiff
        size.width = previewWidth
        size.height = previewHeight
      }
    }
    return size
  }

  /**
   * Because the preview image and screen size may be different,
   * the area on the screen should be converted to the corresponding area
   * on the preview image according to the scale
   */
  fun getPreviewFrameRect(screenFrameRect: Rect): Rect {
    checkNotNull(mCamera) { "Need call initCamera() before this." }
    val previewRect = Rect()
    previewRect.left = screenFrameRect.left * cameraSize!!.width / screenSize.width
    previewRect.right = screenFrameRect.right * cameraSize!!.width / screenSize.width
    previewRect.top = screenFrameRect.top * cameraSize!!.height / screenSize.height
    previewRect.bottom = screenFrameRect.bottom * cameraSize!!.height / screenSize.height
    return previewRect
  }

  private fun rotateYUVdata90(srcData: ByteArray): ByteArray {
    val desData = ByteArray(srcData.size)
    val srcWidth = cameraSize!!.height
    val srcHeight = cameraSize!!.width

    // Only copy Y
    var i = 0
    for (x in 0 until srcWidth) {
      for (y in srcHeight - 1 downTo 0) {
        desData[i++] = srcData[y * srcWidth + x]
      }
    }
    return desData
  }

  override fun onAutoFocus(success: Boolean, camera: Camera) {
    if (mCamera != null && mState == CameraState.PREVIEW) {
      try {
        mCamera!!.autoFocus(this@CameraManager)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun setPreviewFrameShotListener(l: PreviewFrameShotListener?) {
    mFrameShotListener = l
  }

  init {
    val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = manager.defaultDisplay
    screenSize = Size(display.width, display.height)
    mState = CameraState.CLOSED
  }
}