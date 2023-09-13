package vn.vistark.autocaller.services

import android.app.ActivityManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import vn.vistark.autocaller.controller.campaign_detail.CampaignCall
import vn.vistark.autocaller.models.CampaignModel
import vn.vistark.autocaller.models.PhoneCallState
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.ui.campaign_detail.CampaignDetailActivity
import vn.vistark.autocaller.utils.call_phone.PhoneCallUtils
import vn.vistark.autocaller.utils.call_phone.PhoneStateReceiver
import java.util.*

class BackgroundServiceCompanion {
    companion object {
        private val TAG = Companion::class.java.name

        // Cờ lưu giá trị có dừng tạm khi khi xuất hiện cuộc gọi đến
        var isStopTemporarily = false

        // Cờ cho phép bắt đầu gọi liên tục
        var isStartCampaign = false

        var currentCampaign: CampaignModel? = null
        var broadcastReceiver: BroadcastReceiver? = null

        var timerDelayBeforeNextCall: Timer? = null

        var noSignalThresholdCountDown = AppStorage.ThresholdOfExitingAppIfNoSignalCalls

        fun Context.IsBackgroundServiceRunning(): Boolean {
            val manager = getSystemService(Service.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (BackgroundService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        fun Context.StopBackgroundService() {
            unregisBroadcastReceiver()
            isStartCampaign = false

            val intent = Intent(this, BackgroundService::class.java)
            stopService(intent)
        }

        fun Context.unregisBroadcastReceiver(isDestroyDelayCallTimer: Boolean = true) {
            if (isDestroyDelayCallTimer) {
                ClearDelayCallTimer()
            }
            try {
                // Bỏ đăng ký broadcast trước đó
                if (broadcastReceiver != null) {
                    unregisterReceiver(broadcastReceiver)
                    broadcastReceiver = null
                }
            } catch (_: Exception) {
                // Ignore this exception because it unmeaning
            }
        }

        fun Context.regisBroadcastReceiver() {
            // Broad cast khi thực hiện nhá máy
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    // Cập nhật vào CSDL
                    if (CampaignCall.currentCampaignData != null) {
                        val phoneDiffCall = PhoneCallUtils.getTimeHaveSignalInMilliseconds()
                        Log.d(TAG, "onReceive: phoneDiffCall =>>>>> [${phoneDiffCall}]")

                        CampaignCall.currentCampaignData!!.isCalled = true
                        CampaignCall.currentCampaignData!!.receivedSignalTimeInMilliseconds =
                            phoneDiffCall

                        if (phoneDiffCall <= AppStorage.ThresholdOfNoSignalCallInMilliseconds) {
                            CampaignCall.currentCampaignData!!.callState = PhoneCallState.NO_SIGNAL
                            --noSignalThresholdCountDown
                        } else {
                            noSignalThresholdCountDown =
                                AppStorage.ThresholdOfExitingAppIfNoSignalCalls
                            CampaignCall.currentCampaignData!!.callState = PhoneCallState.CALLED
                        }

                        CampaignCall.updateCallState(
                            this@regisBroadcastReceiver,
                            currentCampaign!!,
                            CampaignCall.currentCampaignData!!
                        )
                    }

                    if (noSignalThresholdCountDown <= 0 && AppStorage.ThresholdOfExitingAppIfNoSignalCalls > 0) {
                        Log.e("ERROR_CEPTION", "Choox nay" )
                        CampaignDetailActivity.stopAndExitApplication()
                    } else {
                        // Cập nhật progress
                        try {
                            CampaignCall.act?.initCampaignData()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // Renew trạng thái
                        PhoneStateReceiver.previousState = "EXTRA_STATE_IDLE"

                        //  Bắt đầu cuộc gọi tiếp theo sau DelayTimeInSeconds
                        timerDelayBeforeNextCall = Timer()
                        timerDelayBeforeNextCall!!.schedule(object : TimerTask() {
                            override fun run() {
                                this.cancel()
                                if (isStartCampaign && !isStopTemporarily)
                                    CampaignCall.start(
                                        this@regisBroadcastReceiver.applicationContext,
                                        currentCampaign!!.id
                                    )
                            }
                        }, AppStorage.DelayTimeInSeconds * 1000L)
                    }
                }
            }

            // Huỷ Đăng ký nghe khi xong cuộc gọi - Giữ Timer nếu có
            unregisBroadcastReceiver(false)

            // Đăng ký
            registerReceiver(broadcastReceiver, IntentFilter(PhoneStateReceiver.NAME))
        }

        fun ClearDelayCallTimer() {
            try {
                Log.w(
                    TAG,
                    "ClearDelayCallTimer: Đang tiến hành huỷ timer chờ gọi! $timerDelayBeforeNextCall"
                )
                if (timerDelayBeforeNextCall != null) {
                    timerDelayBeforeNextCall?.cancel()
                    timerDelayBeforeNextCall?.purge()
                    timerDelayBeforeNextCall = null
                }
            } catch (_: Exception) {
            }
        }

        fun Context.StartBackgroundService() {
            val intent = Intent(this, BackgroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
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

            StartBackgroundService()

            regisBroadcastReceiver()

            // Bắt đầu cuộc gọi
            CampaignCall.start(applicationContext, campaign.id)
        }
    }
}