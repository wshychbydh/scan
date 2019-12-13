package com.cool.eye.scan

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.cool.eye.scan.encode.QRCodeUtil
import com.cool.eye.scan.listener.CaptureParams
import com.cool.eye.scan.listener.PermissionListener

abstract class CaptureFragment : Fragment(), CaptureParams {

  private lateinit var executor: CaptureExecutor

  @CallSuper
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    executor = CaptureExecutor(this, this)
  }

  private var callback: PermissionListener? = null

  override fun checkPermission(listener: PermissionListener) {
    val target = requireContext().applicationInfo.targetSdkVersion
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

  fun vibratorAble(enable: Boolean) {
    executor.vibrator(enable)
  }

  fun playBeepAble(playBeep: Boolean) {
    executor.playBeep(playBeep)
  }
}
