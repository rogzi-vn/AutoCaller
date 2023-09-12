package vn.vistark.autocaller.ui.campaign_detail

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main._campaign_item.*
import kotlinx.android.synthetic.main.activity_campaign_create.campaignItemName
import kotlinx.android.synthetic.main.activity_campaign_create.campaignItemProgressBar
import kotlinx.android.synthetic.main.activity_campaign_create.campaignItemProgressCount
import kotlinx.android.synthetic.main.activity_campaign_create.campaignItemProgressPercent
import kotlinx.android.synthetic.main.activity_campaign_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import vn.vistark.autocaller.DefaultExceptionHandler
import vn.vistark.autocaller.R
import vn.vistark.autocaller.controller.campaign_detail.CampaignCall
import vn.vistark.autocaller.controller.campaign_detail.CampaignResetLoader
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.repositories.CampaignDataRepository
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.services.BackgroundServiceCompanion
import vn.vistark.autocaller.services.BackgroundServiceCompanion.Companion.IsBackgroundServiceRunning
import vn.vistark.autocaller.services.BackgroundServiceCompanion.Companion.StartBackgroundService
import vn.vistark.autocaller.services.BackgroundServiceCompanion.Companion.StopBackgroundService
import vn.vistark.autocaller.services.BackgroundServiceCompanion.Companion.isStartCampaign
import vn.vistark.autocaller.services.BackgroundServiceCompanion.Companion.isStopTemporarily
import vn.vistark.autocaller.ui.campaign.CampaignActivity
import vn.vistark.autocaller.ui.campaign_update.CampaignUpdateActivity
import vn.vistark.autocaller.utils.SPUtils
import vn.vistark.autocaller.utils.call_phone.PhoneStateReceiver
import java.io.File
import java.io.FileOutputStream


class CampaignDetailActivity : AppCompatActivity() {

    var campaign: CampaignModel? = null

    val campaignDatas = ArrayList<CampaignDataModel>()
    lateinit var adapter: CampaignDataDetailAdapter

    var lastPhoneIndex = 0

    lateinit var campaignDataRepository: CampaignDataRepository

    companion object {
        var crrCampaignDetail: CampaignDetailActivity? = null

        fun stopAndExitApplication() {
            Log.e("ERROR_CEPTION", "1455" )
            crrCampaignDetail?.pause()
            crrCampaignDetail?.StopBackgroundService()
            crrCampaignDetail?.finish()
            //Stopping application
            System.exit(0)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_detail)

        // Khởi tạo các hằng số cơ bản và bộ nhớ lưu trữ cục bộ
        SPUtils.init(this)

        // Thao tác sẽ đựợc apply sau khi ứng dụng được khởi động lại
        if (AppStorage.IsAutoReopenAppIfShutdownSuddenly) {
            Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler(this))
        }

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

        // Sự kiện khi nhấn chỉnh sửa chiến dịch
        editBtnEvent()

        // SK xuất file
        exportBtnEvent()

        // Load 200 record đầu
        loadMore()

        // Nếu có sự kiện khởi động chiến dịch ngay
        try {
            if (intent.getBooleanExtra("IsStartNow", false)) {
                acdBtnStart.performClick()
            }
        } catch (_: java.lang.Exception) {

        }

        crrCampaignDetail = this
    }

    private fun editBtnEvent() {
        cItemLnRoot.setOnClickListener {
            campaignTvOption.performClick()
        }
        campaignTvOption.setOnClickListener {
            // Tạm ngưng việc chạy chiến dịch để thực hiện việc chỉnh sửa
            if (acdBtnPause.isEnabled)
                acdBtnPause.performClick()

            // Khởi chạy trang chỉnh sửa
            val intent = Intent(this, CampaignUpdateActivity::class.java)
            intent.putExtra(CampaignModel.ID, campaign?.id ?: -1)
            startActivity(intent)

            // Kết thúc trang thông tin hiện tại cho nhẹ
            finish()
        }
    }

    fun normalizeFileName(input: String, replacementChar: Char = '_'): String {
        val invalidChars = charArrayOf('\\', '/', ':', '*', '?', '"', '<', '>', '|')

        var result = input
        for (invalidChar in invalidChars) {
            result = result.replace(invalidChar, replacementChar)
        }

        // Loại bỏ khoảng trắng ở đầu và cuối chuỗi
        result = result.trim()

        // Thay thế khoảng trắng và các ký tự khoảng trắng bằng replacementChar
        result = result.replace("\\s+".toRegex(), replacementChar.toString())

        return result
    }

    private fun exportFileTxt() {
        val filename =
            "[${System.currentTimeMillis()}}]${normalizeFileName(campaign?.name ?: "")}.txt";
        try {


            // Lấy đường dẫn của thư mục tài liệu của ứng dụng
            val documentDir = this.getExternalFilesDir(null)

            // Tạo một tệp tin với đường dẫn đầy đủ
            val file = File(documentDir, filename)

            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Tiến hành xuất tệp tại [${file!!.path}]?")
                .setContentText("XUẤT DỮ LIỆU CHIẾN DỊCH?")
                .showCancelButton(true)
                .setCancelButton("Hủy thao tác") {
                    it.dismissWithAnimation()
                    it.cancel()
                }
                .setConfirmButton("Xác nhận") {
                    it.dismissWithAnimation()
                    it.cancel()


                    // Hiển thị hộp thoại SweetAlertLoading
                    val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                    loadingDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
                    loadingDialog.titleText = "Đang tiến hành xuất tệp $file"
                    loadingDialog.setCancelable(false)
                    loadingDialog.show()

                    // Sử dụng Coroutine để thực hiện việc lưu file trong luồng khác
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            // Process
                            val campaignDetails = campaignDataRepository.getAll(campaign!!.id)
                            var textBuilder = ""
                            campaignDetails.forEach { cpgDt ->
                                textBuilder += "${cpgDt.phone}|${cpgDt.receivedSignalTimeInMilliseconds / 1000.0}\n"
                            }

                            // Mở tệp tin để ghi dữ liệu
                            val fileOutputStream = FileOutputStream(file)

                            // Ghi dữ liệu vào tệp tin
                            fileOutputStream.write(textBuilder.toByteArray())

                            // Đóng tệp tin
                            fileOutputStream.close()

                            // Ẩn hộp thoại SweetAlertLoading sau khi lưu thành công
                            campaignTvOptionExportDataTxt.post {
                                loadingDialog.dismissWithAnimation()
                                loadingDialog.cancel()
                            }

                        } catch (e: Exception) {
                            // Xử lý lỗi (có thể thay đổi hoặc bỏ qua)
                            e.printStackTrace()

                            // Ẩn hộp thoại SweetAlertLoading nếu có lỗi
                            campaignTvOptionExportDataTxt.post {
                                loadingDialog.dismissWithAnimation()
                                loadingDialog.cancel()
                            }
                        }
                    }
                }.show()

        } catch (e: Exception) {
            // Xử lý lỗi (có thể thay đổi hoặc bỏ qua)
            e.printStackTrace()
        }
    }

    private fun exportBtnEvent() {
        campaignTvOptionExportDataTxt.setOnClickListener {
            // Tạm ngưng việc chạy chiến dịch để thực hiện việc chỉnh sửa
            if (acdBtnPause.isEnabled)
                acdBtnPause.performClick()

            exportFileTxt()
        }
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
        if (IsBackgroundServiceRunning()) {
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

    private fun stopRegisReciver() {
        try {
            unregisterReceiver(BackgroundServiceCompanion.broadcastReceiver)
        } catch (_: Exception) {
        }
    }

    fun pause(isLoadMore: Boolean = true) {
        isStartCampaign = false
        // Bỏ đăng ký nghe khi xong cuộc gọi
        stopRegisReciver()

        StopBackgroundService()

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
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Bạn có thực sự muốn dừng chiến dịch hiện tại?")
                .setContentText("DỪNG CHIẾN DỊCH")
                .setCancelText("Quay lại")
                .setConfirmText("Dừng ngay")
                .showCancelButton(true)
                .setCancelClickListener { sDialog -> sDialog.cancel() }
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    sDialog.cancel()
                    pause()
                }
                .show()
        }
    }

    private fun startBtnEvent() {
        acdBtnStart.setOnClickListener {

            StartBackgroundService(campaign!!)

            acdBtnPause.isEnabled = true
            acdBtnStart.isEnabled = false

            cDataRvListPhone.visibility = View.GONE
            acdRlCallShowing.visibility = View.VISIBLE
            lastPhoneIndex = 0
            campaignDatas.clear()
            adapter.notifyDataSetChanged()

        }
    }

    override fun onStart() {
        super.onStart()
        CampaignCall.act = this
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
        pause(false)
        val intent = Intent(this, CampaignActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
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

    override fun onDestroy() {
        super.onDestroy()
        CampaignCall.act = null
    }

    fun successCallAllPhone() {
        pause()
        acdBtnPause.post {
            acdBtnPause.isEnabled = false
            acdBtnStart.isEnabled = false

            if (AppStorage.TimerAutoRunCampaignInSeconds != 0) {
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Hoàn tất chiến dịch")
                    .setContentText("CHÚC MỪNG")
                    .setConfirmButton("Xác nhận") {
                        it.dismissWithAnimation()
                        it.cancel()
                    }.show()
            } else {
                Toast.makeText(this, "CHÚC MỪNG: Hoàn tất chiến dịch", Toast.LENGTH_SHORT).show()
                goBack()
            }
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