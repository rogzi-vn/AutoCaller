package vn.vistark.autocaller.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class SPUtils {
    companion object {
        var sp: SharedPreferences? = null
        fun init(context:AppCompatActivity) {
            sp =
                context.getSharedPreferences(context.packageName.toUpperCase(Locale.ROOT), Context.MODE_PRIVATE)
        }
    }
}