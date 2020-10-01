package vn.vistark.autocaller.views.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_setting.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.storages.AppStorage

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // Thiết lập tiêu đề
        supportActionBar?.title = "Thiết lập ứng dụng"

        // Hiển thị nút trở về
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Load lại dữ liệu trước đó
        settingEdtTimerDelay.setText(AppStorage.DelayTimeInSeconds.toString())
        settingEdtTimerCallIn.setText(AppStorage.DelayTimeCallInSeconds.toString())

        // Đặt sự kiện lưu lại thiết lập
        settingBtnConfirmSave.setOnClickListener {
            saveSettingChange()
        }
    }

    private fun saveSettingChange() {

        // Lưu thiết lập về thời gian nghỉ giãn cách sau các cuộc gọi
        if (!saveDelayTime())
            return

        // Lưu thiết lập về thời gian thực hiện cho mỗi cuộc gọi
        if (!saveDelayTimeInCall())
            return

        Toasty.success(this, "Cập nhật thiết lập thành công", Toasty.LENGTH_SHORT, true).show()
    }

    private fun saveDelayTime(): Boolean {
        // Lấy giá trị mà người dùng đã nhập
        val inpDelayTime = settingEdtTimerDelay.text.toString().toIntOrNull()

        // Nếu không thể phân giải thành số
        if (inpDelayTime == null) {
            Toasty.error(this, "Số giây đợi không đúng", Toasty.LENGTH_SHORT, true).show()
            return false
        }

        // Nếu giá trị nhỏ nhơn 2
        if (inpDelayTime < 2) {
            Toasty.error(this, "Vui lòng nhập số giây lớn hơn 2", Toasty.LENGTH_SHORT, true).show()
            return false
        }

        // Nếu nhập thành công
        AppStorage.DelayTimeInSeconds = inpDelayTime

        // Trả về true
        return true
    }

    private fun saveDelayTimeInCall(): Boolean {
        // Lấy giá trị mà người dùng đã nhập
        val inpDelayTimeInCall = settingEdtTimerCallIn.text.toString().toIntOrNull()

        // Nếu không thể phân giải thành số
        if (inpDelayTimeInCall == null) {
            Toasty.error(this, "Thời gian cho cuộc gọi không đúng", Toasty.LENGTH_SHORT, true)
                .show()
            return false
        }

        // Nếu giá trị nhỏ nhơn 2
        if (inpDelayTimeInCall < 3) {
            Toasty.error(
                this,
                "Thời gian tối thiểu để gọi thành công là 3 giây",
                Toasty.LENGTH_SHORT,
                true
            ).show()
            return false
        }

        // Nếu nhập thành công
        AppStorage.DelayTimeCallInSeconds = inpDelayTimeInCall

        // Trả về true
        return true
    }

    // Trở về khi nhấn nút back
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}