package com.example.maskdetection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class OverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private val targets: MutableList<Pair<Rect, String>> = ArrayList()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        synchronized(this) {
            for (entry in targets) {
                val rect = entry.first
                val text = entry.second
                paint.color = when (text) {
                    "with_mask" -> Color.GREEN
                    "without_mask" -> Color.RED
                    else -> Color.BLUE
                }
                canvas.drawRect(rect, paint)
                canvas.drawText(text, rect.left.toFloat(), rect.top.toFloat(), paint)
            }
        }
    }

    fun setTargets(sources: List<Pair<Rect, String>>) {
        synchronized(this) {
            targets.clear()
            targets.addAll(sources)
            this.postInvalidate()
        }
    }

    init {
        val density = context.resources.displayMetrics.density
        paint.strokeWidth = 2.0f * density
        paint.color = Color.BLUE
        paint.style = Paint.Style.STROKE
    }
}
