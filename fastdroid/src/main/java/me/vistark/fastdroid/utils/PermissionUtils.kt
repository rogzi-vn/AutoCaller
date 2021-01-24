package me.vistark.fastdroid.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.vistark.fastdroid.R
import me.vistark.fastdroid.core.models.RequirePermission
import me.vistark.fastdroid.ui.activities.FastdroidActivity
import me.vistark.fastdroid.ui.dialog.permission_request.PermissionRequestAdapter
import me.vistark.fastdroid.utils.AnimationUtils.scaleDownCenter
import me.vistark.fastdroid.utils.AnimationUtils.scaleUpCenter
import me.vistark.fastdroid.utils.MultipleLanguage.L
import me.vistark.fastdroid.utils.ViewExtension.onTap


object PermissionUtils {
    const val QUICK_PERMISISON_REQUEST_CODE = 22313

    var _onCompleted: ((Boolean) -> Unit)? = null

    var _permissions: ArrayList<RequirePermission> = ArrayList()

    // Trả về false nếu có bất cứ quyền nào chưa được cấp
    fun FastdroidActivity.checkAllPermissions(permissions: ArrayList<RequirePermission> = _permissions): Boolean {
        if (_permissions.isEmpty())
            _permissions = permissions
        return !permissions.any { p ->
            ContextCompat.checkSelfPermission(
                this,
                p.permission
            ) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun FastdroidActivity.requestAllPermissions(
        permissions: ArrayList<RequirePermission> = _permissions,
        title: String = L("RequestPermission"),
        onCompleted: ((Boolean) -> Unit),
        onDenied: (() -> Unit)?
    ) {
        if (_permissions.isEmpty())
            _permissions = permissions

        // Nếu tất cả quyền đã đủ, không làm gì thêm
        if (checkAllPermissions(permissions)) {
            onCompleted.invoke(true)
            _onCompleted = null
            return
        }

        val v = LayoutInflater.from(this)
            .inflate(R.layout.dialog_permission_request, null)

        val dprTvTitle: TextView = v.findViewById(R.id.dprTvTitle)
        val dprRvListPermissions: RecyclerView = v.findViewById(R.id.dprRvListPermissions)
        val dprBtnConfirm: Button = v.findViewById(R.id.dprBtnConfirm)
        val dprBtnDenied: Button = v.findViewById(R.id.dprBtnDenied)

        // Nếu tin nhắn nhập vào khác trống
        if (title.isNotEmpty()) {
            dprTvTitle.text = title
        }

        // Khởi tạo recyclerview và danh sách liên quan
        dprRvListPermissions.setHasFixedSize(true)
        dprRvListPermissions.layoutManager = LinearLayoutManager(this)

        dprRvListPermissions.adapter = PermissionRequestAdapter(permissions)

        val mBuilder = AlertDialog
            .Builder(this)
            .setView(v)

        val mAlertDialog = mBuilder.show()

        v.scaleUpCenter()

        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mAlertDialog.setCancelable(false)

        _onCompleted = onCompleted
        dprBtnConfirm.onTap {
            v.scaleDownCenter {
                mAlertDialog.dismiss()
            }
            // Không thì hiện bảng yêu cầu
            ActivityCompat.requestPermissions(
                this,
                permissions.map { it.permission }.toTypedArray(),
                QUICK_PERMISISON_REQUEST_CODE
            )
        }
        dprBtnDenied.onTap {
            v.scaleDownCenter {
                mAlertDialog.dismiss()
            }
            onDenied?.invoke()
        }
    }

    fun FastdroidActivity.onRequestAllPermissionsResult(
        requestCode: Int,
        permissions: ArrayList<RequirePermission> = _permissions
    ) {
        if (_permissions.isEmpty())
            _permissions = permissions
        when (requestCode) {
            // Nếu mã trả về là mã yêu cầu quyền
            QUICK_PERMISISON_REQUEST_CODE -> {
                _onCompleted?.invoke(checkAllPermissions(permissions))
                _onCompleted = null
            }
        }
    }

    fun Context.isPermissionGranted(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun Context.isPermissionGranted(vararg permissions: String): Boolean {
        return permissions.all { isPermissionGranted(it) }
    }
}