package com.eye.cool.scan.camera

internal interface PreviewFrameShotListener {
  fun onPreviewFrame(data: ByteArray, frameSize: Size)
}