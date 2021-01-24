package me.vistark.fastdroid.utils

import android.widget.EditText
import android.widget.TextView

object EdittextUtils {
    fun EditText.required(msg: String = "Please input " + this.id): String {
        if (this.text.toString().trim().isEmpty()) {
            this.error = msg
            return msg
        }
        return ""
    }

    fun TextView.required(msg: String = "Please input " + this.id): String {
        if (this.text.toString().trim().isEmpty()) {
            this.error = msg
            return msg
        }
        return ""
    }

    fun EditText.validate(
        regex: String,
        msg: String = "$id is invalid"
    ): String {
        if (regex.toRegex().matches(this.text.toString())) {
            return ""
        }
        this.error = msg
        return msg
    }
}