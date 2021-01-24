package me.vistark.fastdroid.utils

object ConvertUtils {
    inline fun <reified T> String.read(): T {
        return when (T::class) {
            Int::class -> toInt() as T
            Long::class -> toLong() as T
            Boolean::class -> toBoolean() as T
            String::class -> this as T
            // add other types here if need
            else -> throw IllegalStateException("Unknown Generic Type")
        }
    }

    fun Boolean.toInt(): Int {
        return if (this) 1 else 0
    }
}