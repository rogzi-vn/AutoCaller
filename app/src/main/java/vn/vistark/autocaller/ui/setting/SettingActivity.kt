package vn.vistark.autocaller.ui.setting

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_setting.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.ui.backlist.BlacklistActivity
import vn.vistark.autocaller.ui.service_provider_list.ServiceProviderListActivity

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
        // --
        settingEdtTimerAutoRunCampaign.setText(AppStorage.TimerAutoRunCampaignInSeconds.toString())
        settingEdtThresholdOfNoSignalCall.setText(AppStorage.ThresholdOfNoSignalCallInMilliseconds.toString())
        settingEdtCountOfNoSignalCallToExit.setText(AppStorage.ThresholdOfExitingAppIfNoSignalCalls.toString())

        scHangUpAsSoonAs.isChecked = AppStorage.IsHangUpAsSoonAsUserAnswer
        scHangUpAsSoonAs.setOnClickListener {
            AppStorage.IsHangUpAsSoonAsUserAnswer = scHangUpAsSoonAs.isChecked
        }
        // --
        scAutoReOpenAppIfShutdownSuddenly.isChecked = AppStorage.IsAutoReopenAppIfShutdownSuddenly
        scAutoReOpenAppIfShutdownSuddenly.setOnClickListener {
            AppStorage.IsAutoReopenAppIfShutdownSuddenly =
                scAutoReOpenAppIfShutdownSuddenly.isChecked
        }


        // Đặt sự kiện lưu lại thiết lập
        settingBtnConfirmSave.setOnClickListener {
            saveSettingChange()
        }

        settingBtnGotoBlacklist.setOnClickListener {
            val intent = Intent(this, BlacklistActivity::class.java)
            startActivity(intent)
        }

        settingServicesProvider.setOnClickListener {
            val intent = Intent(this, ServiceProviderListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveSettingChange() {

        // Lưu thiết lập về thời gian nghỉ giãn cách sau các cuộc gọi
        if (!saveDelayTime())
            return

        // Lưu thiết lập về thời gian thực hiện cho mỗi cuộc gọi
        if (!saveDelayTimeInCall())
            return

        if (!saveTimerAutoRunCampaignInSeconds() || !saveThresholdOfNoSignalCallInMilliseconds() || !saveThresholdOfExitingAppIfNoSignalCalls()) {
            return
        }

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

    private fun saveTimerAutoRunCampaignInSeconds(): Boolean {
        // Lấy giá trị mà người dùng đã nhập
        var value = settingEdtTimerAutoRunCampaign.text.toString().toIntOrNull()

        // Nếu không thể phân giải thành số
        if (value == null) {
            Toasty.error(this, "Số giây đợi không đúng", Toasty.LENGTH_SHORT, true).show()
            return false
        }

        // Nếu giá trị nhỏ nhơn 0
        if (value < 0) {
            value = 0
            settingEdtTimerAutoRunCampaign.setText(value.toString())
            return false
        }

        // Nếu nhập thành công
        AppStorage.TimerAutoRunCampaignInSeconds = value

        // Trả về true
        return true
    }

    private fun saveThresholdOfNoSignalCallInMilliseconds(): Boolean {
        // Lấy giá trị mà người dùng đã nhập
        var value = settingEdtThresholdOfNoSignalCall.text.toString().toLongOrNull()

        // Nếu không thể phân giải thành số
        if (value == null) {
            Toasty.error(this, "Số mili giây đợi không đúng", Toasty.LENGTH_SHORT, true).show()
            return false
        }

        // Nếu giá trị nhỏ nhơn 500
        if (value < 500) {
            value = 500
            settingEdtThresholdOfNoSignalCall.setText(value.toString())
            return false
        }

        // Nếu nhập thành công
        AppStorage.ThresholdOfNoSignalCallInMilliseconds = value

        // Trả về true
        return true
    }

    private fun saveThresholdOfExitingAppIfNoSignalCalls(): Boolean {
        // Lấy giá trị mà người dùng đã nhập
        var value = settingEdtCountOfNoSignalCallToExit.text.toString().toIntOrNull()

        // Nếu không thể phân giải thành số
        if (value == null) {
            Toasty.error(this, "Số lượng không đúng", Toasty.LENGTH_SHORT, true).show()
            return false
        }

        // Nếu giá trị nhỏ nhơn 0
        if (value < 0) {
            value = 0
            settingEdtCountOfNoSignalCallToExit.setText(value.toString())
            return false
        }

        // Nếu nhập thành công
        AppStorage.ThresholdOfExitingAppIfNoSignalCalls = value

        // Trả về true
        return true
    }

    // Trở về khi nhấn nút back
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}