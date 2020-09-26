package vn.vistark.autocaller.models.app_license

data class AppState(
    val AllowRun: Boolean = false,
    val Message: String = "Vui lòng thanh toán số tiền còn lại trước khi có thể tiến hành sử dụng tiếp ứng dụng"
)