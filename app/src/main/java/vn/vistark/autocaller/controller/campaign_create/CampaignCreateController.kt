package vn.vistark.autocaller.controller.campaign_create

import java.text.SimpleDateFormat
import java.util.*

class CampaignCreateController {
    companion object {
        fun generateCampaignName(): String {
            return "Chiến dịch lúc " + SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                .format(Date())
        }

    }
}