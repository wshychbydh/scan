package com.cool.eye.scan.decode;

import android.graphics.BitmapFactory;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import java.io.File;
import java.util.HashMap;

/**
 * Created by cool on 17-2-9.
 */

public class DecodeUtil {

  public static String decode(File file) {
    HashMap<DecodeHintType, String> hints = new HashMap<>();
    hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
    RGBLuminanceSourceBitmap source = new RGBLuminanceSourceBitmap(
        BitmapFactory.decodeFile(file.getAbsolutePath()));
    BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
    QRCodeReader reader2 = new QRCodeReader();
    try {
      return reader2.decode(bitmap1, hints).getText();
    } catch (NotFoundException e) {
      e.printStackTrace();
    } catch (ChecksumException e) {
      e.printStackTrace();
    } catch (FormatException e) {
      e.printStackTrace();
    }
    return null;
  }
}
