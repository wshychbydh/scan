package com.eye.cool.scan.decode.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.eye.cool.scan.R
import com.google.zxing.ResultPoint
import java.util.*

class CaptureView : View {

  private inner class PossiblePoint {
    var x = 0f
    var y = 0f
    var foundTime: Long = 0
  }

  val frameRect = Rect()
  private var startTime: Long = -1
  private val paint = Paint()
  private var possiblePoints = LinkedList<PossiblePoint>()
  private var scannerHeight = 0
  private var descX = 0f
  private var descY = 0f

  private var params = CaptureParams.Builder().build()
    set(value) {
      field = value
      invalidate()
    }

  private var frameDrawable: Drawable? = null
  private var scannerDrawable: Drawable? = null

  constructor(context: Context) : super(context) {
    init(context, null)
  }

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    init(context, attrs)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    paint.isAntiAlias = true
    paint.isDither = true
    attrs?.let {
      val array = context.obtainStyledAttributes(it, R.styleable.scan_capture_view)
      params.maskColor = array.getColor(R.styleable.scan_capture_view_scan_mask_color, MASK_COLOR)
      params.possiblePointColor = array.getColor(
          R.styleable.scan_capture_view_scan_possible_point_color,
          POSSIBLE_POINT_COLOR
      )
      params.possiblePointAliveDuration = array.getInt(
          R.styleable.scan_capture_view_scan_possible_point_alive_duration,
          POSSIBLE_POINT_ALIVE_DURATION
      )
      params.scanDuration = array.getInt(R.styleable.scan_capture_view_scan_duration, SCAN_DURATION)
      params.frameResId = array.getResourceId(
          R.styleable.scan_capture_view_scan_frame_drawable,
          R.drawable.scan_frame_shape
      )
      params.scannerResId = array.getResourceId(
          R.styleable.scan_capture_view_scan_scanner_drawable,
          R.drawable.scan_scanner_shape
      )
      params.frameWidthRatio = array.getFloat(
          R.styleable.scan_capture_view_scan_frame_width_ratio,
          0.6f
      )
      params.frameHeightRatio = array.getFloat(
          R.styleable.scan_capture_view_scan_frame_height_ratio,
          0.4f
      )
      params.scanDesc = array.getString(R.styleable.scan_capture_view_scan_description)
      params.descTextColor = array.getColor(
          R.styleable.scan_capture_view_scan_text_color,
          Color.WHITE
      )
      params.descTextSize = array.getDimension(
          R.styleable.scan_capture_view_scan_text_size,
          context.resources.displayMetrics.density * 15f
      )
      params.descOffsetXRatio = array.getFloat(
          R.styleable.scan_capture_view_scan_text_offset_x_ratio,
          0f
      )
      params.descOffsetYRatio = array.getFloat(
          R.styleable.scan_capture_view_scan_text_offset_y_ratio,
          params.frameHeightRatio / 2.0f + 0.03f
      )
      array.recycle()
    }
    frameDrawable = ContextCompat.getDrawable(context, params.frameResId)
    scannerDrawable = ContextCompat.getDrawable(context, params.scannerResId)
  }


  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val width = measuredWidth
    val height = measuredHeight
    val frameWidth = (width * params.frameWidthRatio).toInt()
    val frameHeight = (height * params.frameHeightRatio).toInt()
    frameRect.left = (width - frameWidth) / 2
    frameRect.right = (width + frameWidth) / 2
    frameRect.top = (height - frameHeight) / 2
    frameRect.bottom = (height + frameHeight) / 2
    val offset = params.frameOffset
    frameDrawable?.setBounds(
        frameRect.left - offset,
        frameRect.top - offset,
        frameRect.right + offset,
        frameRect.bottom + offset
    )
    scannerDrawable?.apply {
      scannerHeight = intrinsicHeight * frameRect.width() / intrinsicWidth
    }

    params.scanDesc?.apply {
      paint.textSize = params.descTextSize
      descX = (width - paint.measureText(this)) / 2f + params.descOffsetXRatio * width
      descY = height / 2 + paint.descent() - paint.ascent() + params.descOffsetYRatio * height
    }
  }


  override fun onDraw(canvas: Canvas) {
    drawMask(canvas)
    drawPoints(canvas)
    drawDrawable(canvas)
    drawDesc(canvas)

    invalidate()
  }

  private fun drawMask(canvas: Canvas) {
    paint.color = params.maskColor
    paint.style = Paint.Style.FILL
    val width = canvas.width
    val height = canvas.height
    canvas.drawRect(0f, 0f, width.toFloat(), frameRect.top.toFloat(), paint)
    canvas.drawRect(
        0f,
        frameRect.top.toFloat(),
        frameRect.left.toFloat(),
        frameRect.bottom.toFloat(), paint
    )
    canvas.drawRect(
        frameRect.right.toFloat(),
        frameRect.top.toFloat(),
        width.toFloat(),
        frameRect.bottom.toFloat(), paint
    )
    canvas.drawRect(0f, frameRect.bottom.toFloat(), width.toFloat(), height.toFloat(), paint)
  }

  private fun drawPoints(canvas: Canvas) {
    if (possiblePoints.isEmpty()) return
    paint.color = params.possiblePointColor
    paint.style = Paint.Style.FILL
    val current = System.currentTimeMillis()
    val duration = params.possiblePointAliveDuration
    while (current - (possiblePoints.peek()?.foundTime ?: current) >= duration) {
      possiblePoints.poll()
    }
    if (possiblePoints.isEmpty()) return
    for (i in possiblePoints.indices) {
      val point = possiblePoints[i]
      val radius = 5f * (duration - current + point.foundTime) / duration
      if (radius > 0f) {
        canvas.drawCircle(frameRect.left + point.x, frameRect.top + point.y, radius, paint)
      }
    }
  }

  private fun drawDrawable(canvas: Canvas) {
    scannerDrawable?.apply {
      val now = System.currentTimeMillis()
      if (startTime < 0) {
        startTime = now
      }
      val timePast = ((now - startTime) % params.scanDuration).toInt()
      if (timePast > 0 && timePast <= params.scanDuration / 2) {
        val scannerShift = frameRect.height() * 2 * timePast / params.scanDuration
        canvas.save()
        canvas.clipRect(frameRect!!)
        setBounds(frameRect.left, frameRect.top + scannerShift, frameRect.right,
            frameRect.top + scannerHeight + scannerShift)
        draw(canvas)
        canvas.restore()
      }
    }

    frameDrawable?.draw(canvas)
  }

  private fun drawDesc(canvas: Canvas) {
    val desc = params.scanDesc ?: return
    paint.color = params.descTextColor
    paint.textSize = params.descTextSize
    canvas.drawText(desc, descX, descY, paint)
  }

  internal fun addPossibleResultPoint(point: ResultPoint) {
    val pp = PossiblePoint()
    pp.foundTime = System.currentTimeMillis()
    pp.x = point.x
    pp.y = point.y
    if (possiblePoints.size >= 10) {
      possiblePoints.poll()
    }
    possiblePoints.add(pp)
  }

  internal companion object {
    const val MASK_COLOR = -0x80000000
    const val POSSIBLE_POINT_COLOR = -0x3f000100
    const val POSSIBLE_POINT_ALIVE_DURATION = 200
    const val SCAN_DURATION = 1500
  }
}