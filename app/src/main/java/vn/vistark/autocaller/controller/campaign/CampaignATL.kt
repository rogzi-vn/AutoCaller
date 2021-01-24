package vn.vistark.autocaller.controller.campaign

import androidx.loader.content.AsyncTaskLoader
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.ui.campaign.CampaignActivity

class CampaignATL(val context: CampaignActivity) : AsyncTaskLoader<Boolean>(context) {
    private val campaignRepository = CampaignRepository(context)

    init {
        onContentChanged()
        context.showLoading()
    }

    override fun onStartLoading() {
        if (takeContentChanged())
            forceLoad()
    }

    override fun stopLoading() {
        cancelLoad()
    }

    override fun loadInBackground(): Boolean? {
        var lastCampaignId = 0
        while (true) {
            // Lấy x phần tử đầu tiên
            val campaigns = campaignRepository.getLimit(lastCampaignId, 5)
            // Nếu không có thì ngưng
            if (campaigns.isEmpty())
                break

            // Có thì lấy ID của phần tử cuối cùng
            lastCampaignId = campaigns.last().id

            // Lặp và thêm vào ds hiển tị
            campaigns.forEach { campaign ->
                context.addCampaign(campaign)
            }
        }

        context.hideLoading()
        return true
    }

}