package vn.vistark.autocaller.views.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.views.campaign.CampaignActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Nếu người dùng đã có mật khẩu và mật khẩu ấy đúng
        if (AppStorage.UserPassword.isNotEmpty() && AppStorage.UserPassword == AppStorage.AppPassword) {
            // Thông báo đăng nhập thành công
            Toasty.success(this, "Tự động đăng nhập thành công", Toasty.LENGTH_SHORT, true).show()
            // Đến trang gọi
            gotoCampaign()
        }

        // Nếu người dùng có mật khẩu mà nó lại khác với mật khẩu ứng dụng
        if (AppStorage.UserPassword.isNotEmpty() && AppStorage.UserPassword != AppStorage.AppPassword)
        // Thông báo cần làm mới mật khẩu
            Toasty.error(
                this,
                "Mật khẩu ứng dụng đã bị đổi, vui lòng nhập lại mật khẩu mới",
                Toasty.LENGTH_SHORT,
                true
            ).show()

        // Khi nhấn vào nút xác nhận
        loginEdtConfirmButton.setOnClickListener {
            // Tiến hành kiểm tra mật khẩu
            checkPassword()
        }
    }

    private fun checkPassword() {
        // Lấy mật khẩu người dùng nhập
        val inputPass = loginEdtPassword.text.toString()

        // Nếu chưa nhập mà bấm
        if (inputPass.isEmpty()) {
            Toasty.error(this, "Bạn chưa nhập mật khẩu", Toasty.LENGTH_SHORT, true).show()
            return
        }

        // Nếu sai mật khẩu
        if (inputPass != AppStorage.AppPassword) {
            // Tăng đếm số lần sai
            AppStorage.LoginFail += 1

            // Thông báo
            Toasty.error(
                this,
                "Sai mật khẩu, bạn còn ${AppStorage.MAX_LOGIN_FAIL - AppStorage.LoginFail} lần thử.",
                Toasty.LENGTH_SHORT,
                true
            ).show()
            return
        }

        // Xóa số lần sai
        AppStorage.LoginFail = 0

        // Lưu mật khẩu ng dùng đã nhập
        AppStorage.UserPassword = inputPass

        // Thông báo thành công
        Toasty.success(
            this,
            "Đăng nhập thành công!",
            Toasty.LENGTH_SHORT,
            true
        ).show()

        // Khi đăng nhập thành công, sang màn hình gọi
        gotoCampaign()
    }

    private fun gotoCampaign() {
        val intent = Intent(this, CampaignActivity::class.java)
        startActivity(intent)
        finish()
    }
}