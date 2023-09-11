package vn.vistark.autocaller.models

class PhoneCallState {
    companion object {
        const val NOT_CALL = 0
        const val CALLED = 1
        const val MISSIED_CALL = 2
        const val HANG_ON = 3
        const val PHONE_NUMBER_ERROR = 4
        const val BLACK_LIST_IGNORED = 5
        const val SERVICE_PROVIDER_IGNORED = 6
        const val NO_SIGNAL = 7 // Khi được cho là tín hiệu bị lỗi
    }
}