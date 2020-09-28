package vn.vistark.autocaller.controller.campaign_create

import androidx.loader.content.AsyncTaskLoader
import kotlinx.android.synthetic.main.activity_campaign_create.*
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.PhoneCallState
import vn.vistark.autocaller.models.repositories.CampaignDataRepository
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.views.campaign_create.CampaignCreateActivity
import java.io.BufferedReader
import java.io.FileReader

class CampaignCreateATL(val context: CampaignCreateActivity, private val path: String) :
    AsyncTaskLoader<Int>(context) {

    // Kho
    val campaignRepository = CampaignDataRepository(context)

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
        if (path.isEmpty())
            return -1

        // Đếm tổng số records
        countTotalRecords()

        // Nhập dữ liệu
        return importAllRecords()
    }

    private fun importAllRecords(): Int {
        val r = BufferedReader(FileReader(path))
        var phoneNumber: String?
        var index = 0
        var processCount = 0L
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

            // Lưu dữ liệu và đếm nếu thành công
            if (campaignRepository.add(campaignData).toInt() > 0)
                successCount++

            context.updateProgressState(campaignData, 1 + processCount++)
        }
        // Đóng bộ đọc file
        r.close()

        // Cập nhập tổng số SĐT đã import vào danh mục
        context.campaign.totalImported = successCount

        // Lưu lại vào CSDL
        CampaignRepository(context).update(context.campaign)

        return successCount
    }

    private fun countTotalRecords() {
        // Đếm tổng số bản ghi
        val reader = BufferedReader(FileReader(path))
        var lines = 0
        while (reader.readLine() != null) lines++
        reader.close()

        // Lưu vào context
        context.totalLines = lines.toLong()

        // Ẩn loading nếu đang hiện
        context.hideLoading()
    }
}