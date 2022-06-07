package com.example.picbook

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

class ZoomImageView constructor(
    context: Context,
    attrs: AttributeSet?
): AppCompatImageView(context, attrs), View.OnTouchListener, GestureDetector.OnGestureListener{
    //Construction details
    private var myContext: Context? = null
    private var myScaleDetector: ScaleGestureDetector? = null
    private var myGestureDetector: GestureDetector? = null
    var myMatrix: Matrix? = null
    private var matrixValue: FloatArray? = null
    var zoomMode = 0

    // required Scales
    var presentScale = 1f
    var minScale = 1f
    var maxScale = 15f

    //Dimensions
    private var originalWidth = 0f
    private var originalHeight = 0f
    private var mViewedWidth = 0
    private var mViewedHeight = 0
    private var lastPoint = PointF()
    private var startPoint = PointF()

    init{
        super.setClickable(true)
        myContext=context
        myScaleDetector= ScaleGestureDetector(context,ScalingListener())
        myMatrix=Matrix()
        matrixValue=FloatArray(10)
        imageMatrix = myMatrix
        scaleType = ScaleType.MATRIX
        myGestureDetector = GestureDetector(context, this)
        setOnTouchListener(this)
    }

    /**
     * If user zoom an image, next will maintain the previous scale.
     * Every time an image is loaded, presentScale is set to default value.
     */
    fun resetPresentScale(){
        presentScale = 1f
    }
    private inner class ScalingListener : ScaleGestureDetector.SimpleOnScaleGestureListener(){
        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            zoomMode = 2
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            val prevScale = mScaleFactor
            presentScale*=mScaleFactor
            if (presentScale > maxScale) {
                presentScale = maxScale
                mScaleFactor = maxScale / prevScale
            }else if (presentScale < minScale) {
                presentScale = minScale
                mScaleFactor = minScale / prevScale
            }

            myMatrix!!.postScale(mScaleFactor, mScaleFactor, detector.focusX, detector.focusY)
            fittedTranslation()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            super.onScaleEnd(detector)
        }
    }

    fun fittedTranslation() {
        myMatrix!!.getValues(matrixValue)
        val translationX =
            matrixValue!![Matrix.MTRANS_X]
        val translationY =
            matrixValue!![Matrix.MTRANS_Y]
        val fittedTransX = getFittedTranslation(translationX, mViewedWidth.toFloat(), originalWidth * presentScale)
        val fittedTransY = getFittedTranslation(translationY, mViewedHeight.toFloat(), originalHeight * presentScale)
        if (fittedTransX != 0f || fittedTransY != 0f)
            myMatrix!!.postTranslate(fittedTransX, fittedTransY)

    }

    /**
     * Handle image negative coordinates and the case when image is not zoomed
     */
    private fun getFittedTranslation(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }
        if (trans < minTrans) return -trans + minTrans
        return if(trans > maxTrans) -trans + maxTrans else 0f
    }
    private fun getFixDragTrans(delta: Float, viewedSize: Float, detailSize: Float): Float {
        return if (detailSize <= viewedSize) {
            0F
        } else delta
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewedWidth = MeasureSpec.getSize(widthMeasureSpec)
        mViewedHeight = MeasureSpec.getSize(heightMeasureSpec)
        if(presentScale == 1f){
            val factor: Float
            val mDrawable = drawable
            if (mDrawable == null || mDrawable.intrinsicWidth == 0 || mDrawable.intrinsicHeight == 0) return
            val mImageWidth = mDrawable.intrinsicWidth
            val mImageHeight = mDrawable.intrinsicHeight
            val factorX = mViewedWidth.toFloat() / mImageWidth.toFloat()
            val factorY = mViewedHeight.toFloat() / mImageHeight.toFloat()
            factor = factorX.coerceAtMost(factorY)
            myMatrix!!.setScale(factor, factor)

            // Centering the image
            var repeatedYSpace = (mViewedHeight.toFloat()
                    - factor * mImageHeight.toFloat())
            var repeatedXSpace = (mViewedWidth.toFloat()
                    - factor * mImageWidth.toFloat())
            repeatedYSpace /= 2.toFloat()
            repeatedXSpace /= 2.toFloat()
            myMatrix!!.postTranslate(repeatedXSpace, repeatedYSpace)
            originalWidth = mViewedWidth - 2 * repeatedXSpace
            originalHeight = mViewedHeight - 2 * repeatedYSpace
            imageMatrix = myMatrix
        }

    }

    private fun setViewSize(mode: Int, size: Int, drawableWidth: Int): Int{
        return when(mode){
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> min(drawableWidth, size)
            MeasureSpec.UNSPECIFIED -> drawableWidth
            else -> size
        }
    }

    override fun onTouch(mView: View, event: MotionEvent): Boolean {
        myScaleDetector!!.onTouchEvent(event)
        myGestureDetector!!.onTouchEvent(event)
        val currentPoint = PointF(event.x, event.y)

        val mLayoutParams = this.layoutParams
        mLayoutParams.width = (this.parent as View).width
        mLayoutParams.height = (this.parent as View).height
        this.layoutParams = mLayoutParams

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastPoint.set(currentPoint)
                startPoint.set(lastPoint)
                zoomMode = 1
            }
            MotionEvent.ACTION_MOVE -> if (zoomMode == 1) {
                val changeInX = currentPoint.x - lastPoint.x
                val changeInY = currentPoint.y - lastPoint.y
                val fixedTranslationX = getFixDragTrans(changeInX, mViewedWidth.toFloat(), originalWidth * presentScale)
                val fixedTranslationY = getFixDragTrans(changeInY, mViewedHeight.toFloat(), originalHeight * presentScale)
                myMatrix!!.postTranslate(fixedTranslationX, fixedTranslationY)
                fittedTranslation()
                lastPoint[currentPoint.x] = currentPoint.y
            }
            MotionEvent.ACTION_POINTER_UP -> zoomMode = 0
        }
        imageMatrix = myMatrix
        return false
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        return false
    }
    override fun onShowPress(p0: MotionEvent?) {}
    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return false
    }
    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return false
    }
    override fun onLongPress(p0: MotionEvent?) {}
    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return false
    }
}
