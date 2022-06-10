package com.example.picbook

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.*
import androidx.appcompat.widget.AppCompatImageView

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

    /**
     *  zoomMode:
     *  0 -> image not zoomed and not translated,
     *  1 -> image translated,
     *  2 -> image zoomed and not translated
     */
    var zoomMode = 0

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
        myGestureDetector = GestureDetector(context, this)
        myMatrix=Matrix()
        matrixValue=FloatArray(10)
        imageMatrix = myMatrix
        scaleType = ScaleType.MATRIX
        setOnTouchListener(this)
    }

    /**
     * Reset zoom scale to original value and set the bitmap
     */
    override fun setImageBitmap(bm: Bitmap?) {
        presentScale = 1f
        super.setImageBitmap(bm)
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
    }

    /**
     * Center image within the view while zooming in and out
     */
    fun fittedTranslation() {
        myMatrix!!.getValues(matrixValue)

        val translationX = matrixValue!![Matrix.MTRANS_X]
        val translationY = matrixValue!![Matrix.MTRANS_Y]

        val fittedTransX = getFittedTranslation(translationX, mViewedWidth.toFloat(), originalWidth * presentScale)
        val fittedTransY = getFittedTranslation(translationY, mViewedHeight.toFloat(), originalHeight * presentScale)
        if (fittedTransX != 0f || fittedTransY != 0f)
            myMatrix!!.postTranslate(fittedTransX, fittedTransY)
    }

    private fun getFittedTranslation(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) { //Image is not zoomed
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {    //Image is zoomed
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

    /**
     * Image is fitted into the parent layout when is not zoomed
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewedWidth = MeasureSpec.getSize(widthMeasureSpec)
        mViewedHeight = MeasureSpec.getSize(heightMeasureSpec)
        if(presentScale == 1f){
            val factor: Float
            val mDrawable = drawable
            if (mDrawable == null || mDrawable.intrinsicWidth == 0 || mDrawable.intrinsicHeight == 0) return
            val drawableWidth = mDrawable.intrinsicWidth
            val drawableHeight = mDrawable.intrinsicHeight
            val factorX = mViewedWidth.toFloat() / drawableWidth.toFloat()
            val factorY = mViewedHeight.toFloat() / drawableHeight.toFloat()
            factor = factorX.coerceAtMost(factorY)
            myMatrix!!.setScale(factor, factor)

            //Center image to his view
            var repeatedYSpace = (mViewedHeight.toFloat() - factor * drawableHeight.toFloat())
            var repeatedXSpace = (mViewedWidth.toFloat() - factor * drawableWidth.toFloat())
            repeatedYSpace /= 2f
            repeatedXSpace /= 2f
            myMatrix!!.postTranslate(repeatedXSpace, repeatedYSpace)
            originalWidth = mViewedWidth - 2 * repeatedXSpace
            originalHeight = mViewedHeight - 2 * repeatedYSpace
            imageMatrix = myMatrix
        }
    }

    override fun onTouch(mView: View, event: MotionEvent): Boolean {
        myScaleDetector!!.onTouchEvent(event)
        myGestureDetector!!.onTouchEvent(event)

        //Display point where user touch
        val currentPoint = PointF(event.x, event.y)

        val mLayoutParams = this.layoutParams
        //Get parent dimensions, in this case the ImageView container layout
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
        //set ImageView matrix with updated values
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