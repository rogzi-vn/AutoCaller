package me.vistark.fastdroid.utils

import android.os.Handler
import android.os.Looper

object TimerUtils {
    fun startAfter(delayedTime: Long, func: () -> Unit) {
        Handler(Looper.myLooper()!!).postDelayed({
            func.invoke()
        }, delayedTime)
    }
}