package vn.vistark.autocaller.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object AppStorageManager {
    var storageSP: SharedPreferences? = null
    fun initialize(context: Context) {
        if (storageSP == null)
            storageSP = context.getSharedPreferences("Storage", Context.MODE_PRIVATE)
    }

    inline fun <reified T> isObject(): Boolean {
        return T::class != Int::class &&
                T::class != Long::class &&
                T::class != Double::class &&
                T::class != String::class &&
                T::class != Boolean::class &&
                T::class != Float::class
    }

    @SuppressLint("ApplySharedPref")
    inline fun <reified T> update(key: String, data: T) {
        if (isObject<T>()) {
            updateObject(key, data)
            return
        }
//        println("[APP_STORAGE SAVE]: ${data.toString()}")
        storageSP!!.edit().putString(key, data.toString()).commit()
    }

    inline fun <reified T> get(key: String): T? {
        if (isObject<T>()) {
            return getObject(key)
        }
        val value = storageSP!!.getString(key, null)
//        println("[APP_STORAGE GET]: $value")
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
//        println("[APP_STORAGE SAVE]: $sJson")
        return storageSP!!.edit().putString(key, sJson).commit()
    }

    inline fun <reified T> getObject(key: String): T {
        val sJson = storageSP!!.getString(key, "")
//        println("[APP_STORAGE GET]: $sJson")

        if (T::class == ArrayList::class) {
            // Không hỗ trợ ArrayList
            throw Exception("Please use Array instead")
        } else {
            return Gson().fromJson(sJson, T::class.java)
        }
    }
}