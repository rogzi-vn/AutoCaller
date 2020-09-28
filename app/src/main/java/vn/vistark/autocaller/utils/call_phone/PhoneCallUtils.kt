package vn.vistark.autocaller.utils.call_phone

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.PhoneNumberUtils
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class PhoneCallUtils {
    companion object {
        fun startCall(context: AppCompatActivity, phoneNumber: String) {
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

            // Tiến hành gọi
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:${number}")
            context.startActivity(intent)
        }
    }
}