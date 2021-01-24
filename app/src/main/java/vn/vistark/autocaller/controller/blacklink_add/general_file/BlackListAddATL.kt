package vn.vistark.autocaller.controller.blacklink_add.general_file

import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.loader.content.AsyncTaskLoader
import vn.vistark.autocaller.models.BlackListModel
import vn.vistark.autocaller.models.DatabaseContext
import vn.vistark.autocaller.models.repositories.BlackListRepository
import vn.vistark.autocaller.ui.backlist.BlacklistActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class BlackListAddATL(val context: BlacklistActivity, private val uri: Uri?) :
    AsyncTaskLoader<Int>(context) {

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
        if (uri == null)
            return -1

        // Đếm tổng số records
        countTotalRecords()

        // Nhập dữ liệu
        return importAllRecords()
    }

    private fun importAllRecords(): Int {
        val inpReader = InputStreamReader(context.contentResolver.openInputStream(uri!!))
        val r = BufferedReader(inpReader)
        var phoneNumber: String?
        var processCount = 0L

        db.beginTransaction()
        while (r.readLine().also { phoneNumber = it } != null) {
            // Khởi tạo dữ liệu
            val blacklistModel =
                BlackListModel(
                    0,
                    phoneNumber
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
        // Đóng bộ đọc file
        r.close()

        // Đóng input stream reader
        inpReader.close()

        // Thông báo là transact đã thành công
        db.setTransactionSuccessful()

        // Kết thúc transact
        db.endTransaction()

        // Đóng database lại
        db.close()

        return successCount
    }

    private fun countTotalRecords() {
        val inpReader = InputStreamReader(context.contentResolver.openInputStream(uri!!))
        // Đếm tổng số bản ghi
        val reader = BufferedReader(inpReader)
        var lines = 0
        while (reader.readLine() != null) lines++
        reader.close()

        inpReader.close()

        // Lưu vào context
        context.totalLines = lines.toLong()

        // Ẩn loading nếu đang hiện
        context.hideLoading()
    }
}