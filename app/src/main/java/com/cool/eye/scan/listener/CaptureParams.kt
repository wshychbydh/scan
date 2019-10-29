package com.cool.eye.scan.listener

import android.view.SurfaceView
import com.cool.eye.scan.view.CaptureView

interface CaptureParams {

  fun checkPermission(listener: PermissionListener)

  fun getCaptureListener(): CaptureListener

  fun getSurfaceView(): SurfaceView

  fun getCaptureView(): CaptureView
}