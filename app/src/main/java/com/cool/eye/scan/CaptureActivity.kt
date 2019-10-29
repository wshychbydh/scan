package com.cool.eye.scan

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.cool.eye.scan.listener.CaptureParams
import com.cool.eye.scan.listener.PermissionListener

@TargetApi(Build.VERSION_CODES.KITKAT)
abstract class CaptureActivity : AppCompatActivity(), CaptureParams {

  private lateinit var executor: CaptureExecutor

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    executor = CaptureExecutor(this, this)
  }

  private var callback: PermissionListener? = null

  override fun checkPermission(listener: PermissionListener) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      callback = listener
      requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1001)
    } else {
      listener.onPermissionGranted()
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

  @TargetApi(Build.VERSION_CODES.KITKAT)
  fun parseImage(uri: Uri) {
    executor.parseImage(uri)
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  fun parseImage(path: String) {
    executor.parseImage(path)
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
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

  fun vibratorAble(enable: Boolean) {
    executor.vibrator(enable)
  }

  fun playBeepAble(playBeep: Boolean) {
    executor.playBeep(playBeep)
  }
}
