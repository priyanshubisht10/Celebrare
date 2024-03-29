package com.example.celebrare

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private lateinit var drawPath: FingerPath
    private lateinit var canvasPaint: Paint
    private lateinit var drawPaint: Paint
    private lateinit var canvas: Canvas
    private lateinit var canvasBitmap: Bitmap
    private var paths = mutableListOf<FingerPath>()
    private var undonePaths = mutableListOf<FingerPath>()

    init {
        setUpDrawing()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath = FingerPath()
                drawPath.moveTo(touchX!!, touchY!!)
                paths.add(drawPath)
                undonePaths.clear()
            }

            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(touchX!!, touchY!!)
            }

            MotionEvent.ACTION_UP -> {}

            else -> return false
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap, 0f, 0f, drawPaint)

        for (path in paths) {
            drawPaint.strokeWidth = path.brushThickness
            drawPaint.color = path.color
            canvas.drawPath(path.path, drawPaint)
        }
    }

    private fun setUpDrawing() {
        drawPaint = Paint()
        drawPath = FingerPath()
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND
        drawPaint.isAntiAlias = true
        drawPaint.isDither = true
        drawPaint.color = drawPath.color
        drawPaint.strokeWidth = drawPath.brushThickness
        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    fun undo() {
        if (paths.isNotEmpty()) {
            undonePaths.add(paths.removeAt(paths.size - 1))
            invalidate()
        }
    }

    fun redo() {
        if (undonePaths.isNotEmpty()) {
            paths.add(undonePaths.removeAt(undonePaths.size - 1))
            invalidate()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addTextView(text: String, linearLayout: LinearLayout) {
        val textView = TextView(context)
        textView.text = text
        textView.setTextColor(Color.BLACK)

        val centerX = width / 2f
        val centerY = height / 2f
        textView.x = centerX
        textView.y = centerY

        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        linearLayout.addView(textView, layoutParams)

        textView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    view.setTag(R.id.tag_initial_x, event.rawX - view.x)
                    view.setTag(R.id.tag_initial_y, event.rawY - view.y)
                }

                MotionEvent.ACTION_MOVE -> {
                    view.x = event.rawX - view.getTag(R.id.tag_initial_x) as Float
                    view.y = event.rawY - view.getTag(R.id.tag_initial_y) as Float
                }
            }
            true
        }

        Toast.makeText(context, "Text added to drawing view", Toast.LENGTH_SHORT).show()
    }


    internal inner class FingerPath {
        val color: Int
        val brushThickness: Float
        val path: Path = Path()

        init {
            color = Color.BLACK
            brushThickness = 20f
        }

        fun moveTo(x: Float, y: Float) {
            path.moveTo(x, y)
        }

        fun lineTo(x: Float, y: Float) {
            path.lineTo(x, y)
        }
    }
}
