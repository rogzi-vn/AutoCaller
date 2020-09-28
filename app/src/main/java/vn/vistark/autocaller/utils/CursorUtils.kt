package vn.vistark.autocaller.utils

import android.database.Cursor

fun Cursor.getString(columnName: String): String {
    val colIndex = this.getColumnIndex(columnName)
    return this.getString(colIndex)
}

fun Cursor.getInt(columnName: String): Int {
    val colIndex = this.getColumnIndex(columnName)
    return this.getInt(colIndex)
}

fun Cursor.getBoolean(columnName: String): Boolean {
    val colIndex = this.getColumnIndex(columnName)
    return this.getInt(colIndex) == 1
}