package vn.vistark.autocaller.ui.campaign

import android.annotation.SuppressLint
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.CampaignModel

class CampaignViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
    val cItemLnRoot: LinearLayout = v.findViewById(R.id.cItemLnRoot)
    private val campaignItemName: TextView = v.findViewById(R.id.campaignItemName)
    private val campaignItemProgressCount: TextView = v.findViewById(R.id.campaignItemProgressCount)
    private val campaignItemProgressBar: ProgressBar = v.findViewById(R.id.campaignItemProgressBar)
    private val campaignItemProgressPercent: TextView =
        v.findViewById(R.id.campaignItemProgressPercent)

    // Lưu lại dữ liệu được gán
    var campaignModel: CampaignModel? = null

    @SuppressLint("SetTextI18n")
    fun bind(campaignModel: CampaignModel) {

        // Cập nhật lưu trữ
        this.campaignModel = campaignModel

        // Hiển thị
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