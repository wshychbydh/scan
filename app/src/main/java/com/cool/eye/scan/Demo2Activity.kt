//package com.cool.eye.scan

//import android.graphics.Bitmap
//import android.os.Bundle
//import android.view.SurfaceView
//import android.view.View
//import android.widget.Toast
//import com.cool.eye.scan.listener.CaptureListener
//import com.cool.eye.scan.view.CaptureView
//import kotlinx.android.synthetic.main.activity_capture.*
//
//class Demo2Activity : CaptureActivity(), CaptureListener {
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
//        enableFlashlight()
//      } else {
//        disableFlashlight()
//      }
//    }
//  }
//
//  override fun getCaptureListener() = this
//
//  override fun getSurfaceView(): SurfaceView = surfaceView
//
//  override fun getCaptureView(): CaptureView = captureView
//
//  override fun onScanSucceed(bitmap: Bitmap, content: String) {
//    //TODO 扫码成功
//    ScanResultActivity.launch(this, bitmap, content)
//  }
//
//  override fun onScanFailed(throwable: Throwable) {
//    //TODO 扫码失败
//    Toast.makeText(this, throwable.message, Toast.LENGTH_SHORT).show()
//  }
//
//  override fun onPreviewSucceed() {
//    //TODO 预览成功
//    flashCb.visibility = if (isFlashEnable()) View.VISIBLE else View.GONE
//  }
//}
