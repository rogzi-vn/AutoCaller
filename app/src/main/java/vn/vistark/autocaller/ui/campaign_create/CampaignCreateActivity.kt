package vn.vistark.autocaller.ui.campaign_create

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_campaign_create.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import vn.vistark.autocaller.BuildConfig
import vn.vistark.autocaller.R
import vn.vistark.autocaller.controller.campaign_create.CampaignCreateController
import vn.vistark.autocaller.controller.campaign_create.CampaignCreateLoader
import vn.vistark.autocaller.models.CampaignDataModel
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.repositories.CampaignRepository
import java.io.File


// Animation: https://stackoverflow.com/questions/6796139/fade-in-fade-out-android-animation-in-java

class CampaignCreateActivity : AppCompatActivity() {
    // Biến nhận loader
    var campaignCreateLoader: CampaignCreateLoader? = null

    // Biến chứa địa chỉ tệp mà người dùng đã chọn
    private var dataUri: Uri? = null

    // Biến chứa số lượng bản ghi
    var totalLines: Long = 0L

    // Đối tượng chiến dịch hiện tại
    val campaign: CampaignModel = CampaignModel()

    // Khởi tạo hộp thoại loading
    var loading: SweetAlertDialog? = null

    // Lưu mã của chiến dịch hiện tại, sẽ có
    var currentCampaignId = -1L

    companion object {
        // Mã yêu cầu khi khởi động màn hình này
        const val REQUEST_CODE = 5454

        // Mã để yêu cầu màn hình chọn tệp tin dữ liệu (*.txt)
        const val PICK_DATA_FILE = 4545
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_create)

        // Thiết lập tiêu đề
        supportActionBar?.title = "Tạo chiến dịch mới"

        // Thiết lập trạng thái mặc định khi đóng màn hình này là kết quả CANCELLED
        setResult(Activity.RESULT_CANCELED)

        // Hiển thị nút trở về
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Thiết lập các view (Kích thước,....)
        initViewSetUp()

        // Lấy mã cho chiến dịch hiện tại
        currentCampaignId = CampaignRepository(this).getMaxId() + 1L

        // Tự động lấy ID tiếp theo cho chiến dịch
        campaignTvCreateName.text = "Tên chiến dịch (#$currentCampaignId)"

        // Tự động tạo tên cho tên chiến dịch
        campaignEdtCreateName.setText(CampaignCreateController.generateCampaignName())
        campaignEdtCreateName.isEnabled = true

        // Khởi tạo sụ kiện nhấn để chọn tệp tin
        initPickDataFileEvents()

        // Sự kiện khi nhấn nút xác nhận
        initConfirmEvents()

        // Sự kiện khi nhấn nút hủy nhập dũ liệu
        initCancelImportEvents()

        // Chỉnh trạng thái hiển thị cho đúng
        campaignRlImportLayout.visibility = View.GONE
        campaignSrvPickLayout.visibility = View.VISIBLE
    }

    private fun initCancelImportEvents() {
        loginEdtCancelButton.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Toàn bộ dữ liệu đang được nhập và chiến dịch hiện tại sẽ bị xóa?")
                .setContentText("HỦY NHẬP DỮ LIỆU")
                .setCancelText("Quay lại")
                .setConfirmText("Vẫn hủy")
                .showCancelButton(true)
                .setCancelClickListener { sDialog -> sDialog.cancel() }
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    sDialog.cancel()
                    campaignCreateLoader?.campaignCreateATL?.cancelLoadInBackground()
                }
                .show()
        }
    }

    private fun initConfirmEvents() {
        if (BuildConfig.DEBUG) {
            btnCreate10K.visibility = View.VISIBLE
            btnCreate10K.setOnClickListener {
                GlobalScope.launch(Dispatchers.IO) {
                    val outputFile = File.createTempFile("test_phone_number_10K", ".txt", cacheDir)

                    if (outputFile.exists()) {
                        outputFile.delete() // Xoá file nếu tồn tại
                    }

                    for (i in 1..10000) {
                        val phone = "032997" + String.format("%04d", i) + "\n"
                        outputFile.appendText(phone)
                    }

                    // Trả về uri của file
                    dataUri = FileProvider.getUriForFile(
                        applicationContext,
                        "${packageName}.provider",
                        outputFile
                    )

                    btnCreate10K.post {
                        fadeOutPicker()

                        // Cập nhật đường dẫn vào TextBox
                        campaignCreateEdtDataPath.setText(dataUri?.path ?: "Đã chọn thành công")
                        // Mở khóa nút confirm
                        loginBtnConfirmButton.isEnabled = true
                    }
                }
            }
        }

        loginBtnConfirmButton.setOnClickListener {
            if (dataUri != null)
            // Thực hiện animation thu nhỏ phần chọn file
                fadeOutPicker()
            else
                Toasty.error(this, "Vui lòng chọn lại tệp tin", Toasty.LENGTH_SHORT, true).show()

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
                campaign.name = campaignEdtCreateName.text.toString()
                // Lưu chiến dịch
                campaign.id = CampaignRepository(this@CampaignCreateActivity).add(campaign).toInt()
                // Nếu chiến dịch lưu chưa thành công, báo lỗi và trở lên
                if (campaign.id <= 0) {
                    Toasty.error(
                        this@CampaignCreateActivity,
                        "Không thể tạo chiến dịch",
                        Toasty.LENGTH_SHORT,
                        true
                    ).show()
                    return
                }
                // Bắt đầu trình đếm
                campaignCreateLoader = CampaignCreateLoader(this@CampaignCreateActivity, dataUri)
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })

        campaignRlImportLayout.startAnimation(fadeIn)
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

    //region Các phương thức dành cho phần chọn tệp dữ liệu

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
            startActivityForResult(intent, PICK_DATA_FILE)
        } else {
            Toasty.error(this, "Không tìm thấy phần mềm quản lý tệp tin", Toasty.LENGTH_SHORT, true)
                .show()
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

    //endregion

    // Khi nhấn nút trở về
    override fun onSupportNavigateUp(): Boolean {
        if (campaignRlImportLayout.visibility == View.VISIBLE)
            loginEdtCancelButton.performClick()
        else
            onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Nếu là yêu cầu pick file của app và thành công
        if (requestCode == PICK_DATA_FILE && resultCode == Activity.RESULT_OK) {
            if (data == null)
                return

            // Lưu uri
            dataUri = data.data

            // Cập nhật đường dẫn vào TextBox
            campaignCreateEdtDataPath.setText(dataUri?.path ?: "Đã chọn thành công")
            // Mở khóa nút confirm
            loginBtnConfirmButton.isEnabled = true
            // Trở lên
            return
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

    private fun cancelLoading() {
        if (loading != null && loading!!.isShowing) {
            loading!!.dismissWithAnimation()
            loading!!.cancel()
            loading = null
        }
    }

    fun showSuccess(count: Long) {
        campaignItemName.post {
            cancelLoading()
            loading = SweetAlertDialog(
                this@CampaignCreateActivity,
                SweetAlertDialog.SUCCESS_TYPE
            )
                .setTitleText("Nhập dữ liệu hoàn tất")
                .setContentText("$count/${totalLines}")
                .setConfirmText("Xem danh sách")
                .showCancelButton(false)
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    sDialog.cancel()
                    finishWithSuccess()
                }
            loading?.setCancelable(false)
            loading?.show()
        }
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

    fun hideLoading() {
        campaignItemName.post {
            cancelLoading()
        }
    }

    private fun finishWithSuccess() {
        val dataIntent = Intent()
        dataIntent.putExtra(CampaignModel.ID, campaign.id)
        setResult(Activity.RESULT_OK, dataIntent)
        finish()
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
}