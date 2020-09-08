package com.eye.cool.scan.camera;

public interface PreviewFrameShotListener {
	void onPreviewFrame(byte[] data, Size frameSize);
}
