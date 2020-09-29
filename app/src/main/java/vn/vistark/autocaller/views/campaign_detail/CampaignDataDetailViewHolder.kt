package vn.vistark.autocaller.views.campaign_detail

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Build
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.PhoneCallState

class CampaignDataDetailViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    private val cDataRlRoot: RelativeLayout = v.findViewById(R.id.cDataRlRoot)
    private val cDataPhoneNumber: TextView = v.findViewById(R.id.cDataPhoneNumber)
    private val cDataCallState: TextView = v.findViewById(R.id.cDataCallState)

    @SuppressLint("SetTextI18n")
    fun bind(campaignViewDataModel: CampaignDataModel) {
        // Thứ tự
        val index = (campaignViewDataModel.indexInCampaign + 1).toString().padStart(5, '0')
        // gán số điện thoại vào
        cDataPhoneNumber.text = "#$index. ${campaignViewDataModel.phone}"

        // Đếu đã gọi thì gạch ngang số
        if (campaignViewDataModel.isCalled)
            cDataPhoneNumber.paintFlags = cDataPhoneNumber.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        // Đặt màu cho trạng thái
        setState(campaignViewDataModel.callState)
    }

    // Phương thức đặt màu cho trạng thái
    private fun setState(state: Int) {
        // Biến chứa mã màu trong app
        var colorId = -1

        // Biến chứa trạng thái cuộc gọi
        var stateString = "~~"

        when (state) {
            PhoneCallState.CALLED -> {
                colorId = R.color.successColor
                stateString = "Đã gọi"
            }
            PhoneCallState.PHONE_NUMBER_ERROR -> {
                colorId = R.color.successColor
                stateString = "Số lỗi"
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