package vn.vistark.autocaller.ui.sync_campaign_online

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import androidx.core.widget.doOnTextChanged
import cn.pedant.SweetAlert.SweetAlertDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_sync_campaign_online.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.controller.campaign_detail.CampaignCall
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.DatabaseContext
import vn.vistark.autocaller.models.PhoneCallState
import vn.vistark.autocaller.models.repositories.CampaignDataRepository
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.ui.campaign.CampaignActivity
import java.net.URL

class SyncCampaignOnline : AppCompatActivity() {

    private var totalLines: Long = 0
    private var currentLines: Long = 0
    private var onlineCampaigns: ArrayList<OnlineCampaign> = ArrayList()
    private var successCampaignCount = 0
    private var successLineCount = 0

    private var regexCommandDetect = "((?<![^\"]),)|(,(?![^\"]))|(,(?![^\"]*\"))".toRegex()

    // Khởi tạo hộp thoại loading
    var loading: SweetAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync_campaign_online)

        // Thiết lập tiêu đề
        supportActionBar?.title = "Đồng bộ chiến dịch online"

        // Hiển thị nút trở về
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        initEdittext()
        initViewState()
        initEvents()
    }

    private fun initEdittext() {
        dataLink.setText(AppStorage.GoogleSheetCampaigns.trim())
        dataLink.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                confirmBtn.isEnabled = !dataLink.text.trim().isEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun initEvents() {
        confirmBtn.setOnClickListener {
            AppStorage.GoogleSheetCampaigns = dataLink.text.toString().trim()
            syncCampaignConfirm()
        }
    }

    fun initViewState() {
        blacklistRlImportLayout.visibility = View.GONE
        lnInputDataSheetUri.visibility = View.VISIBLE
        confirmBtn.isEnabled = !dataLink.text.trim().isEmpty()
    }

    fun syncCampaignConfirm() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Bạn có thực sự muốn đồng bộ dữ liệu chiến dịch trực tuyến vào danh sách cùng với các chiến dịch hiện tại?")
            .setContentText("ĐỒNG BỘ CHIẾN DỊCH")
            .setCancelText("Quay lại")
            .setConfirmText("Đồng bộ")
            .showCancelButton(true)
            .setCancelClickListener { sDialog -> sDialog.cancel() }
            .setConfirmClickListener { sDialog ->
                sDialog.dismissWithAnimation()
                sDialog.cancel()
                preSync()
                syncDataFromGoogleSheet()
            }
            .show()
    }

    fun fadeOutUriInput() {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator() //and this
        fadeOut.startOffset = 1000
        fadeOut.duration = 300
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                // Ẩn phần chọn
                lnInputDataSheetUri.visibility = View.GONE
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })
        lnInputDataSheetUri.startAnimation(fadeOut)
    }

    fun fadeInProgress() {
        blacklistRlImportLayout.visibility = View.VISIBLE
        val fadeIn: Animation = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator() //add this
        fadeIn.duration = 300
        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })

        blacklistRlImportLayout.startAnimation(fadeIn)
    }

    fun preSync() {
        fadeOutUriInput()
        fadeInProgress()
    }

    fun syncDataFromGoogleSheet() {
        blacklistRlImportLayout.post {
            loading = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("Đang tính toán tổng số bản ghi dữ liệu")
                .setContentText("ĐANG XỬ LÝ")
                .showCancelButton(false)
                .apply {
                    setCancelable(false)
                }
            loading?.show()
        }
        Thread {
            // Count
            try {
                val loadedContent: String = URL(AppStorage.GoogleSheetCampaigns).readText()
                if (loadedContent.isNotEmpty()) {
                    val temp = loadedContent.split("\r\n")
                    var isFirstLine = true
                    temp.forEach {
                        if (isFirstLine) {
                            it.split(regexCommandDetect).forEach { cName ->
                                onlineCampaigns.add(
                                    OnlineCampaign(
                                        cName.replace("(^\")|(\"\$)".toRegex(), "").trim(),
                                        ArrayList()
                                    )
                                )
                            }
                            isFirstLine = false
                        } else {
                            val phoneNumbers = it.split(regexCommandDetect)
                            for (i in phoneNumbers.indices) {
                                if ("\\d{8,12}".toRegex()
                                        .matches(phoneNumbers[i].trim()) && i < onlineCampaigns.size
                                ) {
                                    onlineCampaigns[i].phoneNumbers.add(phoneNumbers[i].trim())
                                    totalLines++
                                }
                            }
                        }
                    }
                }
                // Hide loading
                blacklistRlImportLayout.post {
                    loading?.dismissWithAnimation()
                }
                // Sync to db
                syncCampaigns()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun syncCampaigns() {
        // Kho
        onlineCampaigns.forEach {
            // Create campaign
            val campaign = CampaignModel(name = it.name)
            campaign.id = CampaignRepository(this).add(campaign).toInt()
            // Nếu chiến dịch lưu chưa thành công, báo lỗi và trở lên
            if (campaign.id <= 0) {
                Toasty.error(
                    this,
                    "Không thể tạo chiến dịch",
                    Toasty.LENGTH_SHORT,
                    true
                ).show()
                return
            }
            val db = DatabaseContext(this).writableDatabase
            db.beginTransaction()

            var index = 0
            var success = 0
            // Import data to campaign
            it.phoneNumbers.forEach { phoneNumber ->
                // Khởi tạo dữ liệu
                val campaignData =
                    CampaignDataModel(
                        0,
                        campaign.id,
                        phoneNumber,
                        PhoneCallState.NOT_CALL,
                        index++,
                        false
                    )

                // Tạo bộ dữ liệu nhập
                val values = CampaignDataRepository.createDataValues(campaignData)

                // Lưu dữ liệu và đếm nếu thành công

                // Gọi phương thức nhập chung
                if (db.insertWithOnConflict(
                        CampaignDataModel.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE
                    ) > 0
                ) {
                    successLineCount++
                    success++
                }

                updateProgressState(phoneNumber, 1 + currentLines++)
            }

            // Cập nhập tổng số SĐT đã import vào danh mục
            campaign.totalImported = success

            // Thông báo là transact đã thành công
            db.setTransactionSuccessful()

            // Kết thúc transact
            db.endTransaction()

            // Đóng database lại
            db.close()

            // Lưu lại vào CSDL
            CampaignRepository(this).update(campaign)

            successCampaignCount++
        }

        blacklistItemName.post {
            CampaignCall.playAudio(this, R.raw.dong_bo_chien_dich_thanh_con)
            loading = SweetAlertDialog(
                this,
                SweetAlertDialog.SUCCESS_TYPE
            )
                .setTitleText("Nhập dữ liệu hoàn tất")
                .setContentText("$successLineCount/${totalLines}")
                .setConfirmText("Quay lại")
                .showCancelButton(false)
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    sDialog.cancel()

                    runOnUiThread {
                        goBack()
                    }
                }
            loading?.setCancelable(false)
            loading?.show()
        }
    }

    fun goBack() {
        val intent = Intent(this, CampaignActivity::class.java)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    fun updateProgressState(phone: String, count: Long) {
        blacklistItemName.post {
            blacklistItemName.text = "Đang nhập số: ${phone}"
            campaignItemProgressCount.text = "($count/$totalLines)"
            val progress = (count.toDouble() / totalLines.toDouble()) * 100
            campaignItemProgressBar.progress = progress.toInt()
            campaignItemProgressPercent.text = campaignItemProgressBar.progress.toString() + "%"
        }
    }

    // Trở về khi nhấn nút back
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val intent = Intent(this, CampaignActivity::class.java)
        startActivity(intent)
        finish()
    }
}