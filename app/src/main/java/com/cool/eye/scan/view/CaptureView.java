package com.cool.eye.scan.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.DrawableRes;
import com.cool.eye.scan.R;
import com.google.zxing.ResultPoint;
import java.util.LinkedList;

public class CaptureView extends View {

  private class PossiblePoint {

    public float x, y;
    public long foundTime;
  }

  public static final int MASK_COLOR = 0x80000000;
  public static final int POSSIBLE_POINT_COLOR = 0xC0FFFF00;
  public static final int POSSIBLE_POINT_ALIVE_DURATION = 200;
  public static final int SCAN_DURATION = 1500;

  private int maskColor = MASK_COLOR;
  private int possiblePointColor = POSSIBLE_POINT_COLOR;
  private int possiblePointAliveDuration = POSSIBLE_POINT_ALIVE_DURATION;
  private int scanDuration = SCAN_DURATION;
  private long startTime = -1;

  private Rect frame;
  private Paint paint;
  private LinkedList<PossiblePoint> possiblePoints;
  private Drawable frameDrawable, scannerDrawable;
  private int scannerHeight = 0;

  public CaptureView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  public CaptureView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public CaptureView(Context context) {
    super(context);
    init(context, null);
  }

  public void init(Context context, AttributeSet attrs) {
    frame = new Rect();
    paint = new Paint();
    paint.setAntiAlias(true);
    possiblePoints = new LinkedList<>();
    int frameResId = R.drawable.scan_qrcode_frame;
    int scannerResId = R.drawable.scan_qrcode_scaner;
    if (attrs != null) {
      TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.scan_captureView);
      maskColor = array.getColor(R.styleable.scan_captureView_scan_mask_color, MASK_COLOR);
      possiblePointColor = array
          .getColor(R.styleable.scan_captureView_scan_possible_point_color, POSSIBLE_POINT_COLOR);
      possiblePointAliveDuration = array
          .getInt(R.styleable.scan_captureView_scan_possible_point_alive_duration,
              POSSIBLE_POINT_ALIVE_DURATION);
      scanDuration = array.getInt(R.styleable.scan_captureView_scan_duration, SCAN_DURATION);
      frameResId = array
          .getResourceId(R.styleable.scan_captureView_scan_frame_drawable, R.drawable.scan_qrcode_frame);
      scannerResId = array
          .getResourceId(R.styleable.scan_captureView_scan_scanner_drawable, R.drawable.scan_qrcode_scaner);
      array.recycle();
    }

    frameDrawable = getResources().getDrawable(frameResId);
    scannerDrawable = getResources().getDrawable(scannerResId);
  }

  public void setMaskColor(int maskColor) {
    this.maskColor = maskColor;
    invalidate();
  }

  public void setPossiblePointAliveDuration(int possiblePointAliveDuration) {
    this.possiblePointAliveDuration = possiblePointAliveDuration;
    invalidate();
  }

  public void setPossiblePointColor(int color) {
    this.possiblePointColor = color;
    invalidate();
  }

  public void setScanDuration(int duration) {
    scanDuration = duration;
    invalidate();
  }

  public void setFrameDrawableRes(@DrawableRes int resId) {
    frameDrawable = getResources().getDrawable(resId);
    invalidate();
  }

  public void setScannerDrawableRes(@DrawableRes int resId) {
    scannerDrawable = getResources().getDrawable(resId);
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int width = getMeasuredWidth();
    int height = getMeasuredHeight();
    int length = (int) (width * 0.6);
    frame.left = width / 2 - length / 2;
    frame.right = width / 2 + length / 2;
    frame.top = height / 2 - length / 2;
    frame.bottom = height / 2 + length / 2;
    frameDrawable.setBounds(frame.left - 10, frame.top - 10, frame.right + 10, frame.bottom + 10);
    scannerHeight =
        scannerDrawable.getIntrinsicHeight() * frame.width() / scannerDrawable.getIntrinsicWidth();
  }

  public Rect getFrameRect() {
    return frame;
  }

  @Override
  protected void onDraw(Canvas canvas) {

    // Draw mask
    paint.setColor(maskColor);
    paint.setStyle(Style.FILL);
    int width = canvas.getWidth();
    int height = canvas.getHeight();
    canvas.drawRect(0, 0, width, frame.top, paint);
    canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
    canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
    canvas.drawRect(0, frame.bottom, width, height, paint);

    // Draw possible points
    paint.setColor(possiblePointColor);
    paint.setStyle(Style.FILL);
    long current = System.currentTimeMillis();
    while (possiblePoints.size() > 0
        && current - possiblePoints.peek().foundTime >= possiblePointAliveDuration) {
      possiblePoints.poll();
    }
    for (int i = 0; i < possiblePoints.size(); i++) {
      PossiblePoint point = possiblePoints.get(i);
      int radius = (int) (5 * (possiblePointAliveDuration - current + point.foundTime)
          / possiblePointAliveDuration);
      if (radius > 0) {
        canvas.drawCircle(frame.left + point.x, frame.top + point.y, radius, paint);
      }
    }

    // Draw scanner
    long now = System.currentTimeMillis();
    if (startTime < 0) {
      startTime = now;
    }
    int timePast = (int) ((now - startTime) % scanDuration);
    if (timePast >= 0 && timePast <= scanDuration / 2) {
      int scannerShift = frame.height() * 2 * timePast / scanDuration;
      canvas.save();
      canvas.clipRect(frame);
      scannerDrawable.setBounds(frame.left, frame.top + scannerShift, frame.right,
          frame.top + scannerHeight + scannerShift);
      scannerDrawable.draw(canvas);
      canvas.restore();
    }
    // Draw frame
    frameDrawable.draw(canvas);

    invalidate();
  }

  public void addPossibleResultPoint(ResultPoint point) {
    PossiblePoint pp = new PossiblePoint();
    pp.foundTime = System.currentTimeMillis();
    pp.x = point.getX();
    pp.y = point.getY();
    if (possiblePoints.size() >= 10) {
      possiblePoints.poll();
    }
    possiblePoints.add(pp);
  }
}
