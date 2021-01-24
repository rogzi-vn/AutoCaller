package me.vistark.fastdroid.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.core.content.ContextCompat


object VibrateUtils {
    fun Context.vibrate(millis: Long = 500) {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v?.vibrate(millis)
        }
    }

    fun View.vibrate(millis: Long = 500) {
        context.vibrate(millis)
    }
}