package vn.vistark.autocaller.utils.call_phone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import java.lang.reflect.Method
import java.util.*


class PhoneStateReceiver : BroadcastReceiver() {
    // Lấy tag log là tên class hiện tại
    var TAG = PhoneStateReceiver::class.java.simpleName

    // Khi nhận được trạng thái về cuộc gọi
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent!!.action.equals("android.intent.action.PHONE_STATE")) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            Log.d(TAG, ">>>>>> Trạng thái cuộc gọi = $state")
            if (state == TelephonyManager.EXTRA_STATE_IDLE) {
//                Toast.makeText(context, "Gác máy EXTRA_STATE_IDLE", Toast.LENGTH_SHORT).show()
                // Gác máy, kết thúc cuộc gọi
                Log.d(TAG, "PhoneStateReceiver**Idle")
            } else if (state == TelephonyManager.EXTRA_STATE_RINGING) {
//                Toast.makeText(context, "Đổ chuông EXTRA_STATE_RINGING", Toast.LENGTH_SHORT).show()
//                // Incoming call
//                val incomingNumber =
//                    intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
//                Log.d(TAG, "PhoneStateReceiver**Incoming call $incomingNumber")
//                if (!killCall(context)) { // Using the method defined earlier
//                    Log.d(TAG, "PhoneStateReceiver **Unable to kill incoming call")
//                }
            } else if (state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
//                Toast.makeText(context, "Đã trả lời EXTRA_STATE_OFFHOOK", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "OFFHOOK: Đã trả lời")
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        this.cancel()
                        killCall(context)
                    }
                }, 8000)
            }
        } else if (intent.action.equals("android.intent.action.NEW_OUTGOING_CALL")) {
//            Toast.makeText(context, "Có cuộc gọi đến NEW_OUTGOING_CALL", Toast.LENGTH_SHORT).show()
            // Outgoing call
            val outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
            Log.d(TAG, "PhoneStateReceiver **Outgoing call $outgoingNumber")
            resultData = null // Kills the outgoing call
        } else {
//            Toast.makeText(context, "Không biết ${intent.action}", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "PhoneStateReceiver **unexpected intent.action=" + intent.action)
        }
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
                Class.forName(telephonyInterface?.javaClass?.name ?: "Chịu")
            val methodEndCall: Method = telephonyInterfaceClass.getDeclaredMethod("endCall")

            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface)
        } catch (ex: Exception) { // Many things can go wrong with reflection calls
            Log.d(TAG, "PhoneStateReceiver **$ex")
            return false
        }
        return true
    }
}