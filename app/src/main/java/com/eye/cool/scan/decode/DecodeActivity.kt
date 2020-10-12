package com.eye.cool.scan.decode

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.eye.cool.scan.decode.listener.PermissionChecker
import com.eye.cool.scan.decode.supprot.complete
import com.eye.cool.scan.encode.QRCodeUtil
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

abstract class DecodeActivity : AppCompatActivity(), PermissionChecker {

  private lateinit var executor: DecodeExecutor
  private var continuation: CancellableContinuation<Boolean>? = null

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    val params = getDecodeParams()
    if (params.permissionChecker == null) {
      params.permissionChecker = this
    }
    executor = DecodeExecutor(this, params)
  }

  abstract fun getDecodeParams(): DecodeParams

  /**
   * Request permission then call executor.execute().
   */
  override suspend fun checkPermission(
      permissions: Array<String>
  ) = suspendCancellableCoroutine<Boolean> {
    val target = applicationInfo.targetSdkVersion
    if (target >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermissions(permissions, 1001)
      this.continuation = it
    } else {
      if (QRCodeUtil.isCameraAvailable()) it.complete(true)
    }
  }

  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<out String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 1001) {
      if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        continuation?.complete(true)
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
  fun disableFlashlight(): Boolean {
    return executor.disableFlashlight()
  }

  /**
   * Call after CaptureListener.onPreviewSucceed
   * <uses-permission android:name="android.permission.FLASHLIGHT"/>
   */
  fun enableFlashlight(): Boolean {
    return executor.enableFlashlight()
  }

  /**
   * Call after CaptureListener.onPreviewSucceed
   * <uses-permission android:name="android.permission.FLASHLIGHT"/>
   */
  fun toggleFlashlight(): Boolean {
    return executor.toggleFlashlight()
  }
}
