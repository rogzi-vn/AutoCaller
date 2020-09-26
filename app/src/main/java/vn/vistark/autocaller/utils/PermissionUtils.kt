package vn.vistark.autocaller.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtils {
    companion object {
        // Mã chung cho tất cả các quyền
        const val PERMISSION_REQUEST_ALL: Int = 3456

        private fun buildRequestPermissions(): ArrayList<String> {
            val permissions = ArrayList<String>()
            permissions.add(Manifest.permission.CALL_PHONE)
            permissions.add(Manifest.permission.READ_PHONE_STATE)
            permissions.add(Manifest.permission.INTERNET)
            permissions.add(Manifest.permission.ACCESS_WIFI_STATE)
            permissions.add(Manifest.permission.ACCESS_NETWORK_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.VIBRATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                permissions.add(Manifest.permission.FOREGROUND_SERVICE)
            }
            return permissions
        }

        fun permissionCheckAll(context: AppCompatActivity): Boolean {
            return !buildRequestPermissions().any { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            }
        }

        fun permissionRequest(context: AppCompatActivity): Boolean {
            // Lưu danh sách các quyền sẽ cần
            val permissions = buildRequestPermissions()

            // Nếu tất cả quyền đã đủ, không làm gì thêm
            if (permissionCheckAll(context))
                return true

            // Không thì hiện bảng yêu cầu
            ActivityCompat.requestPermissions(
                context,
                permissions.toTypedArray(),
                PERMISSION_REQUEST_ALL
            )
            return false
        }
    }
}