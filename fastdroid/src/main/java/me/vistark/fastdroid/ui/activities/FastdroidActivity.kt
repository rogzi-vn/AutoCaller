package me.vistark.fastdroid.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import me.vistark.fastdroid.broadcasts.FastdroidBroadcastReceiver
import me.vistark.fastdroid.broadcasts.FastdroidBroadcastReceiver.Companion.FASTDROID_BROADCAST_ACTION
import me.vistark.fastdroid.language.LanguageConfig
import me.vistark.fastdroid.utils.FastdroidContextWrapper
import me.vistark.fastdroid.utils.FastdroidContextWrapper.Companion.forOnCreate
import me.vistark.fastdroid.utils.MultipleLanguage.autoTranslate
import me.vistark.fastdroid.utils.PermissionUtils.onRequestAllPermissionsResult
import me.vistark.fastdroid.utils.keyboard.HideKeyboardExtension.Companion.HideKeyboard
import me.vistark.fastdroid.utils.storage.AppStorageManager


abstract class FastdroidActivity(
    val layoutId: Int,
    val isLimit: Boolean = true,
    val isHaveActionBar: Boolean = false,
    var isCanAutoTranslate: Boolean = false,
    var actionBarBackground: Int = -1,
    var windowBackground: Int = -1
) : AppCompatActivity() {
    var statusBarHeight: Int = 0
    private var mFastdroidBroadcastReciver: FastdroidBroadcastReceiver? = null

    var reqOri = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    override fun onCreate(savedInstanceState: Bundle?) {
        if (windowBackground > 0)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        // Thiết lập màu nền cho ứng dụng
        if (windowBackground > 0) {
            window.setBackgroundDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    windowBackground,
                    theme
                )
            )
        }

        // Khởi tạo bộ lưu trữ shared preference mặc định
        AppStorageManager.initialize(this)
        // Cấu hình ngôn ngữ
        forOnCreate()
        // Cấu hình cho khả năng dịch
        if (isCanAutoTranslate)
            window.decorView.autoTranslate()
        // Cấu hình ẩn bàn phím khi nhấn bên ngoài
        checkForSetHide(window.decorView.rootView)
        // Full màn hình hoặc không
        if (!isLimit) {
            // Ẩn actionbar hoặc không
            if (!isHaveActionBar)
                supportActionBar?.hide()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )
            }
        }

        // Ẩn actionbar hoặc không
        if (!isHaveActionBar && actionBarBackground <= 0)
            supportActionBar?.hide()

        // Nếu có cho đổi màu action bar
        if (actionBarBackground > 0) {
            supportActionBar?.setBackgroundDrawable(
                ResourcesCompat.getDrawable(resources, actionBarBackground, theme)
            )
        }

        // Cấu hình quay
        requestedOrientation = reqOri

        // Đẩy màn hình layout lên khi xuất hiện bàn phím
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            super.attachBaseContext(
                FastdroidContextWrapper.wrap(
                    newBase,
                    LanguageConfig.LanguageCode
                )
            )
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        HideKeyboard()
        return super.onTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun checkForSetHide(view: View) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                HideKeyboard()
                return@setOnTouchListener false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView: View = view.getChildAt(i)
                checkForSetHide(innerView)
            }
        }
    }

    //region PickImage
    private val REQUEST_CODE_PICK_IMAGE: Int = 324212

    var pickImageBitmapResult: ((Bitmap?) -> Unit)? = null
    private fun pickImageForBitmap(pickImageResult: ((Bitmap?) -> Unit)) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        this.pickImageBitmapResult = pickImageResult
    }

    var pickImageUriResult: ((Uri?) -> Unit)? = null
    private fun pickImageForUri(pickImageResult: ((Uri?) -> Unit)) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        this.pickImageUriResult = pickImageResult
    }
    //endregion

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pick image for bitmap or uri
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            pickImageUriResult?.invoke(data?.data)
            if (data?.data != null) {
                pickImageBitmapResult?.invoke(
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(
                                    this.contentResolver,
                                    data.data!!
                                )
                            )
                        } else {
                            MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
                        }
                    } catch (e: Exception) {
                        null
                    }
                )
            } else {
                pickImageBitmapResult?.invoke(null)
            }
        }
    }

    fun startSingleActivity(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    fun onStatusbarHeight(action: (Int) -> Unit) {
        if (statusBarHeight > 0) {
//            println(">>>>>>>>>>>>>>> [EXISTS]: $statusBarHeight")
            action.invoke(statusBarHeight)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            window.decorView.setOnApplyWindowInsetsListener { _, insets ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    statusBarHeight = insets.getInsets(WindowInsets.Type.statusBars()).top
//                    println(">>>>>>>>>>>>>>> [Build.VERSION_CODES.R]: $statusBarHeight")
                    action.invoke(statusBarHeight)
                } else {
                    statusBarHeight = insets.systemWindowInsetTop
//                    println(">>>>>>>>>>>>>>> [Build.VERSION_CODES.KITKAT_WATCH]: $statusBarHeight")
                    action.invoke(statusBarHeight)
                }
                return@setOnApplyWindowInsetsListener insets
            }
        } else {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                statusBarHeight = resources.getDimensionPixelSize(resourceId)
//                println(">>>>>>>>>>>>>>> [OLD]: $statusBarHeight")
                action.invoke(statusBarHeight)
            } else {
//                println(">>>>>>>>>>>>>>> [NOOO]: $statusBarHeight")
                action.invoke(0)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestAllPermissionsResult(requestCode)
    }

    // Khởi tạo bộ nhận tín hiệu chung
    open fun onSignal(key: String, value: String) {
    }

    private fun initFastdroidBroadcastReciver() {
        mFastdroidBroadcastReciver = FastdroidBroadcastReceiver().apply {
            onSignalEvent = { k, v ->
                onSignal(k, v)
            }
        }
        val filter = IntentFilter(FASTDROID_BROADCAST_ACTION)
        registerReceiver(mFastdroidBroadcastReciver, filter)
    }

    override fun onStart() {
        super.onStart()
        initFastdroidBroadcastReciver()
    }

    override fun onStop() {
        super.onStop()
        try {
            if (mFastdroidBroadcastReciver != null) {
                unregisterReceiver(mFastdroidBroadcastReciver)
            }
            mFastdroidBroadcastReciver = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}