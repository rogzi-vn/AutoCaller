package vn.vistark.autocaller.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_campaign_detail.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.controller.campaign_detail.CampaignCall
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.PhoneCallState
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.utils.SPUtils
import vn.vistark.autocaller.utils.call_phone.PhoneStateReceiver
import java.lang.Exception
import java.util.*


class BackgroundService : Service() {

    companion object {
        // Cờ lưu giá trị có dừng tạm khi khi xuất hiện cuộc gọi đến
        var isStopTemporarily = false

        // Cờ cho phép bắt đầu gọi liên tục
        var isStartCampaign = false

        var currentCampaign: CampaignModel? = null
        var broadcastReceiver: BroadcastReceiver? = null

        fun Context.IsBackgroundServiceRunning(): Boolean {
            val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (BackgroundService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        fun Context.StopBackgroundService() {

            try {
                // Bỏ đăng ký broadcast trước đó
                if (broadcastReceiver != null) {
                    unregisterReceiver(broadcastReceiver)
                    broadcastReceiver = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            isStartCampaign = false

            val intent = Intent(this, BackgroundService::class.java)
            stopService(intent)
        }

        fun Context.StartBackgroundService(campaign: CampaignModel) {
            // Nếu đã chạy rồi thì bỏ qua
            if (IsBackgroundServiceRunning())
                return

            try {
                // Bỏ đăng ký broadcast trước đó
                if (broadcastReceiver != null) {
                    unregisterReceiver(broadcastReceiver)
                    broadcastReceiver = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            currentCampaign = campaign

            isStartCampaign = true
            isStopTemporarily = false

            val intent = Intent(this, BackgroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

            // Broad cast khi thực hiện nhá máy
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    // Cập nhật vào CSDL
                    if (CampaignCall.currentCampaignData != null) {
                        CampaignCall.currentCampaignData!!.callState = PhoneCallState.CALLED
                        CampaignCall.currentCampaignData!!.isCalled = true
                        CampaignCall.updateCallState(
                            this@StartBackgroundService,
                            currentCampaign!!,
                            CampaignCall.currentCampaignData!!
                        )
                    }

                    // Cập nhật progress
                    try {
                        CampaignCall.act?.initCampaignData()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // Renew trạng thái
                    PhoneStateReceiver.previousState = "EXTRA_STATE_IDLE"

                    //  Bắt đầu cuộc gọi tiếp theo sau DelayTimeInSeconds
                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            this.cancel()
                            if (isStartCampaign || isStopTemporarily)
                                CampaignCall.start(
                                    this@StartBackgroundService,
                                    currentCampaign!!.id
                                )
                        }
                    }, AppStorage.DelayTimeInSeconds * 1000L)
                }
            }

            // Đăng ký nghe khi xong cuộc gọi
            registerReceiver(broadcastReceiver, IntentFilter(PhoneStateReceiver.NAME))

            // Bắt đầu cuộc gọi
            CampaignCall.start(this, campaign.id)
        }
    }

    private val PERIOD = 5000.toLong()

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

        // Thực hiện các tác vụ tại đây
        try {
            // Đăng ký broadcast khi có cuộc gọi đến để tạm ngừng chiến dịch
            unregisterReceiver(broadcastReceiverWhenPhoneComming)
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
            unregisterReceiver(broadcastStopTemporarilyDone)
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
        super.onDestroy()
    }

    // Broad cast khi có cuộc gọi đến
    private var broadcastReceiverWhenPhoneComming: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Ngưng chiến dịch khi có cuộc gọi đến
                try {
                    CampaignCall.act?.pause()
                    // Bỏ đăng ký nghe khi xong cuộc gọi
                    stopRegisReciver()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                // Cho biết đây chỉ là hành động ngưng tạm thời
                isStopTemporarily = true
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
                    try {
                        // Bấm nút tái khởi động nếu chưa được bấm
                        if (CampaignCall.act?.acdBtnStart?.isEnabled == true)
                            CampaignCall.act?.acdBtnStart?.performClick()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // Cho biết không còn ngưng tạm thời nữa
                    isStopTemporarily = false
                }

            }, AppStorage.DelayTimeInSeconds * 1000L)
        }
    }

    private fun stopRegisReciver() {
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
        }
    }

    override fun stopService(name: Intent?): Boolean {
        stopRegisReciver()
        return super.stopService(name)
    }
}