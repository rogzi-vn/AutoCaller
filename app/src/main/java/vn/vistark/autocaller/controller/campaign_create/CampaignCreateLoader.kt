package vn.vistark.autocaller.controller.campaign_create

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import es.dmoral.toasty.Toasty
import vn.vistark.autocaller.views.campaign_create.CampaignCreateActivity


class CampaignCreateLoader(val context: CampaignCreateActivity, val uri: Uri?) :
    LoaderManager.LoaderCallbacks<Int> {


    init {
        val loaderManager = LoaderManager.getInstance(context)
        loaderManager.initLoader(-1, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Int> {
        return CampaignCreateATL(
            context,
            getPath(uri) ?: ""
        )
    }

    private fun getPath(uri: Uri?): String? {
        val projection =
            arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? =
            context.contentResolver.query(uri!!, projection, null, null, null)
        if (cursor == null) {
            cursor?.close()
            return ""
        }
        val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s: String = cursor.getString(columnIndex)
        cursor.close()
        return s
    }

    override fun onLoadFinished(loader: Loader<Int>, data: Int) {
        when (data) {
            -1 -> {
                Toasty.error(context, "Nhập dữ liệu thất bại", Toasty.LENGTH_SHORT, true).show()
            }
            -2 -> {
                Toasty.error(context, "Không tìm thấy tệp tin", Toasty.LENGTH_SHORT, true).show()
            }
            else -> {
                context.showSuccess(data.toLong())
            }
        }
    }


    override fun onLoaderReset(loader: Loader<Int>) {
    }
}