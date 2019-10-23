package com.cool.eye.scan.decode;

import android.graphics.Bitmap;
import com.google.zxing.LuminanceSource;

public class RGBLuminanceSourceBitmap extends LuminanceSource {

  private final byte[] luminances;
  private final int dataWidth;
  private final int dataHeight;
  private final int left;
  private final int top;

  public RGBLuminanceSourceBitmap(Bitmap bitmap) {
    super(bitmap.getWidth(), bitmap.getHeight());
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    int[] pixels = new int[width * height];
    bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

    dataWidth = width;
    dataHeight = height;
    left = 0;
    top = 0;
    // In order to measure pure decoding speed, we convert the entire image to a greyscale array
    // up front, which is the same as the Y channel of the YUVLuminanceSource in the real app.
    luminances = new byte[width * height];
    for (int y = 0; y < height; y++) {
      int offset = y * width;
      for (int x = 0; x < width; x++) {
        int pixel = pixels[offset + x];
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = pixel & 0xff;
        if (r == g && g == b) {
          // Image is already greyscale, so pick any channel.
          luminances[offset + x] = (byte) r;
        } else {
          // Calculate luminance cheaply, favoring green.
          luminances[offset + x] = (byte) ((r + g + g + b) >> 2);
        }
      }
    }
  }

  @Override
  public byte[] getMatrix() {
    int width = getWidth();
    int height = getHeight();
    // If the caller asks for the entire underlying image, save the copy and give them the
    // original data. The docs specifically warn that result.length must be ignored.
    if (width == dataWidth && height == dataHeight) {
      return luminances;
    }
    int area = width * height;
    byte[] matrix = new byte[area];
    int inputOffset = top * dataWidth + left;
    // If the width matches the full width of the underlying data, perform a single copy.
    if (width == dataWidth) {
      System.arraycopy(luminances, inputOffset, matrix, 0, area);
      return matrix;
    }
    // Otherwise copy one cropped row at a time.
    byte[] rgb = luminances;
    for (int y = 0; y < height; y++) {
      int outputOffset = y * width;
      System.arraycopy(rgb, inputOffset, matrix, outputOffset, width);
      inputOffset += dataWidth;
    }
    return matrix;

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
    int offset = (y + top) * dataWidth + left;
    System.arraycopy(luminances, offset, row, 0, width);
    return row;
  }
} 