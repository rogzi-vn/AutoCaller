package vn.vistark.autocaller.ui.campaign_detail

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.PhoneCallState
import kotlin.math.floor

class CampaignDataDetailViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    private val cDataRlRoot: RelativeLayout = v.findViewById(R.id.cDataRlRoot)
    private val cDataPhoneNumber: TextView = v.findViewById(R.id.cDataPhoneNumber)
    private val cDataCallState: TextView = v.findViewById(R.id.cDataCallState)
    private val cDataCallSignalTime: TextView = v.findViewById(R.id.cDataCallSignalTime)

    @SuppressLint("SetTextI18n")
    fun bind(campaignViewDataModel: CampaignDataModel) {
        // Thứ tự
        val index = (campaignViewDataModel.indexInCampaign + 1).toString().padStart(5, '0')
        // gán số điện thoại vào
        cDataPhoneNumber.text = "#$index. ${campaignViewDataModel.phone}"

        // Đặt màu cho trạng thái
        setState(campaignViewDataModel.callState)

        // Thời gian bắt tín hiệu
        cDataCallSignalTime.text = "${campaignViewDataModel.receivedSignalTimeInMilliseconds / 1000.0}"
    }

    // Phương thức đặt màu cho trạng thái
    private fun setState(state: Int) {
        // Biến chứa mã màu trong app
        var colorId = -1

        // Biến chứa trạng thái cuộc gọi
        var stateString = "~~"

        when (state) {
            PhoneCallState.CALLED -> {
//                cDataPhoneNumber.paintFlags =
//                    cDataPhoneNumber.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                colorId = R.color.successColor
                stateString = "Đã gọi"
            }

            PhoneCallState.PHONE_NUMBER_ERROR -> {
                colorId = R.color.successColor
                stateString = "Số lỗi"
            }

            PhoneCallState.BLACK_LIST_IGNORED -> {
                colorId = R.color.colorDanger
                stateString = "DS.Đen"
            }

            PhoneCallState.SERVICE_PROVIDER_IGNORED -> {
                colorId = R.color.colorWarning
                stateString = "DS.Mạng"
            }

            PhoneCallState.NO_SIGNAL -> {
                colorId = R.color.colorDanger
                stateString = "K.T hiệu"
            }

            else -> {
                colorId = R.color.colorSecondary
                stateString = "Chưa gọi"
            }
        }

        // Set trạng thái gọi
        cDataCallState.text = stateString

        // Đặt màu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cDataCallState.setTextColor(
                cDataCallState.resources.getColor(colorId, null)
            )
        } else {
            cDataCallState.setTextColor(
                cDataCallState.resources.getColor(colorId)
            )
        }
    }
}