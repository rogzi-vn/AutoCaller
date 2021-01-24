package vn.vistark.autocaller.controller.campaign_create

import android.net.Uri
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import vn.vistark.autocaller.ui.campaign_create.CampaignCreateActivity


class CampaignCreateLoader(val context: CampaignCreateActivity, val uri: Uri?) :
    LoaderManager.LoaderCallbacks<Int> {
    private val loaderManager = LoaderManager.getInstance(context)
    lateinit var campaignCreateATL: CampaignCreateATL


    init {
        loaderManager.initLoader(2212, null, this)
    }


    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Int> {
        campaignCreateATL = CampaignCreateATL(context, uri)
        return campaignCreateATL
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