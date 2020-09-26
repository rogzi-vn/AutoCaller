package vn.vistark.autocaller.utils.call_phone

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity

class PhoneCallUtils {
    companion object {
        fun startCall(context: AppCompatActivity, phoneNumber: String) {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:${phoneNumber}")
            context.startActivity(intent)
        }
    }
}