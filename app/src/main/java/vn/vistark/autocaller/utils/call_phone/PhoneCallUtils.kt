package vn.vistark.autocaller.utils.call_phone

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import es.dmoral.toasty.Toasty
import vn.vistark.autocaller.controller.campaign_detail.CampaignCall.Companion.runHandler
import java.util.*


class PhoneCallUtils {
    companion object {
        private val TAG = PhoneCallUtils::class.java.simpleName

        public var timerEnsureSimReadyToCallAgainIfFail: Timer? = null;

        fun startCall(context: Context, phoneNumber: String) {
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

        fun removeTimer() {
            Log.w(TAG, "removeTimer: Đã nhận được tín hiệu cuộc gọi => Bỏ ngay timer đảm bảo")
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
    }
}