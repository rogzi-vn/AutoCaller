package vn.vistark.autocaller.views.campaign

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_campaign.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.utils.call_phone.PhoneCallUtils
import vn.vistark.autocaller.views.campaign_create.CampaignCreateActivity

class CampaignActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)


    }

    // Khởi tạo và chèn menu vào thanh ứng dụng ở trên, bên phải
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_right, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.trMenuAddCampaign -> {
                return createNewCampaign()
            }
            else -> {
                Toasty.error(this, "Không tìm thấy tùy chọn này", Toasty.LENGTH_SHORT, true).show()
            }
        }
        return false
    }

    private fun createNewCampaign(): Boolean {
        val intent = Intent(this, CampaignCreateActivity::class.java)
        startActivityForResult(intent, CampaignCreateActivity.REQUEST_CODE)
        return true
    }

//    private fun checkPhoneAndCall() {
//        // lấy số người dùng nhập
//        val inpPhone = campaignEdtPhone.text.toString()
//
//        // Nếu không đủ 10 số
//        if (inpPhone.length < 10) {
//            // Thông báo
//            Toasty.error(
//                this,
//                "Số điện thoại quá ngắn.",
//                Toasty.LENGTH_SHORT,
//                true
//            ).show()
//            return
//        }
//
//        // Nếu chưa nhập
//        if (inpPhone.isEmpty()) {
//            // Thông báo
//            Toasty.error(
//                this,
//                "Bạn chưa nhập số điện thoại.",
//                Toasty.LENGTH_SHORT,
//                true
//            ).show()
//            return
//        }
//
//        // Sau khi OK, tiến hành gọi
//        PhoneCallUtils.startCall(this, inpPhone)
//    }
}