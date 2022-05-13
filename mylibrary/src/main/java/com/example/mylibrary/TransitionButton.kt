package com.example.mylibrary

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.AttributeSet
import android.util.TypedValue
import android.view.animation.Animation
import android.view.animation.CycleInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.mylibrary.utils.WindowUtils

private const val WIDTH_ANIMATION_DURATION: Long = 200
private const val SCALE_ANIMATION_DURATION: Long = 300
private const val SHAKE_ANIMATION_DURATION: Long = 500
private const val COLOR_ANIMATION_DURATION: Long = 350

class TransitionButton : AppCompatButton {

    private var messageAnimationDuration = COLOR_ANIMATION_DURATION * 10
    private var currentState: State? = null

    private var isMorphingInProgress = false

    private var initialWidth = 0
    private var initialHeight = 0
    private var initialText: String? = null

    private var defaultColor = 0
    private var errorColor = 0
    private var loaderColor = 0

    private var progressCircularAnimatedDrawable: CircularAnimatedDrawable? = null

    constructor (context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        currentState = State.IDLE
        errorColor = ContextCompat.getColor(getContext(), R.color.colorError)
        loaderColor = ContextCompat.getColor(getContext(), R.color.colorLoader)
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true)
        defaultColor = typedValue.data
        if (attrs != null) {
            val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.TransitionButton)
            defaultColor = attrsArray.getColor(R.styleable.TransitionButton_defaultColor, resources.getColor(R.color.defaultColor))
            loaderColor = attrsArray.getColor(R.styleable.TransitionButton_loaderColor, resources.getColor(R.color.defaultLoaderColor))
            attrsArray.recycle()
        }
        backgroundTintList = ColorStateList.valueOf(defaultColor)
        val background: Drawable =
            ContextCompat.getDrawable(context, R.drawable.transition_button_shape_idle)!!
        setBackground(background)
    }

    fun startAnimation() {
        currentState = State.PROGRESS
        isMorphingInProgress = true
        initialWidth = width
        initialHeight = height
        initialText = text.toString()
        text = null
        isClickable = false
        startWidthAnimation(initialHeight, object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationCancel(animation)
                isMorphingInProgress = false
            }
        })
    }

    fun setMessageAnimationDuration(messageAnimationDuration: Long) {
        this.messageAnimationDuration = messageAnimationDuration
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (currentState == State.PROGRESS && !isMorphingInProgress) {
            drawIndeterminateProgress(canvas)
        }
    }

    private fun drawIndeterminateProgress(canvas: Canvas) {
        if (progressCircularAnimatedDrawable == null || !progressCircularAnimatedDrawable!!.isRunning) {
            val arcWidth = height / 18
            progressCircularAnimatedDrawable =
                CircularAnimatedDrawable(loaderColor, arcWidth.toFloat())
            val offset = (width - height) / 2
            val right = width - offset
            val bottom = height
            val top = 0
            progressCircularAnimatedDrawable!!.setBounds(offset, top, right, bottom)
            progressCircularAnimatedDrawable!!.callback = this
            progressCircularAnimatedDrawable!!.start()
        } else {
            progressCircularAnimatedDrawable!!.draw(canvas)
            invalidate()
        }
    }

    fun stopAnimation(
        stopAnimationStyle: StopAnimationStyle?,
        onAnimationStopEndListener: OnAnimationStopEndListener?
    ) {
        when (stopAnimationStyle) {
            StopAnimationStyle.SHAKE -> {
                currentState = State.ERROR
                startWidthAnimation(initialWidth, object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        text = initialText
                        startShakeAnimation(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {}

                            override fun onAnimationEnd(animation: Animation?) {
                                currentState = State.IDLE
                                isClickable = true
                                onAnimationStopEndListener?.onAnimationStopEnd()
                            }

                            override fun onAnimationRepeat(animation: Animation?) {}
                        })
                    }
                })
            }
            StopAnimationStyle.EXPAND -> {
                currentState = State.TRANSITION
                startScaleAnimation(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation) {
                        onAnimationStopEndListener?.onAnimationStopEnd()
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
        }
    }

    private fun startWidthAnimation(to: Int, onAnimationEnd: AnimatorListenerAdapter) {
        startWidthAnimation(width, to, onAnimationEnd)
    }

    private fun startWidthAnimation(from: Int, to: Int, onAnimationEnd: AnimatorListenerAdapter?) {
        val widthAnimation = ValueAnimator.ofInt(from, to)
        widthAnimation.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = layoutParams
            layoutParams.width = `val`
            setLayoutParams(layoutParams)
        }
        val animatorSet = AnimatorSet()
        animatorSet.duration = WIDTH_ANIMATION_DURATION
        animatorSet.playTogether(widthAnimation)
        if (onAnimationEnd != null) animatorSet.addListener(onAnimationEnd)
        animatorSet.start()
    }

    private fun startShakeAnimation(animationListener: Animation.AnimationListener) {
        val shake = TranslateAnimation(0f, 15f, 0f, 0f)
        shake.duration = SHAKE_ANIMATION_DURATION
        shake.interpolator = CycleInterpolator(4f)
        shake.setAnimationListener(animationListener)
        startAnimation(shake)
    }

    private fun startScaleAnimation(animationListener: Animation.AnimationListener) {
        val ts = (WindowUtils.getHeight(context) / height * 2.1).toFloat()
        val anim: Animation = ScaleAnimation(
            1f, ts,
            1f, ts,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        anim.duration = SCALE_ANIMATION_DURATION
        anim.fillAfter = true
        anim.setAnimationListener(animationListener)
        startAnimation(anim)
    }

    fun showErrorMessage(message: String?) {
        text = message
        isClickable = false
        startColorAnimation(defaultColor, errorColor)
        Handler().postDelayed({
            text = initialText
            isClickable = true
            startColorAnimation(errorColor, defaultColor)
        }, messageAnimationDuration)
    }

    private fun startColorAnimation(from: Int, to: Int) {
        val anim = ValueAnimator.ofArgb(from, to)
        anim.addUpdateListener { valueAnimator ->
            backgroundTintList = ColorStateList.valueOf((valueAnimator.animatedValue as Int))
            refreshDrawableState()
        }
        anim.duration = COLOR_ANIMATION_DURATION
        anim.start()
    }

    interface OnAnimationStopEndListener {
        fun onAnimationStopEnd()
    }

    private enum class State {
        PROGRESS, IDLE, ERROR, TRANSITION
    }

    enum class StopAnimationStyle {
        EXPAND, SHAKE
    }

}