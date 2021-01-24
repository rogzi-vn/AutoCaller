package vn.vistark.autocaller.models

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseContext(val context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME, null, 2101241
) {
    companion object {
        const val DATABASE_NAME = "AutoCaller.db"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Chạy lệnh tạo bảng
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS \"${CampaignModel.TABLE_NAME}\" (\n" +
                    "\t\"${CampaignModel.ID}\"\tINTEGER NOT NULL,\n" +
                    "\t\"${CampaignModel.NAME}\"\tTEXT NOT NULL,\n" +
                    "\t\"${CampaignModel.LAST_PHONE_ID}\"\tINTEGER NOT NULL DEFAULT 0,\n" +
                    "\t\"${CampaignModel.TOTAL_IMPORTED}\"\tINTEGER NOT NULL DEFAULT 0,\n" +
                    "\t\"${CampaignModel.TOTAL_CALLED}\"\tINTEGER NOT NULL DEFAULT 0,\n" +
                    "\t\"${CampaignModel.TOTAL_FAIL}\"\tINTEGER DEFAULT 0,\n" +
                    "\tPRIMARY KEY(\"${CampaignModel.ID}\" AUTOINCREMENT)\n" +
                    ");"
        )
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS \"${CampaignDataModel.TABLE_NAME}\" (\n" +
                    "\t\"${CampaignDataModel.ID}\"\tINTEGER NOT NULL,\n" +
                    "\t\"${CampaignDataModel.CAMPAIGN_ID}\"\tINTEGER NOT NULL,\n" +
                    "\t\"${CampaignDataModel.PHONE}\"\tTEXT NOT NULL,\n" +
                    "\t\"${CampaignDataModel.CALL_STATE}\"\tINTEGER NOT NULL DEFAULT 0,\n" +
                    "\t\"${CampaignDataModel.INDEX_IN_CAMPAIGN}\"\tINTEGER NOT NULL DEFAULT 0,\n" +
                    "\t\"${CampaignDataModel.IS_CALLED}\"\tINTEGER NOT NULL DEFAULT 0,\n" +
                    "\tPRIMARY KEY(\"${CampaignDataModel.ID}\" AUTOINCREMENT)\n" +
                    ");"
        )
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS \"${BlackListModel.TABLE_NAME}\" (\n" +
                    "\t\"${BlackListModel.ID}\"\tINTEGER NOT NULL,\n" +
                    "\t\"${BlackListModel.PHONE}\"\tTEXT NOT NULL,\n" +
                    "\tPRIMARY KEY(\"${BlackListModel.ID}\" AUTOINCREMENT)\n" +
                    ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

        // Xóa các bảng cũ
        db?.execSQL("DROP TABLE IF EXISTS ${CampaignModel.TABLE_NAME}")
        db?.execSQL("DROP TABLE IF EXISTS ${CampaignDataModel.TABLE_NAME}")

        // Tọa lại bảng mới
        onCreate(db)
    }


}