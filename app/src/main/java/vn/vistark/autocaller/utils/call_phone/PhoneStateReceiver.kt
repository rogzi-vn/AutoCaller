package vn.vistark.autocaller.utils.call_phone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.telephony.TelephonyManager
import android.util.Log
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.services.BackgroundServiceCompanion.Companion.isStopTemporarily


// https://stackoverflow.com/questions/9684866/how-to-detect-when-phone-is-answered-or-rejected
class PhoneStateReceiver : BroadcastReceiver() {

    private val TAG = PhoneStateReceiver::javaClass.name

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
        PhoneCallUtils.removeTimer()
        if (intent.action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                TelephonyManager.EXTRA_STATE_IDLE -> {

                    // Nếu trạng thái trước đó là mình gọi, và bây giờ đã kết thúc
                    if (previousState == "EXTRA_STATE_OFFHOOK") {
                        PhoneCallUtils.onOnCallEnd()
                        // Gửi thông báo
                        context.sendBroadcast(Intent(NAME))
                    }

                    // Nếu trạng thái trước đó là trạng thái dừng tạm thời do có cuộc gọi đến
                    if (previousState == "EXTRA_STATE_RINGING" || isStopTemporarily) {
                        Log.w(
                            TAG,
                            "onReceive: Đã xong phần tạm dừng của cuộc gọi đến, tiến hành TIẾP TỤC chiến dịch"
                        )
                        // Gửi thông báo để tái khởi động lại chiến dịch
                        context.sendBroadcast(Intent(STOP_TEMPORARILY_DONE))
                    }

                    // Làm mới trạng thái hiện tại
                    previousState = "EXTRA_STATE_IDLE"

                    Log.w("PHONE_STATE", "IDLE")
                }

                TelephonyManager.EXTRA_STATE_RINGING -> {

                    // Làm mới trạng thái hiện tại
                    previousState = "EXTRA_STATE_RINGING"

                    // Gửi thông báo có cuộc gọi đến
                    context.sendBroadcast(Intent(INCOMMING_CALL))
                    Log.w("PHONE_STATE", "RING")
                }

                TelephonyManager.EXTRA_STATE_OFFHOOK -> {

                    // Nếu trước đó đang là trạng thái nghỉ
                    if (previousState == "EXTRA_STATE_IDLE" || previousState == "EXTRA_STATE_RINGING") {
                        if (previousState == "EXTRA_STATE_IDLE") {
                            PhoneCallUtils.onHaveSignal()
                        }
                        KillCallTimer(context)
                    }

                    // Làm mới trạng thái hiện tại
                    previousState = "EXTRA_STATE_OFFHOOK"
                    Log.w("PHONE_STATE", "OFF_HOOK")
                }

                else -> {
                    Log.w("PHONE_STATE", "OTHER")
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
                PhoneCallUtils.KillCall(context)
            }
        }.start()
//        if (AppStorage.IsHangUpAsSoonAsUserAnswer) {
//            println("READY KILL =>")
//            // Đếm ngược
//            object : CountDownTimer(1500, 750) {
//                override fun onTick(millisUntilFinished: Long) {}
//                override fun onFinish() {
//                    // Kết thúc cuộc gọi ngay lập tức
//                    killCall(context)
//                }
//            }.start()
//        } else {
//
//        }
    }


}