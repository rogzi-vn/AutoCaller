package vn.vistark.autocaller.models

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Parcel
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import vn.vistark.autocaller.R
import vn.vistark.autocaller.utils.ResourceUtils

class DatabaseContext(val context: AppCompatActivity) : SQLiteOpenHelper(
    context,
    DATABASE_NAME, null, 1
) {
    companion object {
        const val DATABASE_NAME = "AutoCaller.db"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Đọc các lệnh tạo bảng từ raw
        val sqlRaw = ResourceUtils.readText(context, R.raw.auto_caller)

        // Chạy lệnh tạo bảng
        db?.execSQL(sqlRaw)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

        // Xóa các bảng cũ
        db?.execSQL("DROP TABLE IF EXISTS ${CampaignModel.TABLE_NAME}")
        db?.execSQL("DROP TABLE IF EXISTS ${CampaignDataModel.TABLE_NAME}")

        // Tọa lại bảng mới
        onCreate(db)
    }

}