package vn.vistark.autocaller.utils.call_phone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.telephony.TelephonyManager
import es.dmoral.toasty.Toasty
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.services.BackgroundService.Companion.isStopTemporarily
import java.lang.reflect.Method


// https://stackoverflow.com/questions/9684866/how-to-detect-when-phone-is-answered-or-rejected
class PhoneStateReceiver : BroadcastReceiver() {
    companion object {
        const val NAME = "PhoneStateReceiver"
        const val INCOMMING_CALL = "INCOMMING_CALL"
        const val STOP_TEMPORARILY_DONE = "STOP_TEMPORARILY_DONE"

        // Biến lưu trữ trạng thái trước đó
        var previousState = "EXTRA_STATE_IDLE"

        // Biến kiểm tra xem đây có phải lần đầu không
        var isFirstTime = true
    }

    // Khi nhận được trạng thái về cuộc gọi
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                TelephonyManager.EXTRA_STATE_IDLE -> {

                    // Nếu trạng thái trước đó là mình gọi, và bây giờ đã kết thúc
                    if (previousState == "EXTRA_STATE_OFFHOOK") {
                        // Gửi thông báo
                        context.sendBroadcast(Intent(NAME))
                    }

                    // Nếu trạng thái trước đó là trạng thái dừng tạm thời do có cuộc gọi đến
                    if (previousState == "EXTRA_STATE_RINGING" || isStopTemporarily) {
                        // Gửi thông báo để tái khởi động lại chiến dịch
                        context.sendBroadcast(Intent(STOP_TEMPORARILY_DONE))
                    }

                    // Làm mới trạng thái hiện tại
                    previousState = "EXTRA_STATE_IDLE"
                }
                TelephonyManager.EXTRA_STATE_RINGING -> {

                    // Làm mới trạng thái hiện tại
                    previousState = "EXTRA_STATE_RINGING"

                    // Gửi thông báo có cuộc gọi đến
                    context.sendBroadcast(Intent(INCOMMING_CALL))
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {

                    // Nếu trước đó đang là trạng thái nghỉ
                    if (previousState == "EXTRA_STATE_IDLE")
                        KillCallTimer(context)

                    // Làm mới trạng thái hiện tại
                    previousState = "EXTRA_STATE_OFFHOOK"
                }
                else -> {

                }
            }
        } else {
            previousState = TelephonyManager.EXTRA_STATE_IDLE
        }

        println(">>>>> [$previousState]")

    }

    private fun KillCallTimer(context: Context) {
        // Thời gian chờ
        var timeDelay = AppStorage.DelayTimeCallInSeconds * 1000L + 380L

        // Nếu không phải lần đầu, +1s
        if (!isFirstTime) {
            timeDelay += 1450
            isFirstTime = false
        }

        println("READY KILL =>")
        // Đếm ngược
        object : CountDownTimer(timeDelay, 750) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                // Kết thúc cuộc gọi ngay lập tức
                killCall(context)
            }
        }.start()
    }

    private fun killCall(context: Context): Boolean {
        try {
            // Get the boring old TelephonyManager
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            // Get the getITelephony() method
            val classTelephony =
                Class.forName(telephonyManager.javaClass.name)
            val methodGetITelephony: Method = classTelephony.getDeclaredMethod("getITelephony")

            // Ignore that the method is supposed to be private
            methodGetITelephony.isAccessible = true

            // Invoke getITelephony() to get the ITelephony interface
            val telephonyInterface: Any? = methodGetITelephony.invoke(telephonyManager)

            // Get the endCall method from ITelephony
            val telephonyInterfaceClass =
                Class.forName(telephonyInterface?.javaClass?.name ?: "ERROR?")
            val methodEndCall: Method = telephonyInterfaceClass.getDeclaredMethod("endCall")

            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface)
        } catch (ex: Exception) { // Many things can go wrong with reflection calls
            Toasty.error(
                context,
                "Ứng dụng không thể can thiệp vào hệ thống để ngưng cuộc gọi",
                Toasty.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }
}