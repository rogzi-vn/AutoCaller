package vn.vistark.autocaller.controller.campaign_detail

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import es.dmoral.toasty.Toasty
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.PhoneCallState
import vn.vistark.autocaller.models.repositories.BlackListRepository
import vn.vistark.autocaller.models.repositories.CampaignDataRepository
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.services.BackgroundService
import vn.vistark.autocaller.services.BackgroundService.Companion.isStartCampaign
import vn.vistark.autocaller.ui.campaign_detail.CampaignDetailActivity
import vn.vistark.autocaller.utils.call_phone.PhoneCallUtils
import java.util.*


class CampaignCall {
    companion object {

        var act: CampaignDetailActivity? = null
        var currentCampaignData: CampaignDataModel? = null

        fun playAudio(context: Context, audioId: Int) {
            val mPlayer: MediaPlayer = MediaPlayer.create(context, audioId)
            mPlayer.start()
        }

        fun Context.runHandler(f: (() -> Unit)) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                f.invoke()
            }
        }

        fun start(context: Context, campaignId: Int) {
            // Lấy chiến dịch hiện tại
            val currentCampaign = CampaignRepository(context).get(campaignId)

            // Nếu không có thì trả về thất bại
            if (currentCampaign == null) {
                try {
                    playAudio(context, R.raw.khoi_dong_cuoc_goi_khong_thanh_cong)
                    act?.startCallFail()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return
            }

            // Lấy mã của mẫu dữ liệu cuối cùng
            val lastCalledIndexData = currentCampaign.lastPhoneId

            // Tiến hành lấy dữ liệu tiếp theo
            val phones =
                CampaignDataRepository(context).getRandomForCall(campaignId)
            phones.shuffle()

            // Nếu không lấy được, tức là đã thành công hết
            if (phones.isEmpty()) {
                try {
                    playAudio(context, R.raw.hoan_tat_chien_dich)
                    act?.successCallAllPhone()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return
            }

            // Lấy đối tượng số điện thoại ra
            val phone = phones.first()

            // Còn không, hiển thị thông tin số
            try {
                act?.updateCallingInfo(phone)
                context.runHandler {
                    Toasty.info(
                        context,
                        "ĐANG GỌI SỐ ${phone.phone}",
                        Toasty.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Nếu số điện thoại trống hoặc quá ngắn
            if (phone.phone == null || phone.phone!!.isEmpty()) {
                phone.callState = PhoneCallState.PHONE_NUMBER_ERROR

                // Cập nhật progress
                try {
                    act?.initCampaignData()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Cập nhật trạng thái thực hiện cuộc gọi, tăng số index lên
                updateCallState(context, currentCampaign, phone)
                // Gọi số tiếp theo nếu được
                if (isStartCampaign)
                    start(context, campaignId)
                return
            }

            // Lưu tạm
            currentCampaignData = phone

            // Kiểm tra xem số điện thoại có trong danh sách đen không, nếu có thì tiến hành bỏ qua, không gọi
            val isIgnoreByBlackList =
                BlackListRepository(context).isHavePhone(phone.phone ?: "aaaa")
            val isIgnoreByPrefix = isIgnoreByPrefix(phone.phone ?: "aaa")
            if (isIgnoreByBlackList || isIgnoreByPrefix) {
                // Nếu có thì tiến hành bỏ qua
                currentCampaignData!!.callState = if (isIgnoreByBlackList)
                    PhoneCallState.BLACK_LIST_IGNORED
                else
                    PhoneCallState.SERVICE_PROVIDER_IGNORED
                currentCampaignData!!.isCalled = true
                updateCallState(
                    context,
                    BackgroundService.currentCampaign!!,
                    currentCampaignData!!
                )
                // Cập nhật progress
                try {
                    act?.initCampaignData()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

                if (isIgnoreByBlackList) {
                    playAudio(context, R.raw.bo_qua_vi_nam_trong_danh_sach_den)
                    context.runHandler {
                        Toasty.warning(
                            context,
                            "Bỏ qua vì nằm trong danh sách đen",
                            Toasty.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
//                    playAudio(context, R.raw.bo_qua_vi_dau_so_thuoc_nha_mang_khong_goi)
                    context.runHandler {
                        Toasty.warning(
                            context,
                            "Bỏ qua vì nằm trong nhà mạng không gọi",
                            Toasty.LENGTH_SHORT
                        )
                            .show()
                    }
                }

                //  Bắt đầu cuộc gọi tiếp theo sau DelayTimeInSeconds
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        this.cancel()
                        if (isStartCampaign || BackgroundService.isStopTemporarily)
                            start(
                                context,
                                BackgroundService.currentCampaign!!.id
                            )
                    }
                }, 1500L)

                return
            } else {
                // Nếu không có, gọi
                println(">>>>>> GỌI")
                // Gọi
                PhoneCallUtils.startCall(context, phone.phone!!)
            }
        }

        fun updateCallState(
            context: Context,
            campaignModel: CampaignModel,
            campaignDataModel: CampaignDataModel
        ) {
            CampaignDataRepository(context).update(campaignDataModel)
            campaignModel.totalCalled++
            campaignModel.lastPhoneId = campaignDataModel.id
            CampaignRepository(context).update(campaignModel)
        }

        fun isIgnoreByPrefix(phoneNumber: String): Boolean {
            AppStorage.ServiceProviders.forEach { sp ->
                if (sp.state) {
                    sp.phonePrefixs.forEach { _prefix ->
                        if (phoneNumber.startsWith(_prefix, true))
                            return true
                    }
                }
            }
            return false
        }


    }
}