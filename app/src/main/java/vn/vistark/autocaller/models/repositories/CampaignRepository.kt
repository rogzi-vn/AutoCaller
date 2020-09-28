package vn.vistark.autocaller.models.repositories

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.DatabaseContext
import vn.vistark.autocaller.utils.getInt
import vn.vistark.autocaller.utils.getString

class CampaignRepository(val context: AppCompatActivity) {
    private val instance: DatabaseContext = DatabaseContext(context)

    // Thêm mới chiến dịch và trả về ID của chiến dịch đó (-1 là lỗi)
    fun add(campaignModel: CampaignModel): Long {
        // Xây dựng bộ dữ liệu
        val contentValues = ContentValues()
        contentValues.put(CampaignModel.NAME, campaignModel.name)
        contentValues.put(CampaignModel.LAST_PHONE_ID, campaignModel.lastPhoneId)
        contentValues.put(CampaignModel.TOTAL_IMPORTED, campaignModel.totalImported)
        contentValues.put(CampaignModel.TOTAL_CALLED, campaignModel.totalCalled)
        contentValues.put(CampaignModel.TOTAL_FAIL, campaignModel.totalFail)

        // Ghi vào db
        val res = instance.writableDatabase.insert(CampaignModel.TABLE_NAME, null, contentValues)

        // Đóng CSDL lại
        instance.writableDatabase.close()

        // Trả về kết quả
        return res
    }

    fun getMaxId(): Long {
        val cursor = instance.readableDatabase.rawQuery(
            "SELECT MAX(${CampaignModel.ID}) as ${CampaignModel.ID} FROM ${CampaignModel.TABLE_NAME}",
            null
        )

        // Nếu không có, trả về 0
        if (!cursor.moveToFirst()) {
            cursor.close()
            instance.readableDatabase.close()
            return 0
        }

        // Hoặc trả về MAX ID
        val res = cursor.getInt(0)

        cursor.close()
        instance.readableDatabase.close()

        return res.toLong()
    }

    // Lấy danh sách các chiến dịch đã có - sắp sếp theo id giảm dần (Mới hơn ở trên)
    fun getAll(): Array<CampaignModel> {

        // Khai báo biến chứa danh sách
        val campaigns = ArrayList<CampaignModel>()

        // Lấy con trỏ
        val cursor = instance.readableDatabase.query(
            true,
            CampaignModel.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "${CampaignModel.ID} DESC",
            null
        )

        // Nếu không có bản ghi
        if (!cursor.moveToFirst()) {
            cursor.close()
            return campaigns.toTypedArray()
        }

        // Còn có thì tiến hành duyệt
        do {
            try {
                // Gán dữ liệu vào đối tượng
                val campaign = CampaignModel(
                    cursor.getInt(CampaignModel.ID),
                    cursor.getString(CampaignModel.NAME),
                    cursor.getInt(CampaignModel.LAST_PHONE_ID),
                    cursor.getInt(CampaignModel.TOTAL_IMPORTED),
                    cursor.getInt(CampaignModel.TOTAL_CALLED),
                    cursor.getInt(CampaignModel.TOTAL_FAIL)
                )
                // Theeo vào danh sách lưu trữ
                campaigns.add(campaign)
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
        return campaigns.toTypedArray()
    }

    // Cập nhật
    fun update(campaignModel: CampaignModel): Int {
        // Xây dựng bộ dữ liệu
        val contentValues = ContentValues()
        contentValues.put(CampaignModel.NAME, campaignModel.name)
        contentValues.put(CampaignModel.LAST_PHONE_ID, campaignModel.lastPhoneId)
        contentValues.put(CampaignModel.TOTAL_IMPORTED, campaignModel.totalImported)
        contentValues.put(CampaignModel.TOTAL_CALLED, campaignModel.totalCalled)
        contentValues.put(CampaignModel.TOTAL_FAIL, campaignModel.totalFail)

        // Ghi vào db
        val res = instance.writableDatabase.update(
            CampaignModel.TABLE_NAME, contentValues, "${CampaignModel.ID}=?",
            arrayOf(campaignModel.id.toString())
        )

        // Đóng CSDL lại
        instance.writableDatabase.close()

        // Trả về kết quả
        return res
    }

    // Xóa chiến dịch, trả về số hàng bị xóa, 0 nếu không có
    fun remove(id: Long): Int {
        val res = instance.writableDatabase.delete(
            CampaignModel.TABLE_NAME,
            "${CampaignModel.ID}=?",
            arrayOf(id.toString())
        )
        instance.writableDatabase.close()
        return res
    }
}