package com.example.picbook

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

class TouchImageView constructor(
    context: Context,
    attrs: AttributeSet
    ): AppCompatImageView(context, attrs) {

    private var scaleFactor = 1.0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener(){

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            scaleFactor *= scaleGestureDetector.scaleFactor
            Log.v("TAG", "$scaleFactor")
            //Prevents image become too small
            scaleFactor = max(1.0f, min(scaleFactor, 10.0f))

            scaleX = scaleFactor
            scaleY = scaleFactor

            return true
        }

    }

    private val scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, scaleListener)
}