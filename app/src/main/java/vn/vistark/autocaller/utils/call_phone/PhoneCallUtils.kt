package vn.vistark.autocaller.utils.call_phone

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.provider.Settings
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import es.dmoral.toasty.Toasty
import vn.vistark.autocaller.controller.campaign_detail.CampaignCall.Companion.runHandler
import java.lang.reflect.Method
import java.util.*


class PhoneCallUtils {
    companion object {
        private val TAG = PhoneCallUtils::class.java.simpleName

        private var timerEnsureSimReadyToCallAgainIfFail: Timer? = null

        // Thời điểm mà cuộc gọi được thực hiện
        private var phoneCallStartAt: Long = 0L

        // Thời điểm mà cuộc gọi có tín hiệu
        private var phoneCallHaveSignalAt: Long = 0L

        // Thời điểm mà tiến hành chấm dứt cuộc gọi
        private var phoneCallEndAt: Long = 0L

        private fun resetAll() {
            phoneCallStartAt = 0L
            phoneCallHaveSignalAt = 0L
            phoneCallEndAt = 0L
        }

        fun getTimeHaveSignalInMilliseconds() = phoneCallHaveSignalAt - phoneCallStartAt

        fun onHaveSignal() {
            phoneCallHaveSignalAt = System.currentTimeMillis()
        }

        fun startCall(context: Context, phoneNumber: String) {
            resetAll()
            var number = phoneNumber
            // Chuẩn hóa số điện thoại
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                number = PhoneNumberUtils.normalizeNumber(number)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                number = PhoneNumberUtils.formatNumber(
                    number,
                    Locale.getDefault().country
                )
            }


            if (!CheckSimConnection(context) || isAirplaneModeOn(context)) {

                context.runHandler {
                    Toasty.error(
                        context,
                        "SIM lỗi, thử lại sau 10 giây",
                        Toasty.LENGTH_SHORT
                    ).show()

                    object : CountDownTimer(10000, 750) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            // Thử lại sau 10 giay
                            startCall(context, phoneNumber)
                        }
                    }.start()
                }

                return
            }

            // Tiến hành gọi

            context.runHandler {
                Toasty.info(
                    context,
                    "ĐANG GỌI SỐ $number",
                    Toasty.LENGTH_SHORT
                ).show()
            }

            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:${number}")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    context.applicationContext.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            phoneCallStartAt = System.currentTimeMillis()


            // (***)
            context.runHandler {
                timerEnsureSimReadyToCallAgainIfFail = Timer()
                timerEnsureSimReadyToCallAgainIfFail!!.schedule(object : TimerTask() {
                    override fun run() {
                        removeTimer()
                        // Thử lại sau 10 giay
                        startCall(context, phoneNumber)
                    }
                }, 10000)
            }
        }

        // Trong vòng 10s nếu nhận được tín hiệu broad cast cuộc gọi thì tiến hành huỷ/
        // Nếu không phần (***) sẽ đợi hết 10s để tiến hành tự động thực hiện lại cuộc gọi
        // và lặp lại phần timer cho đến khi được broadcast đóng
        fun removeTimer() {
            if (timerEnsureSimReadyToCallAgainIfFail != null) {
                timerEnsureSimReadyToCallAgainIfFail!!.cancel()
                timerEnsureSimReadyToCallAgainIfFail = null
            }
        }

//        private fun isAirplaneModeOn(context: Context): Boolean {
//            return Settings.System.getInt(
//                context.contentResolver,
//                Settings.Global.AIRPLANE_MODE_ON, 0
//            ) != 0
//        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        fun isAirplaneModeOn(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.AIRPLANE_MODE_ON, 0
                ) != 0
            } else {
                Settings.Global.getInt(
                    context.contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON, 0
                ) != 0
            }
        }

        fun CheckSimConnection(context: Context): Boolean {
            // Context.TELEPHONY_SERVICE
            val telMgr = getSystemService(context, TelephonyManager::class.java) ?: return true

            val simState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telMgr.getSimState(0)
            } else {
                telMgr.simState
            }
            Log.w(TAG, "CheckSimConnection: ${telMgr.simState}")
            when (simState) {
                TelephonyManager.SIM_STATE_ABSENT -> {
                    Log.w(TAG, "CheckSimConnection: SIM_STATE_ABSENT")
                    return false
                }

                TelephonyManager.SIM_STATE_NETWORK_LOCKED -> {
                    Log.w(TAG, "CheckSimConnection: SIM_STATE_NETWORK_LOCKED")
                    return false
                }

                TelephonyManager.SIM_STATE_PIN_REQUIRED -> {
                    Log.w(TAG, "CheckSimConnection: SIM_STATE_PIN_REQUIRED")
                    return false
                }

                TelephonyManager.SIM_STATE_PUK_REQUIRED -> {
                    Log.w(TAG, "CheckSimConnection: SIM_STATE_PUK_REQUIRED")
                    return false
                }

                TelephonyManager.SIM_STATE_READY -> {
                    Log.w(TAG, "CheckSimConnection: SIM_STATE_READY")
                    return true
                }

                TelephonyManager.SIM_STATE_UNKNOWN -> {
                    Log.w(TAG, "CheckSimConnection: SIM_STATE_UNKNOWN")
                    return false
                }

                TelephonyManager.SIM_STATE_CARD_IO_ERROR -> {
                    Log.w(TAG, "CheckSimConnection: SIM_STATE_CARD_IO_ERROR")
                    return false
                }

                TelephonyManager.SIM_STATE_CARD_RESTRICTED -> {
                    Log.w(TAG, "CheckSimConnection: SIM_STATE_CARD_RESTRICTED")
                    return false
                }

                TelephonyManager.SIM_STATE_NOT_READY -> {
                    Log.w(TAG, "CheckSimConnection: SIM_STATE_NOT_READY")
                    return false
                }

                TelephonyManager.SIM_STATE_PERM_DISABLED -> {
                    Log.w(TAG, "CheckSimConnection: SIM_STATE_PERM_DISABLED")
                    return false
                }
            }
            return false
        }

        fun KillCall(context: Context): Boolean {
            phoneCallEndAt = System.currentTimeMillis()
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
                ex.printStackTrace()
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
}