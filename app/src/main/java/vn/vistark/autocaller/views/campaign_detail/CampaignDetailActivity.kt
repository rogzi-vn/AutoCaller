package vn.vistark.autocaller.views.campaign_detail

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_campaign_create.*
import kotlinx.android.synthetic.main.activity_campaign_detail.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.controller.campaign_detail.CampaignCall
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.PhoneCallState
import vn.vistark.autocaller.models.repositories.CampaignDataRepository
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.utils.call_phone.PhoneStateReceiver
import vn.vistark.autocaller.views.campaign.CampaignActivity
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class CampaignDetailActivity : AppCompatActivity() {

    var campaign: CampaignModel? = null

    val campaignDatas = ArrayList<CampaignDataModel>()
    lateinit var adapter: CampaignDataDetailAdapter

    var lastPhoneIndex = 0

    var loading: SweetAlertDialog? = null

    lateinit var campaignDataRepository: CampaignDataRepository

    var isStartCampaign = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_detail)

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
    }


    fun pause() {
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


    fun cancelLoading() {
        if (loading != null && loading!!.isShowing) {
            loading?.dismissWithAnimation()
            loading?.cancel()
            loading = null
        }
    }

    fun showLoading() {
        cancelLoading()
        loading = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText("Đang tải dữ liệu, vui lòng đợi trong giây lát")
            .setContentText("ĐANG TẢI")
            .showCancelButton(false)
        loading?.setCancelable(false)
        loading?.show()
    }

    override fun onBackPressed() {
        val intent = Intent(this, CampaignActivity::class.java)
        startActivity(intent)
        finish()
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
    var broadcastReceiverWhenPhoneComming: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Ngưng chiến dịch
            pause()
        }
    }

    // Broad cast khi thực hiện nhá máy
    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
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
            }, 50)
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
            e.printStackTrace()
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

    fun updateCallingInfo(callingPhone: CampaignDataModel) {
        // Cập nhật số đang gọi và thứ tự
        acdCallingPhoneIndex.post {
            acdCallingPhoneIndex.text = "ĐANG GỌI SỐ THỨ ${callingPhone.indexInCampaign + 1}"
            acdCallingPhoneNumber.text = callingPhone.phone ?: "<Không xác định>"
        }
    }
}