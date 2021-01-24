package vn.vistark.autocaller.controller.campaign_create

import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.loader.content.AsyncTaskLoader
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.DatabaseContext
import vn.vistark.autocaller.models.PhoneCallState
import vn.vistark.autocaller.models.repositories.CampaignDataRepository
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.ui.campaign_create.CampaignCreateActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class CampaignCreateATL(val context: CampaignCreateActivity, private val uri: Uri?) :
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
        var index = 0
        var processCount = 0L

        db.beginTransaction()
        while (r.readLine().also { phoneNumber = it } != null) {
            // Khởi tạo dữ liệu
            val campaignData =
                CampaignDataModel(
                    0,
                    context.campaign.id,
                    phoneNumber,
                    PhoneCallState.NOT_CALL,
                    index++,
                    false
                )

            // Tạo bộ dữ liệu nhập
            val values = CampaignDataRepository.createDataValues(campaignData)

            // Lưu dữ liệu và đếm nếu thành công
//            db.insert(CampaignDataModel.TABLE_NAME, null,values)

            // Gọi phương thức nhập chung
            if (db.insertWithOnConflict(
                    CampaignDataModel.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                ) > 0
            )
                successCount++

            context.updateProgressState(campaignData, 1 + processCount++)
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

        // Cập nhập tổng số SĐT đã import vào danh mục
        context.campaign.totalImported = successCount

        // Lưu lại vào CSDL
        CampaignRepository(context).update(context.campaign)

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