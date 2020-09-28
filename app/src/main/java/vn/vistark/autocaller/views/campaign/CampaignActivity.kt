package vn.vistark.autocaller.views.campaign

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_campaign.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.controller.campaign.CampaignController
import vn.vistark.autocaller.controller.campaign.CampaignLoader
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.utils.call_phone.PhoneCallUtils
import vn.vistark.autocaller.views.campaign_create.CampaignCreateActivity

class CampaignActivity : AppCompatActivity() {
    // Nơi chứa dữ liệu danh sách các chiến dịch
    val campaigns = ArrayList<CampaignModel>()

    // Adapter để hiển thị và điều khiển dữ liệu
    lateinit var adapter: CampaignAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)

        // Khởi tạo adapter
        adapter = CampaignAdapter(campaigns)

        // Khởi tạo RecyclerView để chứa danh sách
        campaignRvList.layoutManager = LinearLayoutManager(this)
        campaignRvList.setHasFixedSize(true)
        campaignRvList.adapter = adapter

        // Tiến hành load dữ liệu
        CampaignLoader(this)
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

    // Phương thức cập nhật, thêm mới chiến dịch vào danh sách
    fun addCampaign(campaignModel: CampaignModel) {
        campaignRvList.visibility = View.VISIBLE
        println("Đã thêm ${campaignModel.name}")
        campaigns.add(campaignModel)
        adapter.notifyDataSetChanged()
    }

    // Phương thức cho chạy loading
    fun showLoading() {
        campaignPbLoading.post {
            campaignPbLoading.visibility = View.VISIBLE
        }
    }

    // Phương thức cho ẩn loading
    fun hideLoading() {
        campaignPbLoading.post {
            campaignPbLoading.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Nếu kết quả trả về là của màn hình tạo chiến dịch, và kết quả thành công
        if (requestCode == CampaignCreateActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // Lấy mã của chiến dịch mới
            val newCampaignId = data.getIntExtra(CampaignModel.ID, -1)

            // Nếu mã không đúng, bỏ qua
            if (newCampaignId <= 0)
                return

            // Lấy chi tiết chiến dịch, nếu không có thì bỏ qua
            val campaign = CampaignRepository(this).get(newCampaignId) ?: return

            // Tiến hành thêm vào view hiển thị
            addCampaign(campaign)

        }
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