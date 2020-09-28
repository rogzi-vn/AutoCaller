package vn.vistark.autocaller.controller.campaign_create

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.content.AsyncTaskLoader
import cn.pedant.SweetAlert.SweetAlertDialog
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.views.campaign_create.CampaignCreateActivity
import java.io.BufferedReader
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*

class CampaignCreateController {
    companion object {
        fun generateCampaignName(context: AppCompatActivity): String {
            val campaignMaxId = CampaignRepository(context).getMaxId() + 1
            return "#$campaignMaxId ngày " + SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date())
        }

    }
}