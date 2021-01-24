package vn.vistark.autocaller.ui.campaign

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_campaign.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.controller.campaign.CampaignLoader
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.ui.campaign_create.CampaignCreateActivity
import vn.vistark.autocaller.ui.campaign_detail.CampaignDetailActivity
import vn.vistark.autocaller.ui.setting.SettingActivity

class CampaignActivity : AppCompatActivity() {
    // Nơi chứa dữ liệu danh sách các chiến dịch
    val campaigns = ArrayList<CampaignModel>()

    // Adapter để hiển thị và điều khiển dữ liệu
    lateinit var adapter: CampaignAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)

        // Ẩn thanh loading tải danh sách chiến dịch
        hideLoading()

        // Khởi tạo adapter
        adapter = CampaignAdapter(campaigns)

        // Sự kiện xem thông tin khi nhấn vào adapter
        showDetailEvent()

        // Sự kiện nhấn giữ adapter
        removeCampaignEvent()

        // Khởi tạo RecyclerView để chứa danh sách
        campaignRvList.layoutManager = LinearLayoutManager(this)
        campaignRvList.setHasFixedSize(true)
        campaignRvList.adapter = adapter

        // Tiến hành load dữ liệu
        CampaignLoader(this)
    }

    private fun showDetailEvent() {
        adapter.onClick = { campaign ->
            val intent = Intent(this, CampaignDetailActivity::class.java)
            intent.putExtra(CampaignModel.ID, campaign.id)
            startActivity(intent)
            /// Đóng activity hiện tại cho nhẹ
            finish()
        }
    }

    private fun removeCampaignEvent() {
        adapter.onLongClick = { campaign ->
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Bạn có muốn xóa chiến dịch này cùng dữ liệu liên quan?")
                .setContentText("XÓA CHIẾN DỊCH")
                .showCancelButton(true)
                .setCancelButton("Không xóa") {
                    it.dismissWithAnimation()
                    it.cancel()
                }
                .setConfirmButton("Xóa") {
                    it.dismissWithAnimation()
                    it.cancel()
                    removeCampaign(campaign)
                }
                .show()

        }
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
            R.id.trMenuSetting -> {
                return settingApp()
            }
            else -> {
                Toasty.error(this, "Không tìm thấy tùy chọn này", Toasty.LENGTH_SHORT, true).show()
            }
        }
        return false
    }


    // Khởi động màn hình thiết lập
    private fun settingApp(): Boolean {
        val intent = Intent(this, SettingActivity::class.java)
        startActivity(intent)
        return true
    }

    // Khởi động màn hình tạo mới chiến dịch
    private fun createNewCampaign(): Boolean {
        val intent = Intent(this, CampaignCreateActivity::class.java)
        startActivityForResult(intent, CampaignCreateActivity.REQUEST_CODE)
        return true
    }

    // Phương thức cập nhật, thêm mới chiến dịch vào danh sách
    fun addCampaign(campaignModel: CampaignModel) {
        campaigns.add(campaignModel)
        runOnUiThread {
            if (campaignRvList.visibility != View.VISIBLE)
                campaignRvList.visibility = View.VISIBLE
            adapter.notifyDataSetChanged()
            updateCount()
        }
    }

    private fun removeCampaign(campaignModel: CampaignModel) {
        val res = CampaignRepository(this).remove(campaignModel.id.toLong())
        if (res <= 0) {
            Toasty.error(this, "Xóa chiến dịch không thành công", Toasty.LENGTH_SHORT, true)
                .show()
        } else {
            val index = campaigns.indexOfFirst { it.id == campaignModel.id }
            if (index < 0) {
                return
            }
            campaigns.removeAt(index)
            adapter.notifyDataSetChanged()
            updateCount()
            Toasty.success(this, "Xóa chiến dịch thành công", Toasty.LENGTH_SHORT, true).show()
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateCount() {
        campaignTvCount.text = "Danh sách chiến dịch đã tạo (${campaigns.size})"
    }

    // Phương thức cho chạy loading
    fun showLoading() {
        campaignPbLoading.visibility = View.VISIBLE
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

}