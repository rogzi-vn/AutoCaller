package vn.vistark.autocaller.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_campaign_detail.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.controller.campaign_detail.CampaignCall
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.PhoneCallState
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.services.BackgroundServiceCompanion.Companion.ClearDelayCallTimer
import vn.vistark.autocaller.services.BackgroundServiceCompanion.Companion.StartBackgroundService
import vn.vistark.autocaller.services.BackgroundServiceCompanion.Companion.broadcastReceiver
import vn.vistark.autocaller.services.BackgroundServiceCompanion.Companion.regisBroadcastReceiver
import vn.vistark.autocaller.services.BackgroundServiceCompanion.Companion.unregisBroadcastReceiver
import vn.vistark.autocaller.utils.SPUtils
import vn.vistark.autocaller.utils.call_phone.PhoneStateReceiver
import java.lang.Exception
import java.util.*


class BackgroundService : Service() {

    private val TAG: String = BackgroundService::javaClass.name

    // Phần khai báo liên quan đến thông báo (Notification)
    private val mNotificationChannelId = "Settings"
    private val mNotificationId = 140398

    override fun onCreate() {
        super.onCreate()
        // Khởi tạo các hằng số cơ bản và bộ nhớ lưu trữ cục bộ
        SPUtils.init(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // A. Tạo notification channel cho android phiên bản từ O đổ lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    mNotificationChannelId,
                    "Cài đặt",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    setShowBadge(true)
                }
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

            notiManager.createNotificationChannel(channel)
        }

        // B. Tạo pendingIntent cho notify
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        // c. Hiển thị noti và chạy services ngầm
        val notification: Notification = NotificationCompat.Builder(this, mNotificationChannelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Đang thực hiện")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notiManager.notify(mNotificationId, notification)

        // C. Tiến hành chạy Forefround (chạy dưới nền)
        startForeground(mNotificationId, notification)

        try {
            // Đăng ký broadcast khi có cuộc gọi đến để tạm ngừng chiến dịch
            registerReceiver(
                broadcastReceiverWhenPhoneComming,
                IntentFilter(PhoneStateReceiver.INCOMMING_CALL)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }



        try {
            // Đăng ký broadcast để tái khởi động chiến dịch khi cuộc gọi đến kết thúc
            registerReceiver(
                broadcastStopTemporarilyDone,
                IntentFilter(PhoneStateReceiver.STOP_TEMPORARILY_DONE)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopRegisReciver()
        super.onDestroy()
    }

    // Broad cast khi có cuộc gọi đến
    private var broadcastReceiverWhenPhoneComming: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.w(TAG, "onReceive: Có cuộc gọi đến")
                // Ngưng chiến dịch khi có cuộc gọi đến
//                try {
//                    CampaignCall.act?.pause()
//                    // Bỏ đăng ký nghe khi xong cuộc gọi
//                    stopRegisReciver()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
                unregisBroadcastReceiver()
                ClearDelayCallTimer()
                // Cho biết đây chỉ là hành động ngưng tạm thời
                BackgroundServiceCompanion.isStopTemporarily = true
            }
        }

    // Broad cast nhận lệnh tái khởi động khi cuộc gọi đến đã kết thúc
    private var broadcastStopTemporarilyDone: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Tái khởi động chiến dịch sau vài giây
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    // Kết thúc timer này
                    this.cancel()
//                    try {
//                        // Bấm nút tái khởi động nếu chưa được bấm
//                        if (CampaignCall.act?.acdBtnStart?.isEnabled == true)
//                            CampaignCall.act?.acdBtnStart?.performClick()
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
                    regisBroadcastReceiver()
                    // Cho biết không còn ngưng tạm thời nữa
                    BackgroundServiceCompanion.isStopTemporarily = false

                    // Bắt đầu cuộc gọi
                    if (BackgroundServiceCompanion.currentCampaign != null) {
                        CampaignCall.start(
                            this@BackgroundService.applicationContext,
                            BackgroundServiceCompanion.currentCampaign!!.id
                        )
                    }
                }

            }, 1500L)
        }
    }

    private fun stopRegisReciver() {
        unregisBroadcastReceiver()

        // Thực hiện các tác vụ tại đây
        try {
            // Hủy Đăng ký broadcast khi có cuộc gọi đến để tạm ngừng chiến dịch
            unregisterReceiver(broadcastReceiverWhenPhoneComming)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            // Hủy Đăng ký broadcast để tái khởi động chiến dịch khi cuộc gọi đến kết thúc
            unregisterReceiver(broadcastStopTemporarilyDone)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stopService(name: Intent?): Boolean {
        stopRegisReciver()
        return super.stopService(name)
    }
}