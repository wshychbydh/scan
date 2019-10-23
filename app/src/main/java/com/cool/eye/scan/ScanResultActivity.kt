//package com.cool.eye.scan
//
//import android.app.Activity
//import android.content.ClipboardManager
//import android.content.Context
//import android.content.Intent
//import android.graphics.Bitmap
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import kotlinx.android.synthetic.main.activity_scan_result.*
//
//class ScanResultActivity : AppCompatActivity() {
//
//  override fun onCreate(savedInstanceState: Bundle?) {
//    super.onCreate(savedInstanceState)
//    setContentView(R.layout.activity_scan_result)
//    tv_scan_content.text = intent.getStringExtra(CONTENT)
//    iv_scan.setImageBitmap(intent.getParcelableExtra(BITMAP))
//    btn_scan_copy.setOnClickListener {
//      val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//      cm.text = intent.getStringExtra(CONTENT)
//      Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show()
//    }
//  }
//
//  companion object {
//    const val CONTENT = "content"
//    const val BITMAP = "bitmap"
//    @JvmStatic
//    fun launch(activity: Activity, bitmap: Bitmap, content: String) {
//      var intent = Intent(activity, ScanResultActivity::class.java)
//      intent.putExtra(BITMAP, bitmap)
//      intent.putExtra(CONTENT, content)
//      activity.startActivity(intent)
//    }
//  }
//}
