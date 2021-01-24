package vn.vistark.autocaller.models.repositories

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import vn.vistark.autocaller.models.BlackListModel
import vn.vistark.autocaller.models.DatabaseContext
import vn.vistark.autocaller.utils.getInt
import vn.vistark.autocaller.utils.getString


// https://stackoverflow.com/questions/10600670/sqlitedatabase-query-method

class BlackListRepository(val context: Context) {
    private val instance: DatabaseContext = DatabaseContext(context)

    companion object {
        // Tạo bộ dữ liệu
        fun createDataValues(blackListModel: BlackListModel): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(BlackListModel.PHONE, blackListModel.phone)
            return contentValues
        }
    }

    fun isHavePhone(
        phone: String
    ): Boolean {
        val Query =
            "Select * from ${BlackListModel.TABLE_NAME} where ${BlackListModel.PHONE} = $phone"
        val cursor = instance.readableDatabase.rawQuery(Query, null)
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    // Xem bên CampaignRespository
    fun add(blackListModel: BlackListModel): Long {
        // Xây dựng bộ dữ liệu
        val contentValues = createDataValues(blackListModel)

        // Ghi vào db
        val res =
            instance.writableDatabase.insert(BlackListModel.TABLE_NAME, null, contentValues)

        // Đóng CSDL lại
        instance.writableDatabase.close()

        // Trả về kết quả
        return res
    }

    // Nên coi http://sqlfiddle.com/#!5/d0a2d/2746
    fun getLimit(lastCampaignDataId: Int, limit: Long): Array<BlackListModel> {
        // Khai báo biến chứa danh sách
        val campaignDatas = ArrayList<BlackListModel>()
        // Lấy con trỏ
        val cursor = instance.readableDatabase.query(
            true,
            BlackListModel.TABLE_NAME,
            null,
            "${BlackListModel.ID} > ?",
            arrayOf(lastCampaignDataId.toString()),
            null,
            null,
            "${BlackListModel.ID} ASC",
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
                val campaignData = BlackListModel(
                    cursor.getInt(BlackListModel.ID),
                    cursor.getString(BlackListModel.PHONE)
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

    // Nên coi http://sqlfiddle.com/#!5/d0a2d/2746
    fun getAll(): Array<BlackListModel> {
        // Khai báo biến chứa danh sách
        val campaignDatas = ArrayList<BlackListModel>()
        // Lấy con trỏ
        val cursor: Cursor =
            instance.readableDatabase.rawQuery("select * from ${BlackListModel.TABLE_NAME}", null)
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
                val campaignData = BlackListModel(
                    cursor.getInt(BlackListModel.ID),
                    cursor.getString(BlackListModel.PHONE)
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

    // Cập nhật, KQ: Số dòng chịu tác động
    fun update(blackListModel: BlackListModel): Int {
        // Xây dựng bộ dữ liệu
        val contentValues = createDataValues(blackListModel)

        // Ghi vào db
        val res =
            instance.writableDatabase.update(
                BlackListModel.TABLE_NAME,
                contentValues,
                "${BlackListModel.ID}=?",
                arrayOf(blackListModel.id.toString())
            )

        // Đóng CSDL lại
        instance.writableDatabase.close()

        // Trả về kết quả
        return res
    }

    // Xóa hết các dữ liệu của chiến dịch này
    fun removeAll(): Int {
        val res = instance.writableDatabase.delete(
            BlackListModel.TABLE_NAME,
            "1",
            null
        )
        instance.writableDatabase.close()
        return res
    }
}