package vn.vistark.autocaller.views.campaign

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_campaign.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.utils.call_phone.PhoneCallUtils

class CampaignActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)

        // Xét sự kiện
        campaignBtnConfirm.setOnClickListener {
            checkPhoneAndCall()
        }
    }

    private fun checkPhoneAndCall() {
        // lấy số người dùng nhập
        val inpPhone = campaignEdtPhone.text.toString()

        // Nếu không đủ 10 số
        if (inpPhone.length < 10) {
            // Thông báo
            Toasty.error(
                this,
                "Số điện thoại quá ngắn.",
                Toasty.LENGTH_SHORT,
                true
            ).show()
            return
        }

        // Nếu chưa nhập
        if (inpPhone.isEmpty()) {
            // Thông báo
            Toasty.error(
                this,
                "Bạn chưa nhập số điện thoại.",
                Toasty.LENGTH_SHORT,
                true
            ).show()
            return
        }

        // Sau khi OK, tiến hành gọi
        PhoneCallUtils.startCall(this, inpPhone)
    }
}