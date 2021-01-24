package me.vistark.fastdroid.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class FastdroidBroadcastReceiver() :
    BroadcastReceiver() {
    var onSignalEvent: ((String, String) -> Unit)? = null

    companion object {
        val FASTDROID_BROADCAST_ACTION = "Fastdroid.CommonBroadcast"
        val FASTDROID_BROADCAST_KEY = "FASTDROID_BROADCAST_KEY"
        val FASTDROID_BROADCAST_VALUE = "FASTDROID_BROADCAST_VALUE"

        fun Context.sendSignal(key: String, value: String) {
            val intent = Intent()
            intent.putExtra(FASTDROID_BROADCAST_KEY, key)
            intent.putExtra(FASTDROID_BROADCAST_VALUE, value)
            intent.action = FASTDROID_BROADCAST_ACTION
            println(">>>>>>>>>>>>> CONTENT <<<<<<<<<<<<<<<<<<")
            sendBroadcast(intent)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.action == FASTDROID_BROADCAST_ACTION) {
            val key = intent.getStringExtra(FASTDROID_BROADCAST_KEY) ?: ""
            val value = intent.getStringExtra(FASTDROID_BROADCAST_VALUE) ?: ""
            onSignalEvent?.invoke(key, value)
        }
    }
}