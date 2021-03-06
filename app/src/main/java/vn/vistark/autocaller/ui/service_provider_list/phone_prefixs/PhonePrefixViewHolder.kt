package vn.vistark.autocaller.ui.service_provider_list.phone_prefixs

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.vistark.autocaller.R

class PhonePrefixViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val phoneNumberPrefix: TextView = v.findViewById(R.id.phoneNumberPrefix)
    val removeNumberPrefixIcon: ImageView = v.findViewById(R.id.removeNumberPrefixIcon)

    fun bind(prefix: String) {
        phoneNumberPrefix.text = prefix
    }
}