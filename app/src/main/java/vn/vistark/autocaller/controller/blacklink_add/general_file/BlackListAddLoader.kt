package vn.vistark.autocaller.controller.blacklink_add.general_file

import android.net.Uri
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import vn.vistark.autocaller.ui.backlist.BlacklistActivity


class BlackListAddLoader(val context: BlacklistActivity, val uri: Uri?) :
    LoaderManager.LoaderCallbacks<Int> {
    private val loaderManager = LoaderManager.getInstance(context)
    lateinit var blackListAddATL: BlackListAddATL


    init {
        loaderManager.initLoader(2215, null, this)
    }


    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Int> {
        blackListAddATL = BlackListAddATL(context, uri)
        return blackListAddATL
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