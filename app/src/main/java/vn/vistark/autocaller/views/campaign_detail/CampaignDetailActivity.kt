package vn.vistark.autocaller.views.campaign_detail

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_campaign_create.*
import kotlinx.android.synthetic.main.activity_campaign_detail.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.controller.campaign_detail.CampaignCall
import vn.vistark.autocaller.controller.campaign_detail.CampaignResetATL
import vn.vistark.autocaller.controller.campaign_detail.CampaignResetLoader
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.PhoneCallState
import vn.vistark.autocaller.models.repositories.CampaignDataRepository
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.utils.call_phone.PhoneStateReceiver
import vn.vistark.autocaller.views.campaign.CampaignActivity
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class CampaignDetailActivity : AppCompatActivity() {

    companion object {
        var isStopTemporarily = false
    }

    var campaign: CampaignModel? = null

    val campaignDatas = ArrayList<CampaignDataModel>()
    lateinit var adapter: CampaignDataDetailAdapter

    var lastPhoneIndex = 0

    lateinit var campaignDataRepository: CampaignDataRepository

    var isStartCampaign = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_detail)

        // Thiết lập răng đây là lần đầu cho cuộc gọi
        PhoneStateReceiver.isFirstTime = true

        // Thiết lập tiêu đề
        supportActionBar?.title = "Thông tin chiến dịch"

        // Hiển thị nút trở về
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Khởi tạo lấy thông tin chiến dịch
        initCampaignData()

        // Lấy kho chứa số điện thoại
        campaignDataRepository = CampaignDataRepository(this)

        // Thiết lập adapter
        adapter = CampaignDataDetailAdapter(campaignDatas)

        // Thiết lập recycler view danh sách
        cDataRvListPhone.layoutManager = LinearLayoutManager(this)
        cDataRvListPhone.setHasFixedSize(true)
        cDataRvListPhone.adapter = adapter

        // Gọi sự kiện khi kéo đến gần cuối danh sách
        initLoadMoreEvents()

        // Tắt nút tạm ngưng
        acdBtnPause.isEnabled = false
        acdBtnStart.isEnabled =
            campaign != null && campaign!!.totalCalled != campaign!!.totalImported

        // Sự kiện khi nhấn nút bắt đầu
        startBtnEvent()

        // Sự kiện khi nhấn nút tạm ngưng
        pauseBtnEvent()

        // Load 200 record đầu
        loadMore()

        // Đăng ký broadcast khi có cuộc gọi đến để tạm ngừng chiến dịch
        registerReceiver(
            broadcastReceiverWhenPhoneComming,
            IntentFilter(PhoneStateReceiver.INCOMMING_CALL)
        )

        // Đăng ký broadcast để tái khởi động chiến dịch khi cuộc gọi đến kết thúc
        registerReceiver(
            broadcastStopTemporarilyDone,
            IntentFilter(PhoneStateReceiver.STOP_TEMPORARILY_DONE)
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_right_campaign_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.trMenuReloadCampaign -> {
                return confirmResetCampaign()
            }
            android.R.id.home -> {
                onBackPressed()
            }
            else -> {
                Toasty.error(this, "Không tìm thấy tùy chọn này", Toasty.LENGTH_SHORT, true).show()
            }
        }
        return false
    }

    private fun confirmResetCampaign(): Boolean {
        if (isStartCampaign || isStopTemporarily) {
            Toasty.error(
                this,
                "Vui lòng TẠM NGƯNG chiến dịch trước khi thao tác",
                Toasty.LENGTH_SHORT
            ).show()
            return true
        }
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Khi thực hiện thao tác này toàn bộ dữ liệu cuộc gọi sẽ được làm mới và trở về trạng thái chưa gọi và bạn không thể hoàn tác. Bạn có chắc muốn thiết lập lại?")
            .setContentText("THIẾT LẬP LẠI")
            .showCancelButton(true)
            .setCancelButton("Hủy thao tác") {
                it.dismissWithAnimation()
                it.cancel()
            }
            .setConfirmButton("Xác nhận") {
                it.dismissWithAnimation()
                it.cancel()
                CampaignResetLoader(this)
            }.show()
        return true
    }

    fun pause(isLoadMore: Boolean = true) {
        isStartCampaign = false
        // Bỏ đăng ký nghe khi xong cuộc gọi
        stopRegisReciver()

        // Điều chỉnh view
        runOnUiThread {
            acdBtnPause.isEnabled = false
            acdBtnStart.isEnabled = true

            cDataRvListPhone.visibility = View.VISIBLE
            acdRlCallShowing.visibility = View.GONE
        }

        // Nếu cho phép load thêm dữ liệu (Mặc định)
        if (isLoadMore)
            loadMore()
    }

    private fun pauseBtnEvent() {
        acdBtnPause.setOnClickListener {
            pause()
        }
    }

    private fun startBtnEvent() {
        acdBtnStart.setOnClickListener {
            isStartCampaign = true

            acdBtnPause.isEnabled = true
            acdBtnStart.isEnabled = false

            cDataRvListPhone.visibility = View.GONE
            acdRlCallShowing.visibility = View.VISIBLE
            lastPhoneIndex = 0
            campaignDatas.clear()
            adapter.notifyDataSetChanged()

            // Đăng ký nghe khi xong cuộc gọi
            registerReceiver(broadcastReceiver, IntentFilter(PhoneStateReceiver.NAME))

            // Bắt đầu cuộc gọi
            CampaignCall.start(this, campaign!!.id)
        }
    }

    @SuppressLint("SetTextI18n")
    fun initCampaignData() {
        var campaignId = 0
        if (campaign == null) {
            campaignId = intent.getIntExtra(CampaignModel.ID, -1)
            if (campaignId <= 0) {
                Toasty.error(
                    this,
                    "Không thể xác định được chiến dịch cần xem thông tin",
                    Toasty.LENGTH_SHORT,
                    true
                ).show()
                onBackPressed()
                return
            }
        } else
            campaignId = campaign!!.id

        campaign = CampaignRepository(this).get(campaignId)

        if (campaign == null) {
            Toasty.error(
                this,
                "Không thể lấy chiến dịch cần xem thông tin",
                Toasty.LENGTH_SHORT,
                true
            ).show()
            onBackPressed()
            return
        }

        // Hiển thị
        campaignItemName.text = campaign!!.name
        campaignItemProgressCount.text =
            "(${campaign!!.totalCalled}/${campaign!!.totalImported})"

        val progress: Int =
            ((campaign!!.totalCalled.toDouble() / campaign!!.totalImported.toDouble()) * 100).toInt()
        campaignItemProgressBar.progress = progress
        campaignItemProgressPercent.text = "$progress%"
    }

    private fun initLoadMoreEvents() {
        cDataRvListPhone.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    loadMore()
                }
            }
        })
    }

    private fun loadMore() {
        val phones = campaignDataRepository.getLimit(campaign!!.id, lastPhoneIndex, 100)

        if (phones.isEmpty())
            return

        lastPhoneIndex = phones[phones.size - 1].id
        campaignDatas.addAll(phones)
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }


    fun goBack() {
        isStopTemporarily = false
        pause(false)
        val intent = Intent(this, CampaignActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (isStartCampaign || isStopTemporarily) {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Chiến dịch đang diễn ra, bạn có thực sự muốn tạm ngưng và quay về?")
                .setContentText("VỀ DANH SÁCH")
                .showCancelButton(true)
                .setCancelButton("Không") {
                    it.dismissWithAnimation()
                    it.cancel()
                }
                .setConfirmButton("Đồng ý") {
                    it.dismissWithAnimation()
                    it.cancel()
                    goBack()
                }.show()
        } else {
            goBack()
        }
    }

    // Khi nhấn nút trở về
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    fun startCallFail() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Khởi động cuộc gọi không thành công")
            .setContentText("KHÔNG THÀNH CÔNG")
            .showCancelButton(true)
            .setCancelButton("Đóng") { d ->
                d.dismissWithAnimation()
                d.cancel()
            }.show()
        pause()
    }

    // Broad cast khi có cuộc gọi đến
    private var broadcastReceiverWhenPhoneComming: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Ngưng chiến dịch khi có cuộc gọi đến
                pause()
                // Cho biết đây chỉ là hành động ngưng tạm thời
                isStopTemporarily = true
            }
        }

    // Broad cast nhận lệnh tái khởi động khi cuộc gọi đến đã kết thúc
    private var broadcastStopTemporarilyDone: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Tái khởi động chiến dịch sau vài giây
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    // Kết thúc timer này
                    this.cancel()
                    // Bấm nút tái khởi động nếu chưa được bấm
                    if (acdBtnStart.isEnabled)
                        acdBtnStart.performClick()
                    // Cho biết không còn ngưng tạm thời nữa
                    isStopTemporarily = false
                }

            }, AppStorage.DelayTimeInSeconds * 1000L)
        }
    }

    // Broad cast khi thực hiện nhá máy
    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Cập nhật vào CSDL
            if (CampaignCall.currentCampaignData != null) {
                CampaignCall.currentCampaignData!!.callState = PhoneCallState.CALLED
                CampaignCall.currentCampaignData!!.isCalled = true
                CampaignCall.updateCallState(
                    this@CampaignDetailActivity,
                    campaign!!,
                    CampaignCall.currentCampaignData!!
                )
            }

            // Cập nhật progress
            initCampaignData()

            // Renew trạng thái
            PhoneStateReceiver.previousState = "EXTRA_STATE_IDLE"

            //  Bắt đầu cuộc gọi tiếp theo sau 2s
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    this.cancel()
                    CampaignCall.start(this@CampaignDetailActivity, campaign!!.id)
                }
            }, AppStorage.DelayTimeInSeconds * 1000L)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRegisReciver()
    }

    private fun stopRegisReciver() {
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
        }
    }

    fun successCallAllPhone() {
        pause()
        acdBtnPause.post {
            acdBtnPause.isEnabled = false
            acdBtnStart.isEnabled = false
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Hoàn tất chiến dịch")
                .setContentText("CHÚC MỪNG")
                .setConfirmButton("Xác nhận") {
                    it.dismissWithAnimation()
                    it.cancel()
                }.show()
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateCallingInfo(callingPhone: CampaignDataModel) {
        // Cập nhật số đang gọi và thứ tự
        acdCallingPhoneIndex.post {
            acdCallingPhoneIndex.text = "ĐANG GỌI SỐ THỨ ${callingPhone.indexInCampaign + 1}"
            acdCallingPhoneNumber.text = callingPhone.phone ?: "<Không xác định>"
        }
    }
}