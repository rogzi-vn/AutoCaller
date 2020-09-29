package vn.vistark.autocaller.models

class PhoneCallState {
    companion object {
        const val NOT_CALL = 0
        const val CALLED = 1
        const val MISSIED_CALL = 2
        const val HANG_ON = 3
        const val PHONE_NUMBER_ERROR = 4
    }
}