package me.vistark.fastdroid.interfaces

interface IClickable<T> {
    var onClick: ((T) -> Unit)?
}