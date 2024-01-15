package com.example.celebrare.Model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View

class TextBox(context: Context) : View(context) {

    private var text = ""
    private var x = 100f
    private var y = 100f
    private var paint: Paint = Paint()

    init {
        paint.textSize = 30f
    }

    fun setText(text: String) {
        this.text = text
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText(text, x, y, paint)
    }

    fun setCoordinates(x: Float, y: Float) {
        this.x = x
        this.y = y
        invalidate()
    }

    fun getCoordinates(): Pair<Float, Float> {
        return Pair(x, y)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                x = event.x
                y = event.y
                invalidate()
            }
        }
        return true
    }
}
