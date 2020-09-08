package com.eye.cool.scan.listener

import android.view.SurfaceView
import com.eye.cool.scan.view.CaptureView

interface CaptureParams {

  fun checkPermission(listener: PermissionListener)

  fun getCaptureListener(): CaptureListener

  fun getSurfaceView(): SurfaceView

  fun getCaptureView(): CaptureView
}