package me.vistark.fastdroid.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import me.vistark.fastdroid.language.LanguageConfig
import java.util.*

class FastdroidContextWrapper(base: Context?) : ContextWrapper(base) {
    companion object {
        fun AppCompatActivity.forOnCreate() {
            val context: Context = wrap(this, LanguageConfig.LanguageCode)
            resources.updateConfiguration(
                context.resources.configuration,
                context.resources.displayMetrics
            )
        }

        fun wrap(_context: Context, language: String): ContextWrapper {
            var context = _context
            val config = context.resources.configuration
            var sysLocale: Locale? = null
            sysLocale = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                getSystemLocale(config)
            } else {
                getSystemLocaleLegacy(config)
            }
            if (language != "" && sysLocale.language != language) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setSystemLocale(config, locale)
                } else {
                    setSystemLocaleLegacy(config, locale)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context = context.createConfigurationContext(config)
            } else {
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
            }
            return ContextWrapper(context)
        }

        fun getSystemLocaleLegacy(config: Configuration): Locale {
            return config.locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun getSystemLocale(config: Configuration): Locale {
            return config.locales[0]
        }

        fun setSystemLocaleLegacy(config: Configuration, locale: Locale?) {
            config.locale = locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun setSystemLocale(config: Configuration, locale: Locale?) {
            config.setLocale(locale)
        }
    }
}