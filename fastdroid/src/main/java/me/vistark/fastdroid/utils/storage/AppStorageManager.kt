package me.vistark.fastdroid.utils.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.lang.Exception

object AppStorageManager {
    var storageSP: SharedPreferences? = null
    fun initialize(context: Context) {
        if (storageSP == null)
            storageSP = context.getSharedPreferences("Storage", Context.MODE_PRIVATE)
    }

    inline fun <reified T> update(key: String, data: T) {
        storageSP?.edit()?.putString(key, data.toString())?.apply()
    }

    inline fun <reified T> get(key: String): T? {
        val value = storageSP?.getString(key, null)
        return try {
            when (T::class) {
                Int::class -> value?.toIntOrNull() as T
                Long::class -> value?.toLongOrNull() as T
                Boolean::class -> value?.toBoolean() as T
                Float::class -> value?.toFloatOrNull() as T
                Double::class -> value?.toDoubleOrNull() as T
                String::class -> value as T
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun <T> updateObject(key: String, data: T): Boolean {
        val sJson = Gson().toJson(data)
        return storageSP?.edit()?.putString(key, sJson)?.commit() ?: false
    }

    inline fun <reified T> getObject(key: String): T {
        val sJson = storageSP?.getString(key, "")
        return Gson().fromJson(sJson, T::class.java)
    }
}