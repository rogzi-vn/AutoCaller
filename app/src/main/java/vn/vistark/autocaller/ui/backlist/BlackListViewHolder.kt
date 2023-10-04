package vn.vistark.autocaller.ui.backlist

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.BlackListModel

class BlackListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val cDataRlRoot: RelativeLayout = v.findViewById(R.id.cDataRlRoot)
    val cDataPhoneNumber: TextView = v.findViewById(R.id.cDataPhoneNumber)
    val cDataCallState: TextView = v.findViewById(R.id.cDataCallState)
    val cDataCallSignalTime: TextView = v.findViewById(R.id.cDataCallSignalTime)

    fun bind(pos: Int, blackListModel: BlackListModel) {
        cDataCallState.visibility = View.GONE
        cDataCallSignalTime.visibility = View.GONE
        cDataPhoneNumber.text = "#${pos + 1}. ${blackListModel.phone}"
    }
}