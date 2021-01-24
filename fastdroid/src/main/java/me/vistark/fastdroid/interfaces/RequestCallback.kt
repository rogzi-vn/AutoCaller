package me.vistark.fastdroid.interfaces

interface RequestCallback<T> {
    fun onResponse(response: T)
    fun onFailure(throwable: Throwable? = null)
}