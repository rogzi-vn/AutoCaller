package vn.vistark.autocaller.controller.campaign_detail

import androidx.loader.content.AsyncTaskLoader
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.repositories.CampaignDataRepository
import vn.vistark.autocaller.views.campaign_detail.CampaignDetailActivity

class CampaignDetailATL(val context: CampaignDetailActivity) :
    AsyncTaskLoader<Array<CampaignDataModel>>(context) {

    var campaignDataRepository: CampaignDataRepository = CampaignDataRepository(context)

    init {
        // Đối tượng kho chứa dữ liệu
        context.showLoading()
        onContentChanged()
    }

    override fun onStartLoading() {
        if (takeContentChanged())
            forceLoad()
    }

    override fun onStopLoading() {
        cancelLoad()
    }

    override fun loadInBackground(): Array<CampaignDataModel> {
        println("Load cho ${context.lastPhoneIndex}")
        return campaignDataRepository.getLimit(context.campaign!!.id, context.lastPhoneIndex, 3)
    }
}