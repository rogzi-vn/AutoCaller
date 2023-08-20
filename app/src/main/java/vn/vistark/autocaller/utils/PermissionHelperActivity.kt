package vn.vistark.autocaller.utils

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_permission_helper.*
import vn.vistark.autocaller.MainActivity
import vn.vistark.autocaller.R
import vn.vistark.autocaller.component.BlockNotifier

class PermissionHelperActivity : AppCompatActivity() {
    // Đối tượng điều kiểm màn hình thông báo khóa
    private var blockNotifier: BlockNotifier? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_helper)

        // Thiết lập tiêu đề
        supportActionBar?.title = "Yêu cầu quyền hoạt động"

        // Khởi tạo đối tượng điều khiển màn hình thông báo khóa
        blockNotifier = BlockNotifier(window.decorView.rootView)

        // Load dữ liệu trang quyền
        val permissionPage = ResourceUtils.readText(this, R.raw.permission_policy)

        // Khởi tạo và thiết lập các sự kiện
        initEvents()

        // Nếu không thể lấy được dữ liệu
        if (permissionPage.isEmpty()) {
            Toasty.error(
                this,
                "Không thể hiển thị chi tiết bản mô tả quyền",
                Toasty.LENGTH_SHORT,
                true
            ).show()
            return
        }

        // Load dữ liệu lấy được vào webview để người dùng đọc
        permissionWvContent.loadData(permissionPage, "text/html; charset=utf-8", "utf-8")

    }

    private fun initEvents() {
        // Sự kiện khi nhấn nút xác nhận
        permissionBtnConfirm.setOnClickListener {
            // Khóa nút
            it.isEnabled = false

            // Nếu các quyền đã được cấp, quay lại trang chính
            if (PermissionUtils.permissionRequest(this)) {
                backToMain()
            }
        }
    }

    private fun backToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            // Nếu mã trả về là mã yêu cầu quyền
            PermissionUtils.PERMISSION_REQUEST_ALL -> {
                // Nếu các quyền đã được cấp đủ
                if (PermissionUtils.permissionRequest(this))
                    return backToMain()

                // Không thì hiển thị thông báo và yêu cầu cấp lại
                Toasty.warning(
                    this,
                    "Vui lòng cung cấp các quyền để ứng dụng có thể hoạt động",
                    Toasty.LENGTH_SHORT,
                    true
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}