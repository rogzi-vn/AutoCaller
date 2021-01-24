package me.vistark.fastdroid.ui.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import me.vistark.fastdroid.R


object LoadingBase {
    fun Context.showLoadingBase(shortMessage: String = ""): AlertDialog {
        val v = LayoutInflater.from(this)
            .inflate(R.layout.alert_loading_overlay, null)

        val loadigMessage: TextView = v.findViewById(R.id.aloTvLoadingMessage)
        val loadigImage: ImageView = v.findViewById(R.id.aloIvLoadingOverlayIcon)

        // Nếu tin nhắn nhập vào khác trống
        if (shortMessage.isNotEmpty()) {
            loadigMessage.text = shortMessage
        }

        Glide.with(this).load(R.raw.loading_pink).into(loadigImage)

        val mBuilder = AlertDialog
            .Builder(this)
            .setView(v)

        val mAlertDialog = mBuilder.show()
        // Khiến cho dialog wrap content

        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mAlertDialog.setCancelable(false)

        return mAlertDialog
    }
}