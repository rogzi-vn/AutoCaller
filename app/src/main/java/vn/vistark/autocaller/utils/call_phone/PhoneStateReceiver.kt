package vn.vistark.autocaller.utils.call_phone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.telephony.TelephonyManager
import java.lang.reflect.Method
import java.util.*


// https://stackoverflow.com/questions/9684866/how-to-detect-when-phone-is-answered-or-rejected
class PhoneStateReceiver : BroadcastReceiver() {
    // Kiểm tra xem đây có phải lầ đầu chạy lại không
    var isFirstRun = true

    // Biến lưu trữ trạng thái trước đó
    var previousState = TelephonyManager.EXTRA_STATE_IDLE

    // Biến lưu giữ Timer
    var timer: Timer? = null

    // Lấy tag log là tên class hiện tại
    var TAG = PhoneStateReceiver::class.java.simpleName

    // Khi nhận được trạng thái về cuộc gọi
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Làm mới trạng thái hiện tại
                    previousState = TelephonyManager.EXTRA_STATE_IDLE
                }
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Làm mới trạng thái hiện tại
                    previousState = TelephonyManager.EXTRA_STATE_RINGING
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Nếu trước đó đang là trạng thái nghỉ
                    if (previousState == TelephonyManager.EXTRA_STATE_IDLE)
                        KillCallTimer(context)

                    // Làm mới trạng thái hiện tại
                    previousState = TelephonyManager.EXTRA_STATE_OFFHOOK
                }
            }
        } else {
            previousState = TelephonyManager.EXTRA_STATE_IDLE
        }

        isFirstRun = false
    }

    private fun KillCallTimer(context: Context) {
        // Thời gian chờ
        var timeDelay = 6380L

        // Nếu không phải lần đầu, +1s
        timeDelay += 1450

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
            return false
        }
        return true
    }
}