package me.vistark.fastdroid.utils.keyboard

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class HideKeyboardExtension {
    companion object {
        fun Fragment.HideKeyboard() {
            view?.let { activity?.HideKeyboard(it) }
        }

        fun Activity.HideKeyboard() {
            HideKeyboard(currentFocus ?: View(this))
        }

        fun AppCompatActivity.HideKeyboard() {
            HideKeyboard(currentFocus ?: View(this))
        }

        fun View.HideKeyboard() {
            context.HideKeyboard(this)
        }


        fun Context.HideKeyboard(view: View) {
            val inputMethodManager =
                getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}