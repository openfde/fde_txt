package com.fde.txt.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.fde.txt.MainActivity
import com.fde.txt.R

class VerticalDragScrollBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), IScrollBar {
    private val TAG = "VerticalDragScrollBar"
    private var isHoverScrollBar = false
    private var isDragScrollBar = false
    private var isFull = false
    private var allLength = 0
    private val rect = Rect()
    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.scrollbar_thumb)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private var lastRawY = 0F
    lateinit var scrollListener: MainActivity.Companion.ScrollListener

    override fun updateData(scrollLength: Int, width: Int, height: Int, allLength: Int) {
        val scrollBarY = height * scrollLength / allLength
        val scrollBarLength = height * height / allLength
        this.allLength = allLength
//        Log.w(
//            TAG,
//            "scrollLength = $scrollLength, width = $width, height = $height, allLength = $allLength"
//        )
//        Log.w(
//            TAG,
//            "scrollBarY = $scrollBarY, scrollBarLength = $scrollBarLength, scrollBarBottom = ${scrollBarY + scrollBarLength}"
//        )
        isFull = height >= allLength
//        Log.w(TAG, "isFull = $isFull")
        // If the last calculated position is a decimal, it may not fill the scroll track,
        // and you need to add one to make it fill completely
        rect.set(0, scrollBarY, measuredWidth, scrollBarY + scrollBarLength + 1)
    }

    override fun startTouch(event: MotionEvent?) {
        if (event == null) return
        isDragScrollBar = rect.contains(event.x.toInt(), event.y.toInt())
        invalidate()
    }

    override fun endTouch(event: MotionEvent?) {
        if (event == null) return
        isDragScrollBar = false
        isHoverScrollBar = rect.contains(event.x.toInt(), event.y.toInt())
        invalidate()
    }

    override fun startScroll() {
        TODO("Not yet implemented")
    }

    override fun needDrag(event: MotionEvent?) = isDragScrollBar

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                startTouch(event)
                lastRawY = event.rawY
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragScrollBar) {
                    scrollListener.scrollYBy(((event.rawY - lastRawY) * this.allLength / measuredHeight).toInt())
                    endTouch(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragScrollBar) {
                    scrollListener.scrollYBy(((event.rawY - lastRawY) * this.allLength / measuredHeight).toInt())
                    lastRawY = event.rawY
                }
            }
        }
        return true
    }

    override fun onHoverEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_HOVER_ENTER, MotionEvent.ACTION_HOVER_MOVE, MotionEvent.ACTION_HOVER_EXIT -> isHoverScrollBar =
                rect.contains(event.x.toInt(), event.y.toInt())
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isDragScrollBar) paint.color =
            ContextCompat.getColor(context, R.color.scrollbar_thumb_selected)
        else if (isHoverScrollBar) paint.color =
            ContextCompat.getColor(context, R.color.scrollbar_thumb_hovered)
        else paint.color = ContextCompat.getColor(context, R.color.scrollbar_thumb)
        if(!isFull) canvas?.drawRect(rect, paint)
    }
}