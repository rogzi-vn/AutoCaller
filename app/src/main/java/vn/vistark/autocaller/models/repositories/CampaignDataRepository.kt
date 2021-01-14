package vn.vistark.autocaller.models.repositories

import android.content.ContentValues
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.DatabaseContext
import vn.vistark.autocaller.models.PhoneCallState
import vn.vistark.autocaller.utils.getBoolean
import vn.vistark.autocaller.utils.getInt
import vn.vistark.autocaller.utils.getString

// https://stackoverflow.com/questions/10600670/sqlitedatabase-query-method

class CampaignDataRepository(val context: Context) {
    private val instance: DatabaseContext = DatabaseContext(context)

    companion object {
        // Tạo bộ dữ liệu
        fun createDataValues(campaignDataModel: CampaignDataModel): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(CampaignDataModel.CAMPAIGN_ID, campaignDataModel.campaignId)
            contentValues.put(CampaignDataModel.PHONE, campaignDataModel.phone)
            contentValues.put(CampaignDataModel.CALL_STATE, campaignDataModel.callState)
            contentValues.put(
                CampaignDataModel.INDEX_IN_CAMPAIGN,
                campaignDataModel.indexInCampaign
            )
            contentValues.put(CampaignDataModel.IS_CALLED, if (campaignDataModel.isCalled) 1 else 0)
            return contentValues
        }
    }

    // Xem bên CampaignRespository
    fun add(campaignDataModel: CampaignDataModel): Long {
        // Xây dựng bộ dữ liệu
        val contentValues = createDataValues(campaignDataModel)

        // Ghi vào db
        val res =
            instance.writableDatabase.insert(CampaignDataModel.TABLE_NAME, null, contentValues)

        // Đóng CSDL lại
        instance.writableDatabase.close()

        // Trả về kết quả
        return res
    }

    // Nên coi http://sqlfiddle.com/#!5/d0a2d/2746
    fun getLimit(campaginId: Int, lastCampaignDataId: Int, limit: Long): Array<CampaignDataModel> {
        // Khai báo biến chứa danh sách
        val campaignDatas = ArrayList<CampaignDataModel>()
        // Lấy con trỏ
        val cursor = instance.readableDatabase.query(
            true,
            CampaignDataModel.TABLE_NAME,
            null,
            "${CampaignDataModel.ID} > ? AND ${CampaignDataModel.CAMPAIGN_ID} = ?",
            arrayOf(lastCampaignDataId.toString(), campaginId.toString()),
            null,
            null,
            "${CampaignDataModel.ID} ASC",
            limit.toString()
        )
        // Nếu không có bản ghi
        if (!cursor.moveToFirst()) {
            instance.readableDatabase.close()
            cursor.close()
            return campaignDatas.toTypedArray()
        }

        // Còn có thì tiến hành duyệt
        do {
            try {
                // Gán dữ liệu vào đối tượng
                val campaignData = CampaignDataModel(
                    cursor.getInt(CampaignDataModel.ID),
                    cursor.getInt(CampaignDataModel.CAMPAIGN_ID),
                    cursor.getString(CampaignDataModel.PHONE),
                    cursor.getInt(CampaignDataModel.CALL_STATE),
                    cursor.getInt(CampaignDataModel.INDEX_IN_CAMPAIGN),
                    cursor.getBoolean(CampaignDataModel.IS_CALLED)
                )
                // Theeo vào danh sách lưu trữ
                campaignDatas.add(campaignData)
            } catch (e: Exception) {
                // Sự đời khó lường trước
                e.printStackTrace()
            }

        } while (cursor.moveToNext())

        // Đóng con trỏ
        cursor.close()

        // Đóng trình đọc
        instance.readableDatabase.close()

        // Trả về dữ liệu
        return campaignDatas.toTypedArray()
    }

    // Thiết lập các tham trị quay về mặc định như ban đầu cho tất cả các bộ dữ liệu thuộc 1 chiến dịch
    fun reset(campaignId: Int): Int {
        // Xây dựng bộ dữ liệu
        val contentValues = ContentValues()
        contentValues.put(CampaignDataModel.CALL_STATE, PhoneCallState.NOT_CALL)
        contentValues.put(CampaignDataModel.IS_CALLED, 0)

        // Ghi vào db
        val res =
            instance.writableDatabase.update(
                CampaignDataModel.TABLE_NAME,
                contentValues,
                "${CampaignDataModel.CAMPAIGN_ID}=?",
                arrayOf(campaignId.toString())
            )

        // Đóng CSDL lại
        instance.writableDatabase.close()

        // Trả về kết quả
        return res
    }

    // Cập nhật, KQ: Số dòng chịu tác động
    fun update(campaignDataModel: CampaignDataModel): Int {
        // Xây dựng bộ dữ liệu
        val contentValues = createDataValues(campaignDataModel)

        // Ghi vào db
        val res =
            instance.writableDatabase.update(
                CampaignDataModel.TABLE_NAME,
                contentValues,
                "${CampaignDataModel.ID}=?",
                arrayOf(campaignDataModel.id.toString())
            )

        // Đóng CSDL lại
        instance.writableDatabase.close()

        // Trả về kết quả
        return res
    }

    // Xóa hết các dữ liệu của chiến dịch này
    fun removeAll(): Int {
        val res = instance.writableDatabase.delete(
            CampaignDataModel.TABLE_NAME,
            "1",
            null
        )
        instance.writableDatabase.close()
        return res
    }
}