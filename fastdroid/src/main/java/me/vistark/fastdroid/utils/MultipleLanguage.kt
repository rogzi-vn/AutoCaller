package me.vistark.fastdroid.utils

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView

object MultipleLanguage {
    fun L(key: String): String {
        if (key.isEmpty())
            return ""
        return "[$key]"
    }

    fun View.autoTranslate() {
        // Nếu là Edittext thì tự động chuyển đổi hint và giữ nguyên nội dung
        if (this is EditText) {
            this.hint = L(this.hint.toString())
            return
        }

        // Nếu nó là TextView thì tự động chuyển đổi trên văn bản
        if (this is TextView) {
            this.text = L(this.text.toString())
            try {
                this.hint = L(this.hint.toString())
            } catch (e: Exception) {
            }
            return
        }

        //Nếu là một nhóm layout thì đệ quy vào các phần tử con
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                getChildAt(i).autoTranslate()
            }
        }
    }
}