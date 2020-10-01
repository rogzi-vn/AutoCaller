package vn.vistark.autocaller.controller.campaign_detail

import androidx.appcompat.app.AppCompatActivity
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.PhoneCallState
import vn.vistark.autocaller.models.repositories.CampaignDataRepository
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.utils.call_phone.PhoneCallUtils
import vn.vistark.autocaller.views.campaign_detail.CampaignDetailActivity

class CampaignCall {
    companion object {

        var currentCampaignData: CampaignDataModel? = null

        fun start(context: CampaignDetailActivity, campaignId: Int) {
            // Lấy chiến dịch hiện tại
            val currentCampaign = CampaignRepository(context).get(campaignId)

            // Nếu không có thì trả về thất bại
            if (currentCampaign == null) {
                context.startCallFail()
                return
            }

            // Lấy mã của mẫu dữ liệu cuối cùng
            val lastCalledIndexData = currentCampaign.lastPhoneId

            // Tiến hành lấy dữ liệu tiếp theo
            val phones =
                CampaignDataRepository(context).getLimit(campaignId, lastCalledIndexData, 1)

            // Nếu không lấy được, tức là đã thành công hết
            if (phones.isEmpty()) {
                context.successCallAllPhone()
                return
            }

            // Lấy đối tượng số điện thoại ra
            val phone = phones.first()

            // Còn không, hiển thị thông tin số
            context.updateCallingInfo(phone)

            // Nếu số điện thoại trống hoặc quá ngắn
            if (phone.phone == null || phone.phone!!.isEmpty()) {
                phone.callState = PhoneCallState.PHONE_NUMBER_ERROR

                // Cập nhật progress
                context.initCampaignData()

                // Cập nhật trạng thái thực hiện cuộc gọi, tăng số index lên
                updateCallState(context, currentCampaign, phone)
                // Gọi số tiếp theo nếu được
                if (context.isStartCampaign)
                    start(context, campaignId)
                return
            }

            // Lưu tạm
            currentCampaignData = phone

            println(">>>>>> GỌI")

            // Gọi
            PhoneCallUtils.startCall(context, phone.phone!!)
        }

        fun updateCallState(
            context: AppCompatActivity,
            campaignModel: CampaignModel,
            campaignDataModel: CampaignDataModel
        ) {
            CampaignDataRepository(context).update(campaignDataModel)
            campaignModel.totalCalled++
            campaignModel.lastPhoneId = campaignDataModel.id
            CampaignRepository(context).update(campaignModel)
        }
    }
}