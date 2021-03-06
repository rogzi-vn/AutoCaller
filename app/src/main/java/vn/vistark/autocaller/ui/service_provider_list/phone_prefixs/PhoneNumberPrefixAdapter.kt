package vn.vistark.autocaller.ui.service_provider_list.phone_prefixs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.vistark.autocaller.R

class PhoneNumberPrefixAdapter(var prefixs: ArrayList<String>) :
    RecyclerView.Adapter<PhonePrefixViewHolder>() {

    var onClick: ((String) -> Unit)? = null
    var onLongClick: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhonePrefixViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout._phone_prefix_item, parent, false)
        return PhonePrefixViewHolder(v)
    }

    override fun onBindViewHolder(holder: PhonePrefixViewHolder, position: Int) {
        holder.bind(prefixs[position])
        holder.removeNumberPrefixIcon.setOnClickListener {
            onClick?.invoke(prefixs[position])
        }
        holder.removeNumberPrefixIcon.setOnLongClickListener {
            onLongClick?.invoke(prefixs[position])
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return prefixs.size
    }
}