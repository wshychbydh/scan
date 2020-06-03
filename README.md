# Scan

条形码、二维码扫码实现

#### 使用方法：

1、在root目录的build.gradle目录中添加
```
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

2、在项目的build.gradle中添加依赖
```
    dependencies {
        implementation 'com.github.wshychbydh:scan:1.2.6'
    }
```

3、添加布局，布局中包含SurfaceView 和 CaptureView，如：
```
 <SurfaceView
    android:id="@+id/surfaceView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

 <com.cool.eye.scan.view.CaptureView
    android:id="@+id/captureView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:frameDrawable="drawable"            // 外边框
    app:possiblePointColor="color"          // 扫码框内闪点的颜色
    app:scanDuration="scan"                 // 扫码条单次动画时长
    app:scannerDrawable="drawable" />       // 扫码条
```

4、创建实例CaptureListener
```
    val captureListener = object :CaptureListener{

    override fun onPreviewSucceed() {
      //预览成功回调
    }
    
    override fun onScanFailed(throwable: Throwable) {
      //扫码失败回调
    }

    override fun onScanSucceed(bitmap: Bitmap, content: String) {
      //扫码成功回调
    }
  }
```

5、配置参数 （以下两种方式选一）

1）自定义构建CaptureExecutor

```   
 val executor = CaptureExecutor(activity, params)  
 
 val params = object :CaptureParams{
    override fun checkPermission(listener: PermissionListener) {
      //1、请求权限相机权限（android.Manifest.permission.CAMERA）
      //2、若授权失败需自行处理，此时相机不会预览
      //3、授权成功后调用listener.onPermissionGranted()
    }

    override fun getCaptureListener(): CaptureListener = captureListener

    override fun getCaptureView(): CaptureView = captureView

    override fun getSurfaceView(): SurfaceView = surfaceView
    
  }
  
  //其他支持的操作  
  executor.parseImage(path|uri|bitmap)  //解析图片
  executor.isFlashEnable()      //判断闪光灯是否可用
  executor.disableFlashlight()  //关闭闪光灯
  executor.enableFlashlight()   //开启闪光灯
  executor.vibrator(Boolean)    //扫码成功时是否震动
  executor.playBeep(Boolean)    //扫码成功时是否播放提示声
```

2）直接继承CaptureFragment或CaptureActivity，并实现以下抽象方法，如：
```
  override fun getCaptureListener(): CaptureListener = captureListener

  override fun getCaptureView(): CaptureView = captureView

  override fun getSurfaceView(): SurfaceView = surfaceView
    
  //其他支持的操作同上
```

6、扩展功能类 (QRCodeUtil)
```
    //构建二维码参数（注：如果参数无效，将返回null的bitmap）
    val params = QRParams.Builder(content)  //（必传）二维码内容 
        .setLogo()                //（可选）设置logo
        .setSize(width, height)   //（可选）二维码大小，默认500x500
        .setMargin()              //（可选）二维码边距，默认2px
        .setSavePath()            //（可选）二维码保存路径
        .setSaveQuality()         //（可选）图片质量（0-100）
        .setGapColor()            //（可选）间隙颜色，默认白色
        .setQrColor()             //（可选）二维码颜色，默认黑色
        .setSaveFormat()          //（可选）保存格式，默认PNG
        .build()

    QRCodeUtil.createQRImage(params) {
      //主线程调用
    } 

    QRCodeUtil.createQRImage(params)     //协程中调用
    
    val bitmap = QRCodeUtil.createQRImageAsync(params)  //异步调用
    
```

#####   
 
**Demo地址：(https://github.com/wshychbydh/SampleDemo)**    
    
##

###### **欢迎fork，更希望你能贡献commit.** (*￣︶￣)    

###### 联系方式 wshychbydh@gmail.com

[![](https://jitpack.io/v/wshychbydh/scan.svg)](https://jitpack.io/#wshychbydh/scan)
