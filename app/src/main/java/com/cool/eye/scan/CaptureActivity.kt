package com.cool.eye.scan

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.cool.eye.scan.encode.QRCodeUtil
import com.cool.eye.scan.listener.CaptureParams
import com.cool.eye.scan.listener.PermissionListener

abstract class CaptureActivity : AppCompatActivity(), CaptureParams {

  private lateinit var executor: CaptureExecutor

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    executor = CaptureExecutor(this, this)
  }

  private var callback: PermissionListener? = null

  override fun checkPermission(listener: PermissionListener) {
    val target = applicationInfo.targetSdkVersion
    if (target >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      callback = listener
      requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1001)
    } else {
      if (QRCodeUtil.isCameraAvailable()) {
        listener.onPermissionGranted()
      }
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 1001) {
      //reference https://github.com/wshychbydh/permission
      if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        callback?.onPermissionGranted()
      }
    }
  }

  fun parseImage(uri: Uri) {
    executor.parseImage(uri)
  }

  fun parseImage(path: String) {
    executor.parseImage(path)
  }

  fun parseImage(bitmap: Bitmap) {
    executor.parseImage(bitmap)
  }

  /**
   * Call after CaptureListener.onPreviewSucceed
   */
  fun isFlashEnable(): Boolean {
    return executor.isFlashEnable()
  }

  /**
   * Call after CaptureListener.onPreviewSucceed
   */
  fun disableFlashlight() {
    executor.disableFlashlight()
  }

  /**
   * Call after CaptureListener.onPreviewSucceed
   */
  fun enableFlashlight() {
    executor.enableFlashlight()
  }

  fun vibrator(enable: Boolean) {
    executor.vibrator(enable)
  }

  fun playBeep(playBeep: Boolean) {
    executor.playBeep(playBeep)
  }
}
