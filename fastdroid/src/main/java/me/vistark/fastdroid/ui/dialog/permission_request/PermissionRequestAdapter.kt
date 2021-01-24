package me.vistark.fastdroid.ui.dialog.permission_request

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.vistark.fastdroid.R
import me.vistark.fastdroid.core.models.RequirePermission

class PermissionRequestAdapter(val permissions: ArrayList<RequirePermission>) :
    RecyclerView.Adapter<PermissionRequestViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionRequestViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_permission_request, parent, false)
        return PermissionRequestViewHolder(v)
    }

    override fun onBindViewHolder(holder: PermissionRequestViewHolder, position: Int) {
        val permission = permissions[position]
        holder.bind(position + 1, permission)
    }

    override fun getItemCount(): Int {
        return permissions.size
    }
}