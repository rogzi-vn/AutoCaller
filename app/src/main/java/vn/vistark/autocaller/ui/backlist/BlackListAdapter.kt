package vn.vistark.autocaller.ui.backlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.BlackListModel

class BlackListAdapter(val blacklists: ArrayList<BlackListModel>) :
    RecyclerView.Adapter<BlackListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlackListViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout._campaign_data_item, parent, false)
        return BlackListViewHolder(v)
    }

    override fun onBindViewHolder(holder: BlackListViewHolder, position: Int) {
        val crrBl = blacklists[position]
        holder.bind(position, crrBl)
    }

    override fun getItemCount(): Int {
        return blacklists.size
    }

}