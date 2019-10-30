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
        implementation 'com.github.wshychbydh:scan:1.2.4'
    }
```

3、自定义布局，布局中必须包含SurfaceView 和 com.cool.eye.scan.CaptureView


4、构建CaptureParams

```   
     1)、初始化CaptureExecutor(activity, CaptureParams)
     2)、在checkPermission(listener: PermissionListener)方法中请求Camera权限，
     3)、然后在授权成功后回调listener.onPermissionGranted()
```


5、直接继承CaptureActivity
    
#### 联系方式 wshychbydh@gmail.com

[![](https://jitpack.io/v/wshychbydh/scan.svg)](https://jitpack.io/#wshychbydh/scan)
