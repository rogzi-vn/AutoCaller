package vn.vistark.autocaller.controller.campaign_detail

import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import es.dmoral.toasty.Toasty
import vn.vistark.autocaller.controller.campaign_create.CampaignCreateATL
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.views.campaign_detail.CampaignDetailActivity

class CampaignDetailLoader(val context: CampaignDetailActivity) :
    LoaderManager.LoaderCallbacks<Array<CampaignDataModel>> {

    private val loaderManager = LoaderManager.getInstance(context)
    private lateinit var campaignDetailATL: CampaignDetailATL

    init {
        loaderManager.initLoader(4567, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Array<CampaignDataModel>> {
        campaignDetailATL = CampaignDetailATL(context)
        return campaignDetailATL
    }

    override fun onLoadFinished(
        loader: Loader<Array<CampaignDataModel>>,
        data: Array<CampaignDataModel>?
    ) {
        if (data == null) {
            Toasty.error(context, "Lỗi khi tải dữ liệu", Toasty.LENGTH_SHORT, true).show()
        } else {
            println(">>>>>>>>>>> LẤY ĐƯỢC ${data.size}")
            data.forEach {
                println(">>> [${it.phone}] <<<")
            }
            context.lastPhoneIndex = data[data.size - 1].id
            println("ID Cuối cùng là ${context.lastPhoneIndex}")
            context.campaignDatas.addAll(data)
            context.adapter.notifyDataSetChanged()
        }
        context.cancelLoading()
    }

    override fun onLoaderReset(loader: Loader<Array<CampaignDataModel>>) {
    }

}