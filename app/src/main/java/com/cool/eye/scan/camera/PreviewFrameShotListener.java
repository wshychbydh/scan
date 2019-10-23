package com.cool.eye.scan.camera;

public interface PreviewFrameShotListener {
	void onPreviewFrame(byte[] data, Size frameSize);
}
