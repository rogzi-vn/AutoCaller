package vn.vistark.autocaller.ui.campaign_detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.CampaignDataModel

class CampaignDataDetailAdapter(private val campaignDatas: ArrayList<CampaignDataModel>) :
    RecyclerView.Adapter<CampaignDataDetailViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CampaignDataDetailViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout._campaign_data_item, parent, false)
        return CampaignDataDetailViewHolder(v)
    }

    override fun getItemCount(): Int {
        return campaignDatas.size
    }

    override fun onBindViewHolder(holder: CampaignDataDetailViewHolder, position: Int) {
        val campaignData = campaignDatas[position]
        holder.bind(campaignData)
    }
}