package vn.vistark.autocaller.controller.campaign_update

import android.net.Uri
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import vn.vistark.autocaller.ui.campaign_update.CampaignUpdateActivity


class CampaignUpdateLoader(val context: CampaignUpdateActivity, val uri: Uri?) :
    LoaderManager.LoaderCallbacks<Int> {
    private val loaderManager = LoaderManager.getInstance(context)
    lateinit var campaignUpdateATL: CampaignUpdateATL


    init {
        loaderManager.initLoader(2213, null, this)
    }


    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Int> {
        campaignUpdateATL = CampaignUpdateATL(context, uri)
        return campaignUpdateATL
    }

    override fun onLoadFinished(loader: Loader<Int>, data: Int) {
        when (data) {
            -1 -> {
                context.importFail()
            }
            -2 -> {
                context.importFail("Không tìm thấy tệp tin")
            }
            else -> {
                context.showSuccess(data.toLong())
            }
        }
    }


    override fun onLoaderReset(loader: Loader<Int>) {
    }
}