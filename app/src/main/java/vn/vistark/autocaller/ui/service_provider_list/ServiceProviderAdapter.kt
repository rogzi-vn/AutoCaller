package vn.vistark.autocaller.ui.service_provider_list

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.ui.service_provider_list.phone_prefixs.PhonePrefixActivity
import vn.vistark.autocaller.ui.service_provider_list.phone_prefixs.ServiceProvider

class ServiceProviderAdapter : RecyclerView.Adapter<ServiceProviderViewHolder>() {
    var sps = AppStorage.ServiceProviders

    init {
        if (AppStorage.ServiceProviders.isNullOrEmpty()) {
            AppStorage.ServiceProviders = ServiceProvider.defaultServiceProviders
        }
        sps = AppStorage.ServiceProviders
    }

    var onLongClick: ((ServiceProvider) -> Unit)? = null
    var onClick: ((ServiceProvider) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceProviderViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout._service_provider_item, parent, false)
        return ServiceProviderViewHolder(v)
    }

    override fun onBindViewHolder(holder: ServiceProviderViewHolder, position: Int) {
        val sp = sps[position]
        holder.bind(sp)
        holder.cItemLnRoot.setOnLongClickListener {
            onLongClick?.invoke(sp)
            return@setOnLongClickListener true
        }
        holder.cItemLnRoot.setOnClickListener {
            onClick?.invoke(sp)
        }
    }

    override fun getItemCount(): Int {
        return sps.size
    }
}