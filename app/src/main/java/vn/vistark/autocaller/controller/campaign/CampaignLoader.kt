package vn.vistark.autocaller.controller.campaign

import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import vn.vistark.autocaller.ui.campaign.CampaignActivity

class CampaignLoader(val context: CampaignActivity) : LoaderManager.LoaderCallbacks<Boolean> {
    var loaderManager: LoaderManager = LoaderManager.getInstance(context)

    init {
        loaderManager.initLoader(-1, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Boolean> {
        return CampaignATL(context)
    }

    override fun onLoadFinished(loader: Loader<Boolean>, data: Boolean?) {
    }

    override fun onLoaderReset(loader: Loader<Boolean>) {
    }
}