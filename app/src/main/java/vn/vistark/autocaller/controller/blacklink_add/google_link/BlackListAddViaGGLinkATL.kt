package vn.vistark.autocaller.controller.blacklink_add.google_link

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.loader.content.AsyncTaskLoader
import vn.vistark.autocaller.models.BlackListModel
import vn.vistark.autocaller.models.DatabaseContext
import vn.vistark.autocaller.models.repositories.BlackListRepository
import vn.vistark.autocaller.ui.backlist.BlacklistActivity
import java.net.URL

class BlackListAddViaGGLinkATL(val context: BlacklistActivity, private val url: String) :
    AsyncTaskLoader<Int>(context) {

    var contents: ArrayList<String> = ArrayList()

    // Kho
    val db = DatabaseContext(context).writableDatabase

    // Biến chứa số bản ghi upload thành công
    var successCount = 0

    init {
        // Hiện loading
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

    override fun loadInBackground(): Int {
        // Nếu path trống
        if (url.isEmpty())
            return -1

        // Đếm tổng số records
        countTotalRecords()

        // Nhập dữ liệu
        return importAllRecords()
    }

    private fun importAllRecords(): Int {
        var processCount = 0L

        db.beginTransaction()
        contents.forEach {
            // Khởi tạo dữ liệu
            val blacklistModel =
                BlackListModel(
                    0,
                    it
                )

            // Tạo bộ dữ liệu nhập
            val values = BlackListRepository.createDataValues(blacklistModel)

            // Lưu dữ liệu và đếm nếu thành công
//            db.insert(CampaignDataModel.TABLE_NAME, null,values)

            // Gọi phương thức nhập chung
            if (db.insertWithOnConflict(
                    BlackListModel.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                ) > 0
            )
                successCount++

            context.updateProgressState(blacklistModel, 1 + processCount++)
        }
        // Thông báo là transact đã thành công
        db.setTransactionSuccessful()

        // Kết thúc transact
        db.endTransaction()

        // Đóng database lại
        db.close()

        return successCount
    }

    private fun countTotalRecords() {
        try {
            Log.e("URL", url)
            val loadedContent: String = URL(url).readText()
            Log.e("CONTENT", loadedContent)
            if (loadedContent.isNotEmpty()) {
                val temp = loadedContent.split("\r\n")
                temp.forEach {
                    if ("\\d{8,12}".toRegex().matches(it.trim())) {
                        contents.add(it.trim())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Lưu vào context
        context.totalLines = contents.size.toLong()

        // Ẩn loading nếu đang hiện
        context.hideLoading()
    }
}