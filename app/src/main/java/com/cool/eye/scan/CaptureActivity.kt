package com.cool.eye.scan

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.cool.eye.scan.listener.CaptureParams
import com.cool.eye.scan.listener.PermissionListener

@TargetApi(Build.VERSION_CODES.KITKAT)
abstract class CaptureActivity : AppCompatActivity(), CaptureParams {

  protected lateinit var executor: CaptureExecutor

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
}
