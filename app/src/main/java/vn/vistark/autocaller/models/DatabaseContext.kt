package vn.vistark.autocaller.models

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseContext(val context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME, null, 2309111//2101241
) {
    companion object {
        const val DATABASE_NAME = "AutoCaller.db"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        createTable(db, CampaignModel.TABLE_NAME,
            "${CampaignModel.ID} INTEGER PRIMARY KEY AUTOINCREMENT",
            "${CampaignModel.NAME} TEXT NOT NULL",
            "${CampaignModel.LAST_PHONE_ID} INTEGER NOT NULL DEFAULT 0",
            "${CampaignModel.TOTAL_IMPORTED} INTEGER NOT NULL DEFAULT 0",
            "${CampaignModel.TOTAL_CALLED} INTEGER NOT NULL DEFAULT 0",
            "${CampaignModel.TOTAL_FAIL} INTEGER DEFAULT 0"
        )
        createTable(db, CampaignDataModel.TABLE_NAME,
            "${CampaignDataModel.ID} INTEGER PRIMARY KEY AUTOINCREMENT",
            "${CampaignDataModel.CAMPAIGN_ID} INTEGER NOT NULL",
            "${CampaignDataModel.PHONE} TEXT NOT NULL",
            "${CampaignDataModel.CALL_STATE} INTEGER NOT NULL DEFAULT 0",
            "${CampaignDataModel.INDEX_IN_CAMPAIGN} INTEGER NOT NULL DEFAULT 0",
            "${CampaignDataModel.IS_CALLED} INTEGER NOT NULL DEFAULT 0",
            "${CampaignDataModel.RECEIVED_SIGNAL_TIME_IN_MILLISECONDS} LONG NOT NULL DEFAULT 0"
        )
        createTable(db, BlackListModel.TABLE_NAME,
            "${BlackListModel.ID} INTEGER PRIMARY KEY AUTOINCREMENT",
            "${BlackListModel.PHONE} TEXT NOT NULL"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        dropTable(db, CampaignModel.TABLE_NAME)
        dropTable(db, CampaignDataModel.TABLE_NAME)
        onCreate(db)
    }

    private fun createTable(
        db: SQLiteDatabase?,
        tableName: String,
        vararg columns: String
    ) {
        val createTableSQL = buildString {
            append("CREATE TABLE IF NOT EXISTS \"$tableName\" (\n")
            for (column in columns) {
                append("\t$column")
                if (column != columns.last()) {
                    append(",\n")
                } else {
                    append("\n")
                }
            }
            append(");")
        }
        db?.execSQL(createTableSQL)
    }

    private fun dropTable(db: SQLiteDatabase?, tableName: String) {
        val dropTableSQL = "DROP TABLE IF EXISTS $tableName"
        db?.execSQL(dropTableSQL)
    }
}
