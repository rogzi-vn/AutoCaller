package vn.vistark.autocaller.models.storages

import vn.vistark.autocaller.models.app_license.AppLicense
import vn.vistark.autocaller.ui.service_provider_list.phone_prefixs.ServiceProvider
import vn.vistark.autocaller.ui.service_provider_list.phone_prefixs.ServiceProvider.Companion.defaultServiceProviders
import vn.vistark.autocaller.utils.AppStorageManager
import vn.vistark.autocaller.utils.SPUtils

class AppStorage {
    companion object {
        const val MAX_LOGIN_FAIL = 5

        var ServiceProviders: Array<ServiceProvider>
            get() = AppStorageManager.get("ServiceProviders") ?: emptyArray()
            set(value) {
                AppStorageManager.update("ServiceProviders", value)
            }

        // Đường dẫn blacklink mặc định
        var GoogleSheetBlackLink: String
            get() {
                return SPUtils.sp?.getString("GoogleSheetBlackLink", null)
                    ?: "https://docs.google.com/spreadsheets/d/e/2PACX-1vTDW6pfNStd63FdUJ61VMwk5-OetUWz_H0OAVbkjHKotKo0tvnc1nmmLNHEaAo9HkRWlJO3NIvGMKCl/pub?gid=0&single=true&output=csv"
            }
            set(appPassword) {
                SPUtils.sp?.edit()?.apply {
                    putString("GoogleSheetBlackLink", appPassword)
                }?.apply()
            }

        var GoogleSheetCampaigns: String
            get() {
                return SPUtils.sp?.getString("GoogleSheetCampaigns", null)
                    ?: "https://docs.google.com/spreadsheets/d/e/2PACX-1vTDW6pfNStd63FdUJ61VMwk5-OetUWz_H0OAVbkjHKotKo0tvnc1nmmLNHEaAo9HkRWlJO3NIvGMKCl/pub?gid=1776004578&single=true&output=csv"
            }
            set(appPassword) {
                SPUtils.sp?.edit()?.apply {
                    putString("GoogleSheetCampaigns", appPassword)
                }?.apply()
            }

        // Mật khẩu mặc định để truy cập ứng dụng
        var AppPassword: String
            get() {
                return SPUtils.sp?.getString("AppPassword", AppLicense().AppPassword)
                    ?: AppLicense().AppPassword
            }
            set(appPassword) {
                SPUtils.sp?.edit()?.apply {
                    putString("AppPassword", appPassword)
                }?.apply()
            }

        // Mật khẩu mà người dùng đã nhập
        var UserPassword: String
            get() {
                return SPUtils.sp?.getString("UserPassword", "")
                    ?: ""
            }
            set(appPassword) {
                SPUtils.sp?.edit()?.apply {
                    putString("UserPassword", appPassword)
                }?.apply()
            }

        // Bật tắt chế độ tắt máy ngay khi người dùng trả lời
        var IsHangUpAsSoonAsUserAnswer: Boolean
            get() {
                return SPUtils.sp?.getBoolean("IsHangUpAsSoonAsUserAnswer", true)
                    ?: true
            }
            set(count) {
                SPUtils.sp?.edit()?.apply {
                    putBoolean("IsHangUpAsSoonAsUserAnswer", count)
                }?.apply()
            }

        // Số lần đăng nhập sai
        var LoginFail: Int
            get() {
                return SPUtils.sp?.getInt("LoginFail", 0)
                    ?: 0
            }
            set(count) {
                SPUtils.sp?.edit()?.apply {
                    putInt("LoginFail", count)
                }?.apply()
            }

        // Thời gian delay sau mỗi cuộc gọi, đơn vị tính bằng giây
        var DelayTimeInSeconds: Int
            get() {
                return SPUtils.sp?.getInt("DelayTimeInSeconds", 10) ?: 10
            }
            set(value) {
                SPUtils.sp?.edit()?.apply {
                    putInt("DelayTimeInSeconds", value)
                }?.apply()
            }

        // Thời gian thực hiện mỗi cuộc gọi
        var DelayTimeCallInSeconds: Int
            get() {
                return SPUtils.sp?.getInt("DelayTimeCallInSeconds", 6) ?: 6
            }
            set(value) {
                SPUtils.sp?.edit()?.apply {
                    putInt("DelayTimeCallInSeconds", value)
                }?.apply()
            }

        // settingEdtTimerAutoRunCampaign
        var TimerAutoRunCampaignInSeconds: Int
            get() {
                return SPUtils.sp?.getInt("TimerAutoRunCampaignInSeconds", 10) ?: 10
            }
            set(value) {
                SPUtils.sp?.edit()?.apply {
                    putInt("TimerAutoRunCampaignInSeconds", value)
                }?.apply()
            }

        // settingEdtThresholdOfNoSignalCall
        var ThresholdOfNoSignalCallInMilliseconds: Long
            get() {
                return SPUtils.sp?.getLong("ThresholdOfNoSignalCallInMilliseconds", 3000L) ?: 3000L
            }
            set(value) {
                SPUtils.sp?.edit()?.apply {
                    putLong("ThresholdOfNoSignalCallInMilliseconds", value)
                }?.apply()
            }

        // settingEdtCountOfNoSignalCallToExit
        var ThresholdOfExitingAppIfNoSignalCalls: Int
            get() {
                return SPUtils.sp?.getInt("ThresholdOfExitingAppIfNoSignalCalls", 20) ?: 20
            }
            set(value) {
                SPUtils.sp?.edit()?.apply {
                    putInt("ThresholdOfExitingAppIfNoSignalCalls", value)
                }?.apply()
            }

        // scAutoReOpenAppIfShutdownSuddenly
        var IsAutoReopenAppIfShutdownSuddenly: Boolean
            get() {
                return SPUtils.sp?.getBoolean("IsAutoReopenAppIfShutdownSuddenly", true)
                    ?: true
            }
            set(count) {
                SPUtils.sp?.edit()?.apply {
                    putBoolean("IsAutoReopenAppIfShutdownSuddenly", count)
                }?.apply()
            }
    }
}