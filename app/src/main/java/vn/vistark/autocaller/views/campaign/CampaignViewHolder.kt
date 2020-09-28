package vn.vistark.autocaller.views.campaign

import android.annotation.SuppressLint
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.CampaignModel

class CampaignViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
    val campaignItemName: TextView = v.findViewById(R.id.campaignItemName)
    val campaignItemProgressCount: TextView = v.findViewById(R.id.campaignItemProgressCount)
    val campaignItemProgressBar: ProgressBar = v.findViewById(R.id.campaignItemProgressBar)
    val campaignItemProgressPercent: TextView = v.findViewById(R.id.campaignItemProgressPercent)

    @SuppressLint("SetTextI18n")
    fun bind(campaignModel: CampaignModel) {
        campaignItemName.post {
            campaignItemName.text = campaignModel.name
            campaignItemProgressCount.text =
                "(${campaignModel.totalCalled}/${campaignModel.totalImported})"

            val progress: Int =
                ((campaignModel.totalCalled.toDouble() / campaignModel.totalImported.toDouble()) * 100).toInt()
            campaignItemProgressBar.progress = progress
            campaignItemProgressPercent.text = "$progress%"
        }
    }
}