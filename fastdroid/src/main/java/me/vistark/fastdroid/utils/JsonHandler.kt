package me.vistark.fastdroid.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object JsonHandler {
    // Chuyển đối tượng thành chuỗi Json
    inline fun <reified T> T.toJson(): String {
        return Gson().toJson(this)
    }

    // Từ chuỗi Json chuyển thành đối tượng có kiểu
    inline fun <reified T> String.toTyped(): T {
        val type = object : TypeToken<T>() {}.type
        return Gson().fromJson(this, type)
    }
}
