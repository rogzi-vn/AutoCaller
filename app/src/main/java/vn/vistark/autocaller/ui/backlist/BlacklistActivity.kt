package vn.vistark.autocaller.ui.backlist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_blacklist.*
import kotlinx.android.synthetic.main.activity_blacklist.campaignItemProgressBar
import kotlinx.android.synthetic.main.activity_blacklist.campaignItemProgressCount
import kotlinx.android.synthetic.main.activity_blacklist.campaignItemProgressPercent
import kotlinx.android.synthetic.main.activity_campaign_create.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.controller.blacklink_add.general_file.BlackListAddLoader
import vn.vistark.autocaller.controller.blacklink_add.google_link.BlackListAddViaGGLinkLoader
import vn.vistark.autocaller.models.BlackListModel
import vn.vistark.autocaller.models.repositories.BlackListRepository
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.services.BackgroundServiceCompanion.Companion.IsBackgroundServiceRunning
import vn.vistark.autocaller.ui.campaign_create.CampaignCreateActivity

class BlacklistActivity : AppCompatActivity() {

    val blackLists = ArrayList<BlackListModel>()
    lateinit var adapter: BlackListAdapter

    // Biến nhận loader
    var blacklistAddLoader: BlackListAddLoader? = null
    var blackListAddViaGGLinkLoader: BlackListAddViaGGLinkLoader? = null

    // Biến chứa địa chỉ tệp mà người dùng đã chọn
    private var dataUri: Uri? = null

    // Biến chứa số lượng bản ghi
    var totalLines: Long = 0L

    // Đối tượng chiến dịch hiện tại
    val campaign: BlackListModel = BlackListModel()

    // Khởi tạo hộp thoại loading
    var loading: SweetAlertDialog? = null

    fun clearAllImport() {
        blacklistBtnConfirmButton.isEnabled = false
        dataUri = null
        abTvAddBlackListPath.text.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blacklist)

        // Thiết lập tiêu đề
        supportActionBar?.title = "Danh sách đen"

        // Hiển thị nút trở về
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        initSwitchEvents()

        initViewSetUp()

        // Khởi tạo sụ kiện nhấn để chọn tệp tin
        initPickDataFileEvents()

        // Sự kiện khi nhấn nút xác nhận
        initConfirmEvents()

        // Sự kiện khi nhấn nút hủy nhập dũ liệu
        initCancelImportEvents()

        // Chỉnh trạng thái hiển thị cho đúng
        blacklistRlImportLayout.visibility = View.GONE
        blacklistSrvPickLayout.visibility = View.VISIBLE

        initBlackListRv()
    }

    private fun initBlackListRv() {
        abRvBlackLists.setHasFixedSize(true)
        abRvBlackLists.layoutManager = LinearLayoutManager(this)

        adapter = BlackListAdapter(blackLists)
        abRvBlackLists.adapter = adapter
        blackLists.addAll(BlackListRepository(this).getAll())

        updateBlackListView()
    }

    private fun updateBlackListView() {
        abTvBlacklistCount.text = "Danh sách đen (${blackLists.size})"
        adapter.notifyDataSetChanged()
    }

    private fun finishWithSuccess() {
        refreshMe()
    }

    fun showSuccess(count: Long) {
        blacklistItemName.post {
            cancelLoading()
            loading = SweetAlertDialog(
                this@BlacklistActivity,
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

    // Ẩn phần tiếp độ nhập và hiển thị phần chọn file
    private fun showPickerAgain() {
        blacklistSrvPickLayout.visibility = View.VISIBLE
        blacklistRlImportLayout.visibility = View.GONE
    }


    fun hideLoading() {
        blacklistItemName.post {
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

    @SuppressLint("SetTextI18n")
    fun updateProgressState(blackListModel: BlackListModel, count: Long) {
        blacklistItemName.post {
            blacklistItemName.text = "Đang nhập số: ${blackListModel.phone}"
            campaignItemProgressCount.text = "($count/$totalLines)"
            val progress = (count.toDouble() / totalLines.toDouble()) * 100
            campaignItemProgressBar.progress = progress.toInt()
            campaignItemProgressPercent.text = campaignItemProgressBar.progress.toString() + "%"
        }
    }

    fun showLoading() {
        blacklistItemName.post {
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

    private fun cancelLoading() {
        if (loading != null && loading!!.isShowing) {
            loading!!.dismissWithAnimation()
            loading!!.cancel()
            loading = null
        }
    }

    // Sự kiện khi nhấn vào các view có chức năng chọn file dữ liệu
    private fun initPickDataFileEvents() {
        abIvBlackListDataPick.setOnClickListener {
            pickDataFileAction()
        }
    }

    private fun initConfirmEvents() {
        blacklistBtnConfirmButton.setOnClickListener {
            if (abSwSwitchMethod.isChecked || dataUri != null) {
                if (abSwSwitchMethod.isChecked)
                    AppStorage.GoogleSheetBlackLink = abBlacklistLinkImport.text.toString()
                fadeOutPicker()
            } else
                Toasty.error(this, "Vui lòng chọn lại tệp tin", Toasty.LENGTH_SHORT, true).show()

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
                blacklistSrvPickLayout.visibility = View.GONE
                // Chạy animation phần progress
                fadeInProgress()
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })

        blacklistSrvPickLayout.startAnimation(fadeOut)
    }

    // Hiển thị phần tiến độ nhập dữ liệu
    private fun fadeInProgress() {
        blacklistRlImportLayout.visibility = View.VISIBLE
        val fadeIn: Animation = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator() //add this
        fadeIn.duration = 300
        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                // Bắt đầu trình đếm
                if (!abSwSwitchMethod.isChecked)
                    blacklistAddLoader = BlackListAddLoader(this@BlacklistActivity, dataUri)
                else
                    blackListAddViaGGLinkLoader = BlackListAddViaGGLinkLoader(
                        this@BlacklistActivity,
                        abBlacklistLinkImport.text.toString().trim()
                    )
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })

        blacklistRlImportLayout.startAnimation(fadeIn)
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

    private fun initCancelImportEvents() {
        blacklistBtnCancel.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Hủy có thể dẫn đến sai sót trong quá trình quản lý dữ liệu sau này?")
                .setContentText("HỦY NHẬP DỮ LIỆU")
                .setCancelText("Quay lại")
                .setConfirmText("Vẫn hủy")
                .showCancelButton(true)
                .setCancelClickListener { sDialog -> sDialog.cancel() }
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    sDialog.cancel()
                    if (!abSwSwitchMethod.isChecked)
                        blacklistAddLoader?.blackListAddATL?.cancelLoadInBackground()
                    else
                        blackListAddViaGGLinkLoader?.blackListAddViaGGLinkATL?.cancelLoadInBackground()
                }
                .show()
        }
    }

    // Thiết lập các view
    private fun initViewSetUp() {
        abTvAddBlackListPath.post {
            val textBoxHeight = abTvAddBlackListPath.height
            val tempLp = abIvBlackListDataPick.layoutParams
            tempLp.width = textBoxHeight
            tempLp.height = textBoxHeight
            abIvBlackListDataPick.layoutParams = tempLp
        }
    }

    private fun initSwitchEvents() {
        abSwSwitchMethod.setOnClickListener {
            clearAllImport()
            if (abSwSwitchMethod.isChecked) {
                checked()
            } else {
                unchecked()
            }
        }
    }

    private fun unchecked() {
        abTvImportTypeLabel.text = "Nhập Blacklist qua: Tệp *.txt"
        abRlBlacklistFileImport.visibility = View.VISIBLE
        abBlacklistLinkImport.visibility = View.GONE
    }

    fun checked() {
        abTvImportTypeLabel.text = "Nhập Blacklist qua: GoogleLink"
        abRlBlacklistFileImport.visibility = View.GONE
        abBlacklistLinkImport.visibility = View.VISIBLE
        abBlacklistLinkImport.setText(AppStorage.GoogleSheetBlackLink)
        blacklistBtnConfirmButton.isEnabled = abBlacklistLinkImport.text.toString()
            .startsWith("https://docs.google.com/spreadsheets")
        abBlacklistLinkImport.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                blacklistBtnConfirmButton.isEnabled = abBlacklistLinkImport.text.toString()
                    .startsWith("https://docs.google.com/spreadsheets")
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    // Trở về khi nhấn nút back
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
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
            abTvAddBlackListPath.setText(dataUri?.path ?: "Đã chọn thành công")
            // Mở khóa nút confirm
            blacklistBtnConfirmButton.isEnabled = true
            // Trở lên
            return
        }

        // Nếu là yêu cầu pick file của app nhưng lại không thành công
        if (requestCode == CampaignCreateActivity.PICK_DATA_FILE && resultCode != Activity.RESULT_OK)
            return
    }

    fun refreshMe() {
        val intent = Intent(this, BlacklistActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Khởi tạo và chèn menu vào thanh ứng dụng ở trên, bên phải
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_right_black_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.removeAllBlacklist -> {
                return confirmClearAllBlackList()
            }
            else -> {
//                Toasty.error(this, "Không tìm thấy tùy chọn này", Toasty.LENGTH_SHORT, true).show()
            }
        }
        return false
    }

    private fun confirmClearAllBlackList(): Boolean {
        if (IsBackgroundServiceRunning()) {
            Toasty.error(
                this,
                "Vui lòng TẠM NGƯNG chiến dịch trước khi thao tác",
                Toasty.LENGTH_SHORT
            ).show()
            return true
        }
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Khi thực hiện thao tác này, toàn bộ các số điện thoại trong danh sách đen sẽ bị xóa hết. Bạn có muốn thực hiện thao tác này?")
            .setContentText("XÓA HẾT")
            .showCancelButton(true)
            .setCancelButton("Hủy thao tác") {
                it.dismissWithAnimation()
                it.cancel()
            }
            .setConfirmButton("Xác nhận") {
                it.dismissWithAnimation()
                it.cancel()
                if (BlackListRepository(this).removeAll() >= 0) {
                    Toasty.success(this, "Xóa hoàn tất", Toasty.LENGTH_SHORT, true).show()
                    refreshMe()
                } else {
                    Toasty.error(this, "Xóa không thành công", Toasty.LENGTH_SHORT, true).show()
                }
            }.show()
        return true
    }

}