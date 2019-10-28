/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cool.eye.scan.decode;

import android.graphics.Bitmap;
import android.graphics.Rect;
import com.cool.eye.scan.camera.Size;

public class PlanarYUVLuminanceSource extends LuminanceSource {

  private byte[] yuvData;
  private Size dataSize;
  private Rect previewRect;

  /**
   * @param yuvData data of YUV
   * @param dataSize image size
   * @param previewRect Image area to process
   */
  public PlanarYUVLuminanceSource(byte[] yuvData, Size dataSize, Rect previewRect) {
    super(previewRect.width(), previewRect.height());

    if (previewRect.left + previewRect.width() > dataSize.width
        || previewRect.top + previewRect.height() > dataSize.height) {
      throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
    }

    this.yuvData = yuvData;
    this.dataSize = dataSize;
    this.previewRect = previewRect;
  }

  @Override
  public byte[] getRow(int y, byte[] row) {
    if (y < 0 || y >= getHeight()) {
      throw new IllegalArgumentException("Requested row is outside the image: " + y);
    }
    int width = getWidth();
    if (row == null || row.length < width) {
      row = new byte[width];
    }
    int offset = (y + previewRect.top) * dataSize.width + previewRect.left;
    System.arraycopy(yuvData, offset, row, 0, width);
    return row;
  }

  @Override
  public byte[] getMatrix() {
    int width = getWidth();
    int height = getHeight();
    if (width == dataSize.width && height == dataSize.height) {
      return yuvData;
    }
    int area = width * height;
    byte[] matrix = new byte[area];
    int inputOffset = previewRect.top * dataSize.width + previewRect.left;

    if (width == dataSize.width) {
      System.arraycopy(yuvData, inputOffset, matrix, 0, area);
      return matrix;
    }

    byte[] yuv = yuvData;
    for (int y = 0; y < height; y++) {
      int outputOffset = y * width;
      System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
      inputOffset += dataSize.width;
    }
    return matrix;
  }

  @Override
  public boolean isCropSupported() {
    return true;
  }

  public int getDataWidth() {
    return dataSize.width;
  }

  public int getDataHeight() {
    return dataSize.height;
  }

  /**
   * Based on the scan results, a grayscale image is generated
   */
  public Bitmap renderCroppedGreyScaleBitmap() {
    int width = getWidth();
    int height = getHeight();
    int[] pixels = new int[width * height];
    byte[] yuv = yuvData;
    int inputOffset = previewRect.top * dataSize.width + previewRect.left;
    for (int y = 0; y < height; y++) {
      int outputOffset = y * width;
      for (int x = 0; x < width; x++) {
        int grey = yuv[inputOffset + x] & 0xff;
        pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
      }
      inputOffset += dataSize.width;
    }

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    return bitmap;
  }
}
