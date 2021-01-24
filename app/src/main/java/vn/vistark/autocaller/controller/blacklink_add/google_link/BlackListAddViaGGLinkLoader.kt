package vn.vistark.autocaller.controller.blacklink_add.google_link

import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import vn.vistark.autocaller.controller.blacklink_add.general_file.BlackListAddATL
import vn.vistark.autocaller.ui.backlist.BlacklistActivity

class BlackListAddViaGGLinkLoader(val context: BlacklistActivity, val url: String) :
    LoaderManager.LoaderCallbacks<Int> {
    private val loaderManager = LoaderManager.getInstance(context)
    lateinit var blackListAddViaGGLinkATL: BlackListAddViaGGLinkATL


    init {
        loaderManager.initLoader(2217, null, this)
    }


    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Int> {
        blackListAddViaGGLinkATL = BlackListAddViaGGLinkATL(context, url)
        return blackListAddViaGGLinkATL
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