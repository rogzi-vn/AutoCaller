package me.vistark.fastdroid.interfaces

interface IDeletable<T> {
    var onEdit: ((T) -> Unit)?
}