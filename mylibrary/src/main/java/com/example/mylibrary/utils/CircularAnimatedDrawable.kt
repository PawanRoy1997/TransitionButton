package com.example.mylibrary.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.Property
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

private val ANGLE_INTERPOLATOR: Interpolator = LinearInterpolator()
private val SWEEP_INTERPOLATOR: Interpolator = DecelerateInterpolator()

private const val ANGLE_ANIMATOR_DURATION: Long = 2000
private const val SWEEP_ANIMATOR_DURATION: Long = 600

const val MIN_SWEEP_ANGLE = 30
private val fBounds = RectF()

class CircularAnimatedDrawable(color: Int, private val borderWidth: Float) : Drawable(),
    Animatable {

    private var mCurrentGlobalAngleOffset = 0f
    private var mCurrentGlobalAngle = 0f
    private var mCurrentSweepAngle = 0f


    private lateinit var mObjectAnimatorSweep: ObjectAnimator
    private lateinit var mObjectAnimatorAngle: ObjectAnimator
    private var mModeAppearing = false
    private val mPaint: Paint = Paint().apply {
        this.isAntiAlias = true
        this.style = Paint.Style.STROKE
        this.strokeWidth = borderWidth
        this.color = color
    }
    private var mRunning = false

    init {
        setupAnimations()
    }

    override fun draw(canvas: Canvas) {
        var startAngle = mCurrentGlobalAngle - mCurrentGlobalAngleOffset
        var sweepAngle = mCurrentSweepAngle
        if (!mModeAppearing) {
            startAngle += sweepAngle
            sweepAngle = 360 - sweepAngle - MIN_SWEEP_ANGLE
        } else {
            sweepAngle += MIN_SWEEP_ANGLE.toFloat()
        }
        canvas.drawArc(fBounds, startAngle, sweepAngle, false, mPaint)
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    private fun toggleAppearingMode() {
        mModeAppearing = !mModeAppearing
        if (mModeAppearing) {
            mCurrentGlobalAngleOffset = (mCurrentGlobalAngleOffset + MIN_SWEEP_ANGLE * 2) % 360
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        fBounds.left = bounds.left + borderWidth / 2f + .5f
        fBounds.right = bounds.right - borderWidth / 2f - .5f
        fBounds.top = bounds.top + borderWidth / 2f + .5f
        fBounds.bottom = bounds.bottom - borderWidth / 2f - .5f
    }

    private val mAngleProperty: Property<CircularAnimatedDrawable, Float> =
        object : Property<CircularAnimatedDrawable, Float>(
            Float::class.java, "angle"
        ) {
            override fun get(circularAnimDrawable: CircularAnimatedDrawable): Float {
                return circularAnimDrawable.getCurrentGlobalAngle()
            }

            override fun set(circularAnimDrawable: CircularAnimatedDrawable, value: Float) {
                circularAnimDrawable.setCurrentGlobalAngle(value)
            }
        }

    private val mSweepProperty: Property<CircularAnimatedDrawable, Float> =
        object : Property<CircularAnimatedDrawable, Float>(
            Float::class.java, "arc"
        ) {
            override fun get(circularAnimDrawable: CircularAnimatedDrawable): Float {
                return circularAnimDrawable.getCurrentSweepAngle()
            }

            override fun set(circularAnimDrawable: CircularAnimatedDrawable, value: Float) {
                circularAnimDrawable.setCurrentSweepAngle(value)
            }
        }

    private fun setupAnimations() {
        mObjectAnimatorAngle = ObjectAnimator.ofFloat(this, mAngleProperty, 360f).apply {
            this.interpolator = ANGLE_INTERPOLATOR
            this.duration = ANGLE_ANIMATOR_DURATION
            this.repeatMode = ValueAnimator.RESTART
            this.repeatCount = ValueAnimator.INFINITE
        }
        mObjectAnimatorSweep =
            ObjectAnimator.ofFloat(this, mSweepProperty, 360f - MIN_SWEEP_ANGLE * 2).apply {
                this.interpolator = SWEEP_INTERPOLATOR
                this.duration = SWEEP_ANIMATOR_DURATION
                this.repeatMode = ValueAnimator.RESTART
                this.repeatCount = ValueAnimator.INFINITE
            }
        mObjectAnimatorSweep.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {
                toggleAppearingMode()
            }
        })
    }

    override fun start() {
        if (!isRunning) {
            mRunning = true
            mObjectAnimatorAngle.start()
            mObjectAnimatorSweep.start()
            invalidateSelf()
        }
    }

    override fun stop() {
        if (isRunning) {
            mRunning = false
            mObjectAnimatorAngle.cancel()
            mObjectAnimatorSweep.cancel()
            invalidateSelf()
        }
    }

    override fun isRunning(): Boolean {
        return mRunning
    }

    fun setCurrentGlobalAngle(currentGlobalAngle: Float) {
        mCurrentGlobalAngle = currentGlobalAngle
        invalidateSelf()
    }

    fun getCurrentGlobalAngle(): Float {
        return mCurrentGlobalAngle
    }

    fun setCurrentSweepAngle(currentSweepAngle: Float) {
        mCurrentSweepAngle = currentSweepAngle
        invalidateSelf()
    }

    fun getCurrentSweepAngle(): Float {
        return mCurrentSweepAngle
    }

}