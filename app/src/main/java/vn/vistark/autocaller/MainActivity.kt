package vn.vistark.autocaller

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import vn.vistark.autocaller.component.BlockNotifier
import vn.vistark.autocaller.models.app_license.AppLicense
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.utils.*
import vn.vistark.autocaller.views.campaign.CampaignActivity
import vn.vistark.autocaller.views.login.LoginActivity
import java.util.*


class MainActivity : AppCompatActivity() {

    //

    // Biến chứa thời gian khởi đầu tải 
    var startLoadMilis = -1L;

    // Đối tượng điều kiểm màn hình thông báo khóa
    private var blockNotifier: BlockNotifier? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo đối tượng điều khiển màn hình thông báo khóa
        blockNotifier = BlockNotifier(window.decorView.rootView)

        // Khởi tạo các hằng số cơ bản và bộ nhớ lưu trữ cục bộ
        SPUtils.init(this)

        // Kiểm tra các quyền
        if (!PermissionUtils.permissionCheckAll(this)) {
            goToPermissionHelper() // Khởi động màn hình hỗ trợ quyền, kết thúc màn hình hiện tại
            return // quai lại
        }

//        // Kiểm tra mạng internet
//        if (!this.isConnected) {
//            blockNotifier?.show("Vui lòng kiểm tra lại kết nối mạng của bạn")
//            return
//        }

        // Nếu đăng nhập sai quá số lần quy định, lock app
        if (AppStorage.LoginFail > AppStorage.MAX_LOGIN_FAIL) {
            blockNotifier?.show("Bạn đã đăng nhập sai quá ${AppStorage.MAX_LOGIN_FAIL} lần! Ứng dụng sẽ bị khóa vình viễn. Hãy liên hệ chủ sở hữu để giải quyết.")
            return
        }

        // cập nhật thời gian bắt đầu tải
        startLoadMilis = System.currentTimeMillis()

        // Đối tượng appLicense
        contiuosTask(AppLicense())
    }

    private fun contiuosTask(appLicense: AppLicense) {
        // Nếu không có quyền tiếp tục chạy
        if (!appLicense.AppState.AllowRun) {
            blockNotifier?.show(appLicense.AppState.Message)
            return
        }

        // Cập nhật mật khẩu cho ứng dụng
        AppStorage.AppPassword = appLicense.AppPassword
//        Toast.makeText(this, "Mật khẩu ứng dụng là: ${appLicense.AppPassword}", Toast.LENGTH_SHORT)
//            .show()

        // Chuyển sang màn hình đăng nhập khi được hơn thời gian quy định
        goToLogin()
    }

    private fun goToLogin() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                this.cancel()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 1000)
    }

    // Phương thức điều hướng sang màn hình yêu cầu quyền
    private fun goToPermissionHelper() {
        val intent = Intent(this, PermissionHelperActivity::class.java)
        startActivity(intent)
        finish()
    }

}