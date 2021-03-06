package vn.vistark.autocaller.ui.service_provider_list

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.vistark.autocaller.R
import vn.vistark.autocaller.ui.service_provider_list.phone_prefixs.ServiceProvider

class ServiceProviderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val cItemLnRoot: LinearLayout = v.findViewById(R.id.cItemLnRoot)
    val serviceProviderName: TextView = v.findViewById(R.id.serviceProviderName)
    val numberPrefixCount: TextView = v.findViewById(R.id.numberPrefixCount)
    val stateIcon: ImageView = v.findViewById(R.id.stateIcon)
    val phonePrefixs: TextView = v.findViewById(R.id.phonePrefixs)

    fun bind(sp: ServiceProvider) {
        serviceProviderName.text = sp.serviceProviderName
        var stateText = "Đang bật"
        var imgRes = R.drawable.checked
        if (!sp.state) {
            stateText = "Đang tắt"
            imgRes = R.drawable.unchecked
        }
        numberPrefixCount.text = "(${sp.phonePrefixs.size} đầu số - ${stateText})"
        stateIcon.setImageResource(imgRes)
        phonePrefixs.text = sp.phonePrefixs.joinToString(", ")
    }
}