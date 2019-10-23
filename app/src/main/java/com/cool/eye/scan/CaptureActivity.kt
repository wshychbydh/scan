package com.cool.eye.scan

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.cool.eye.scan.listener.CaptureListener
import com.cool.eye.scan.listener.CaptureParams
import com.cool.eye.scan.listener.PermissionListener
import com.cool.eye.scan.view.CaptureView

@TargetApi(Build.VERSION_CODES.KITKAT)
abstract class CaptureActivity : AppCompatActivity() {

  private lateinit var executor: CaptureExecutor

  protected abstract fun getCaptureListener(): CaptureListener
  protected abstract fun getSurfaceView(): SurfaceView
  protected abstract fun getCaptureView(): CaptureView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val params = object : CaptureParams {
      override fun getCaptureListener() = this@CaptureActivity.getCaptureListener()

      override fun getSurfaceView() = this@CaptureActivity.getSurfaceView()

      override fun getCaptureView() = this@CaptureActivity.getCaptureView()

      override fun checkPermission(listener: PermissionListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          listener.onPermissionGranted()
          requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1001)
        } else {
          listener.onPermissionGranted()
        }
      }
    }
    executor = CaptureExecutor(this, params)
  }

  private var callback: PermissionListener? = null

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 1001) {
      //这里只请求了一个权限，实际情况需要逐个判断
      //参考https://github.com/wshychbydh/permission
      if (grantResults.size == 1) {
        callback?.onPermissionGranted()
      }
    }
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

  fun vibrator(vibrator: Boolean) {
    executor.vibrator(vibrator)
  }

  fun playBeep(playBeep: Boolean) {
    executor.playBeep(playBeep)
  }
}
