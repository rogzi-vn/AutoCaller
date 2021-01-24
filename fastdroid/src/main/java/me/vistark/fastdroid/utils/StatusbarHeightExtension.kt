package me.vistark.fastdroid.utils

import android.content.Context

object StatusbarHeightExtension {
    val Context.statusBarHeight: Int
        get() {
            var result = 0
            try {
                val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
                if (resourceId > 0)
                    result = resources.getDimensionPixelSize(resourceId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }
}