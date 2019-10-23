//package com.cool.eye.scan
//
//import android.graphics.Bitmap
//import android.os.Build
//import android.os.Bundle
//import android.view.SurfaceView
//import android.view.View
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.cool.eye.scan.listener.CaptureListener
//import com.cool.eye.scan.listener.CaptureParams
//import com.cool.eye.scan.listener.PermissionListener
//import com.cool.eye.scan.view.CaptureView
//import kotlinx.android.synthetic.main.activity_capture.*
//
//class DemoActivity : AppCompatActivity(), CaptureListener, CaptureParams {
//
//  private val executor = CaptureExecutor(this, this)
//
//  override fun onCreate(savedInstanceState: Bundle?) {
//    super.onCreate(savedInstanceState)
//    setContentView(R.layout.activity_capture)
//
//    albumIv.setOnClickListener {
//      Toast.makeText(this, "选择图片", Toast.LENGTH_SHORT).show()
//      //参考https://github.com/wshychbydh/photo
//      //1、Select qr image from local
//      //2、Call parseImage(path)
//    }
//
//    flashCb.setOnCheckedChangeListener { _, isChecked ->
//      if (isChecked) {
//        executor.enableFlashlight()
//      } else {
//        executor.disableFlashlight()
//      }
//    }
//  }
//
//  override fun checkPermission(listener: PermissionListener) {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//      listener.onPermissionGranted()
//      requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1001)
//    } else {
//      listener.onPermissionGranted()
//    }
//  }
//
//  override fun getCaptureListener() = this
//
//  override fun getSurfaceView(): SurfaceView = surfaceView
//
//  override fun getCaptureView(): CaptureView = captureView
//
//
//  private var callback: PermissionListener? = null
//
//  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    if (requestCode == 1001) {
//      //这里只请求了一个权限，实际情况需要逐个判断
//      //参考https://github.com/wshychbydh/permission
//      if (grantResults.size == 1) {
//        callback?.onPermissionGranted()
//      }
//    }
//  }
//
//  override fun onScanSucceed(bitmap: Bitmap, content: String) {
//    ScanResultActivity.launch(this, bitmap, content)
//  }
//
//  override fun onScanFailed(throwable: Throwable) {
//    Toast.makeText(this, throwable.message, Toast.LENGTH_SHORT).show()
//  }
//
//  override fun onPreviewSucceed() {
//    flashCb.visibility = if (executor.isFlashEnable()) View.VISIBLE else View.GONE
//  }
//}
