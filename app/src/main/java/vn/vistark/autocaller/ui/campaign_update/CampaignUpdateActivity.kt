package vn.vistark.autocaller.ui.campaign_update

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import cn.pedant.SweetAlert.SweetAlertDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_campaign_create.*
import kotlinx.android.synthetic.main.activity_campaign_update.*
import kotlinx.android.synthetic.main.activity_campaign_update.campaignCreateBtnDataPick
import kotlinx.android.synthetic.main.activity_campaign_update.campaignCreateEdtDataPath
import kotlinx.android.synthetic.main.activity_campaign_update.campaignEdtCreateName
import kotlinx.android.synthetic.main.activity_campaign_update.campaignItemName
import kotlinx.android.synthetic.main.activity_campaign_update.campaignItemProgressBar
import kotlinx.android.synthetic.main.activity_campaign_update.campaignItemProgressCount
import kotlinx.android.synthetic.main.activity_campaign_update.campaignItemProgressPercent
import kotlinx.android.synthetic.main.activity_campaign_update.campaignRlImportLayout
import kotlinx.android.synthetic.main.activity_campaign_update.campaignSrvPickLayout
import kotlinx.android.synthetic.main.activity_campaign_update.campaignTvCreateName
import kotlinx.android.synthetic.main.activity_campaign_update.loginBtnConfirmButton
import kotlinx.android.synthetic.main.activity_campaign_update.loginEdtCancelButton
import vn.vistark.autocaller.R
import vn.vistark.autocaller.controller.campaign_update.CampaignUpdateLoader
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.ui.campaign_create.CampaignCreateActivity
import vn.vistark.autocaller.ui.campaign_detail.CampaignDetailActivity

class CampaignUpdateActivity : AppCompatActivity() {
    // Tên trước khi đổi
    var previousNamed = ""

    // Biến nhận loader
    var campaignUpdateLoader: CampaignUpdateLoader? = null

    // Biến chứa địa chỉ tệp mà người dùng đã chọn
    private var dataUri: Uri? = null

    // Biến chứa số lượng bản ghi
    var totalLines: Long = 0L

    // Đối tượng chiến dịch hiện tại
    var campaign: CampaignModel? = null

    // Khởi tạo hộp thoại loading
    var loading: SweetAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_update)

        loadCurrentCampaign()

        // Thiết lập tiêu đề
        supportActionBar?.title = "Cập nhật chiến dịch"

        // Hiển thị nút trở về
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Thiết lập các view (Kích thước,....)
        initViewSetUp()

        // Tự động lấy ID tiếp theo cho chiến dịch
        campaignTvCreateName.setText("Tên chiến dịch (#${campaign?.id})")

        // Tự động tạo tên cho tên chiến dịch
        campaignEdtCreateName.setText(campaign?.name)
        previousNamed = campaign?.name ?: ""
        campaignEdtCreateName.isEnabled = true
        campaignEdtCreateName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val crrText = campaignEdtCreateName.text.toString()
                ((crrText.trim()
                    .isNotEmpty() && crrText != previousNamed) || dataUri != null).also {
                    loginBtnConfirmButton.isEnabled = it
                }
            }
        })

        // Khởi tạo sụ kiện nhấn để chọn tệp tin
        initPickDataFileEvents()

        // Sự kiện khi nhấn nút xác nhận
        initConfirmEvents()

        // Sự kiện khi nhấn nút hủy nhập dũ liệu
        initCancelImportEvents()

        // Sự kiện xóa file đã chọn
        initClearImportFileEvent()

        // Chỉnh trạng thái hiển thị cho đúng
        campaignRlImportLayout.visibility = View.GONE
        campaignSrvPickLayout.visibility = View.VISIBLE
    }

    private fun initClearImportFileEvent() {
        acuTvClearImportFile.setOnClickListener {
            campaignCreateEdtDataPath.text.clear()
            dataUri = null
            // Mở khóa nút confirm
            acuTvClearImportFile.visibility = View.GONE

            val crrText = campaignEdtCreateName.text.toString()
            ((crrText.trim()
                .isNotEmpty() && crrText != previousNamed) || dataUri != null).also {
                loginBtnConfirmButton.isEnabled = it
            }
        }
    }

    private fun loadCurrentCampaign() {
        // Lấy mã được truyền của chiến dịch hiện tại
        val currentCampaignId = intent.getIntExtra(CampaignModel.ID, -1)
        if (currentCampaignId <= 0) {
            Toasty.error(this, "Chiến dịch không thể sửa", Toasty.LENGTH_SHORT, true).show()
            finish()
            return
        }

        // Load chi tiết chiến dịch từ trong CSDL
        campaign = CampaignRepository(this).get(currentCampaignId)

        // Nếu không lấy được thì tiến hành thông báo và bỏ qua
        if (campaign == null) {
            Toasty.error(
                this,
                "Không thể lấy chiến dịch cần sửa thông tin",
                Toasty.LENGTH_SHORT,
                true
            ).show()
            finish()
            return
        }
    }

    // Thiết lập các view
    private fun initViewSetUp() {
        campaignCreateEdtDataPath.post {
            val textBoxHeight = campaignCreateEdtDataPath.height
            val tempLp = campaignCreateBtnDataPick.layoutParams
            tempLp.width = textBoxHeight
            tempLp.height = textBoxHeight
            campaignCreateBtnDataPick.layoutParams = tempLp
        }
    }

    // Sự kiện khi nhấn vào các view có chức năng chọn file dữ liệu
    private fun initPickDataFileEvents() {
        campaignCreateBtnDataPick.setOnClickListener {
            pickDataFileAction()
        }
    }

    // Hành động chọn file dữ liệu
    private fun pickDataFileAction() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "text/plain"

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CampaignCreateActivity.PICK_DATA_FILE)
        } else {
            Toasty.error(this, "Không tìm thấy phần mềm quản lý tệp tin", Toasty.LENGTH_SHORT, true)
                .show()
        }
    }

    private fun initConfirmEvents() {
        loginBtnConfirmButton.setOnClickListener {
            if (dataUri != null)
            // Thực hiện animation thu nhỏ phần chọn file
                fadeOutPicker()
            else {
                campaign!!.name = campaignEdtCreateName.text.toString()
                if (CampaignRepository(this).update(campaign!!) > 0) {
                    Toasty.success(
                        this,
                        "Cập nhật thành công",
                        Toasty.LENGTH_SHORT,
                        true
                    )
                        .show()
                    onBackPressed()
                } else {
                    Toasty.error(
                        this,
                        "Cập nhật không thành công",
                        Toasty.LENGTH_SHORT,
                        true
                    )
                        .show()
                    onBackPressed()
                }
            }
        }
    }

    // Hiển thị phần tiến độ nhập dữ liệu
    private fun fadeInProgress() {
        campaignRlImportLayout.visibility = View.VISIBLE
        val fadeIn: Animation = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator() //add this
        fadeIn.duration = 300
        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                // Thêm tên của chiến dịch
                campaign!!.name = campaignEdtCreateName.text.toString()

                // Bắt đầu trình đếm
                campaignUpdateLoader = CampaignUpdateLoader(this@CampaignUpdateActivity, dataUri)
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })

        campaignRlImportLayout.startAnimation(fadeIn)
    }

    private fun cancelLoading() {
        if (loading != null && loading!!.isShowing) {
            loading!!.dismissWithAnimation()
            loading!!.cancel()
            loading = null
        }
    }

    fun hideLoading() {
        campaignItemName.post {
            cancelLoading()
        }
    }

    fun importFail(msgError: String = "Nhập dữ liệu không được, vui lòng thử lại hoặc tìm một tập dữ liệu khác thay thể") {
        // Đóng loading
        cancelLoading()

        // Hiển thị lại phần chọn file
        showPickerAgain()

        // Thông báo lỗi
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(msgError)
            .setContentText("NHẬP THẤT BẠI")
            .showCancelButton(true)
            .setCancelButton("Đóng") { sDialog ->
                sDialog.dismissWithAnimation()
                sDialog.cancel()
            }.show()
    }

    fun showLoading() {
        campaignItemName.post {
            cancelLoading()
            loading = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("Đang tính toán tổng số bản ghi dữ liệu")
                .setContentText("ĐANG XỬ LÝ")
                .showCancelButton(false)
                .apply {
                    setCancelable(false)
                }
            loading?.show()
        }
    }

    fun showSuccess(count: Long) {
        campaignItemName.post {
            cancelLoading()
            loading = SweetAlertDialog(
                this,
                SweetAlertDialog.SUCCESS_TYPE
            )
                .setTitleText("Nhập dữ liệu hoàn tất")
                .setContentText("$count/${totalLines}")
                .setConfirmText("Xem danh sách")
                .showCancelButton(false)
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    sDialog.cancel()
                    onBackPressed()
                }
            loading?.setCancelable(false)
            loading?.show()
        }
    }

    // Thu nhỏ phần chọn file
    private fun fadeOutPicker() {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator() //and this
        fadeOut.startOffset = 1000
        fadeOut.duration = 300
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                // Ẩn phần chọn
                campaignSrvPickLayout.visibility = View.GONE
                // Chạy animation phần progress
                fadeInProgress()
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })

        campaignSrvPickLayout.startAnimation(fadeOut)
    }

    // Ẩn phần tiếp độ nhập và hiển thị phần chọn file
    private fun showPickerAgain() {
        campaignSrvPickLayout.visibility = View.VISIBLE
        campaignRlImportLayout.visibility = View.GONE
    }

    private fun initCancelImportEvents() {
        loginEdtCancelButton.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Toàn bộ dữ liệu đang được nhập và chiến dịch hiện tại sẽ bị hủy và có khả năng xảy ra lỗi?")
                .setContentText("HỦY NHẬP DỮ LIỆU")
                .setCancelText("Quay lại")
                .setConfirmText("Vẫn hủy")
                .showCancelButton(true)
                .setCancelClickListener { sDialog -> sDialog.cancel() }
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    sDialog.cancel()
                    campaignUpdateLoader?.campaignUpdateATL?.cancelLoadInBackground()
                }
                .show()
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateProgressState(campaignDataModel: CampaignDataModel, count: Long) {
        campaignItemName.post {
            campaignItemName.text = "Đang nhập số: ${campaignDataModel.phone}"
            campaignItemProgressCount.text = "($count/$totalLines)"
            val progress = (count.toDouble() / totalLines.toDouble()) * 100
            campaignItemProgressBar.progress = progress.toInt()
            campaignItemProgressPercent.text = campaignItemProgressBar.progress.toString() + "%"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Nếu là yêu cầu pick file của app và thành công
        if (requestCode == CampaignCreateActivity.PICK_DATA_FILE && resultCode == Activity.RESULT_OK) {
            if (data == null)
                return

            // Lưu uri
            dataUri = data.data

            // Cập nhật đường dẫn vào TextBox
            campaignCreateEdtDataPath.setText(dataUri?.path ?: "Đã chọn thành công")
            // Mở khóa nút confirm
            loginBtnConfirmButton.isEnabled = true
            // Hiện nút xóa
            acuTvClearImportFile.visibility = View.VISIBLE
            // Trở lên
            return
        }

        // Nếu là yêu cầu pick file của app nhưng lại không thành công
        if (requestCode == CampaignCreateActivity.PICK_DATA_FILE && resultCode != Activity.RESULT_OK)
            return
    }

    // Khi nhấn nút trở về
    override fun onSupportNavigateUp(): Boolean {
        if (campaignRlImportLayout.visibility == View.VISIBLE)
            loginEdtCancelButton.performClick()
        else
            onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val intent = Intent(this, CampaignDetailActivity::class.java)
        intent.putExtra(CampaignModel.ID, campaign?.id ?: -1)
        startActivity(intent)
        finish()
    }

}