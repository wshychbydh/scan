# Scan

条形码、二维码扫码实现

#### 使用方法：

1. 在root目录的build.gradle目录中添加
```
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

2. 在项目的build.gradle中添加依赖
```
    dependencies {
        implementation 'com.github.wshychbydh:scan:Tag'
    }
```

3. 添加布局，布局中包含SurfaceView 和 CaptureView，例如：
```
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  android:layout_width="match_parent"
  android:layout_height="match_parent">

  <SurfaceView                              //预览区域
    android:id="@+id/surfaceView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

  <com.eye.cool.scan.view.CaptureView       //扫码区域
    android:id="@+id/captureView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:scan_frame_drawable="drawable"      // 外边框
    app:scan_possible_point_color="color"   // 扫码框内闪点的颜色
    app:scan_duration="integer"             // 扫码条单次动画时长
    app:scan_scanner_drawable="drawable" /> // 扫码条

  <Button
    android:id="@+id/albumBtn"              //选择相册(可选)
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_margin="12dp"
    android:background="@null"
    android:text="Album"
    android:textColor="@android:color/white"
    android:textSize="15sp"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintBottom_toBottomOf="parent" />

  <Button
    android:id="@+id/flashlightBtn"         //闪光灯(可选)
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_margin="12dp"
    android:background="@null"
    android:text="Flashlight"
    android:textColor="@android:color/white"
    android:textSize="15sp"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintBottom_toBottomOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

4. 构建扫码参数：DecodeParams
```
   val params = DecodeParams.Builder() //如未按要求设置，将返回参数无效错误.
        .captureView(captureView)                 //(必须)
        .surfaceView(surfaceView)                 //(必须)
        .decodeListener(object :DecodeListener{   //(必须)
          override fun onPreviewSucceed() {
            //预览成功 (UI Thread)
          }

          override suspend fun onScanSucceed(bitmap: Bitmap, content: String) {
            //扫码成功 (Work Thread)
          }

          override fun onScanFailed(throwable: Throwable) {
			      //扫码失败 (UI Thread)
          }
        })
        .permissionChecker(this)     //camera权限请求 (必须), 参考DecodeFragment或DecodeActivity
        .scaleBitmap()               //是否缩放扫码得到的bitmap，缩放1倍（可选）
        .build()
```

5. 配置参数 （以下两种方式选一）

* 自定义构建DecodeExecutor

```
  val executor = CaptureExecutor(fragment/context, params)
  executor.parseImage(path|uri|bitmap) //解析图片
  executor.isFlashEnable()             //判断闪光灯是否可用
  executor.disableFlashlight()         //关闭闪光灯
  executor.enableFlashlight()          //开启闪光灯，需添加FLASHLIGHT权限
  executor.toggleFlashlight()          //切换闪光灯，需添加FLASHLIGHT权限
  executor.vibrator(Boolean)           //扫码成功时是否震动，默认true
  executor.playBeep(Boolean)           //扫码成功时是否播放提示声，默认true
```

* 继承DecodeFragment或DecodeActivity，并实现以下抽象方法，如：
```
  override fun getDecodeParams() = params

  //其他支持的操作同上
```

6. 生成二维码 (QRCodeUtil)
```
    //构建二维码参数（注：如果参数无效，将返回null的bitmap）
    val params = QRParams.Builder(content)  //（必传）二维码内容
        .setLogo()                //（可选）设置logo
        .setSize(width, height)   //（可选）二维码大小，默认500x500
        .setMargin()              //（可选）二维码边距，默认2px
        .setSavePath()            //（可选）二维码保存路径
        .setSaveQuality()         //（可选）图片质量（0-100)，默认100
        .setGapColor()            //（可选）间隙颜色，默认Color.WHITE
        .setQrColor()             //（可选）二维码颜色，默认Color.BLACK
        .setSaveFormat()          //（可选）保存格式，默认PNG
        .setBitmapConfig()        //（可选）Bitmap的Config，默认RGB_565
        .setLogoScale()           //（可选）Logo相对bitmap的比例，默认5.0
        .build()

    QRCodeUtil.createQRImage(params) {
      //主线程调用
    }

    val bitmap = QRCodeUtil.createQRImage(params)       //协程中调用

    val bitmap = QRCodeUtil.createQRImageAsync(params)  //非主线程调用

```

#####

**Demo地址：(https://github.com/wshychbydh/SampleDemo)**

##

###### **欢迎fork，期待你的宝贵意见** (*￣︶￣)

###### 联系方式 wshychbydh@gmail.com

[![](https://jitpack.io/v/wshychbydh/scan.svg)](https://jitpack.io/#wshychbydh/scan)