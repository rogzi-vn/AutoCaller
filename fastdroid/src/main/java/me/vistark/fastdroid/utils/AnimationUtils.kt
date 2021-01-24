package me.vistark.fastdroid.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import me.vistark.fastdroid.utils.AnimationUtils.scaleDownBottomRight
import me.vistark.fastdroid.utils.AnimationUtils.scaleUpBottomRight

object AnimationUtils {
    //region Do on End
    fun Animation.doOnEnd(onEnd: () -> Unit) {
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                onEnd.invoke()
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
    }
    //endregion

    //region Scale down
    fun View.scaleDownBottomRight(duration: Long = 300L, onFinished: (() -> Unit)? = null) {
        val anim: Animation = ScaleAnimation(
            1f, 0f,  // Start and end values for the X axis scaling
            1f, 0f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 1f,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 1f
        ) // Pivot point of Y scaling

        // anim.fillAfter = true // Needed to keep the result of the animation

        anim.doOnEnd {
            this.visibility = View.GONE
            onFinished?.invoke()
        }

        anim.duration = duration
        this.startAnimation(anim)
    }

    fun View.scaleDownBottomLeft(duration: Long = 300L, onFinished: (() -> Unit)? = null) {
        val anim: Animation = ScaleAnimation(
            1f, 0f,  // Start and end values for the X axis scaling
            1f, 0f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0f,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 1f
        ) // Pivot point of Y scaling

        // anim.fillAfter = true // Needed to keep the result of the animation

        anim.doOnEnd {
            this.visibility = View.GONE
            onFinished?.invoke()
        }

        anim.duration = duration
        this.startAnimation(anim)
    }

    fun View.scaleDownTopLeft(duration: Long = 300L, onFinished: (() -> Unit)? = null) {
        val anim: Animation = ScaleAnimation(
            1f, 0f,  // Start and end values for the X axis scaling
            1f, 0f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0f,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 0f
        ) // Pivot point of Y scaling

        // anim.fillAfter = true // Needed to keep the result of the animation

        anim.doOnEnd {
            this.visibility = View.GONE
            onFinished?.invoke()
        }

        anim.duration = duration
        this.startAnimation(anim)
    }

    fun View.scaleDownTopRight(duration: Long = 300L, onFinished: (() -> Unit)? = null) {
        val anim: Animation = ScaleAnimation(
            1f, 0f,  // Start and end values for the X axis scaling
            1f, 0f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0f,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 1f
        ) // Pivot point of Y scaling

        // anim.fillAfter = true // Needed to keep the result of the animation

        anim.doOnEnd {
            this.visibility = View.GONE
            onFinished?.invoke()
        }

        anim.duration = duration
        this.startAnimation(anim)
    }
    //endregion

    //region Scale up
    fun View.scaleUpBottomRight(duration: Long = 300L, onFinished: (() -> Unit)? = null) {
        val anim: Animation = ScaleAnimation(
            0f, 1f,  // Start and end values for the X axis scaling
            0f, 1f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 1f,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 1f
        ) // Pivot point of Y scaling

        // anim.fillAfter = true // Needed to keep the result of the animation

        anim.doOnEnd {
            this.visibility = View.VISIBLE
            onFinished?.invoke()
        }

        anim.duration = duration
        this.startAnimation(anim)
    }

    fun View.scaleUpBottomLeft(duration: Long = 300L, onFinished: (() -> Unit)? = null) {
        val anim: Animation = ScaleAnimation(
            0f, 1f,  // Start and end values for the X axis scaling
            0f, 1f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0f,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 1f
        ) // Pivot point of Y scaling

        // anim.fillAfter = true // Needed to keep the result of the animation

        anim.doOnEnd {
            this.visibility = View.VISIBLE
            onFinished?.invoke()
        }

        anim.duration = duration
        this.startAnimation(anim)
    }

    fun View.scaleUpTopLeft(duration: Long = 300L, onFinished: (() -> Unit)? = null) {
        val anim: Animation = ScaleAnimation(
            0f, 1f,  // Start and end values for the X axis scaling
            0f, 1f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0f,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 0f
        ) // Pivot point of Y scaling

        // anim.fillAfter = true // Needed to keep the result of the animation

        anim.doOnEnd {
            this.visibility = View.VISIBLE
            onFinished?.invoke()
        }

        anim.duration = duration
        this.startAnimation(anim)
    }

    fun View.scaleUpTopRight(duration: Long = 300L, onFinished: (() -> Unit)? = null) {
        val anim: Animation = ScaleAnimation(
            0f, 1f,  // Start and end values for the X axis scaling
            0f, 1f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0f,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 1f
        ) // Pivot point of Y scaling

        // anim.fillAfter = true // Needed to keep the result of the animation

        anim.doOnEnd {
            this.visibility = View.VISIBLE
            onFinished?.invoke()
        }

        anim.duration = duration
        this.startAnimation(anim)
    }

    fun View.scaleUpCenter(duration: Long = 300L, onFinished: (() -> Unit)? = null) {
        val anim: Animation = ScaleAnimation(
            0f, 1f,  // Start and end values for the X axis scaling
            0f, 1f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0.5f,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 0.5f
        ) // Pivot point of Y scaling

        // anim.fillAfter = true // Needed to keep the result of the animation

        anim.doOnEnd {
            this.visibility = View.VISIBLE
            onFinished?.invoke()
        }

        anim.duration = duration
        this.startAnimation(anim)
    }

    fun View.scaleDownCenter(duration: Long = 300L, onFinished: (() -> Unit)? = null) {
        val anim: Animation = ScaleAnimation(
            1f, 0f,  // Start and end values for the X axis scaling
            1f, 0f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0.5f,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 0.5f
        ) // Pivot point of Y scaling

        // anim.fillAfter = true // Needed to keep the result of the animation

        anim.doOnEnd {
            this.visibility = View.GONE
            onFinished?.invoke()
        }

        anim.duration = duration
        this.startAnimation(anim)
    }

    //endregion
}