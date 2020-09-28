package vn.vistark.autocaller.models

// Thành phần đối tượng chiến dịch
class CampaignModel(
    // Mã, khóa chính
    var id: Int = 0,

    // Tên chiến dịch
    var name: String = "",

    // Mã trong CampaignData của số điện thoại được gọi cuối cùng
    var lastPhoneId: Int = 0,

    // Tổng số SĐT đã được nhập
    var totalImported: Int = 0,

    // Tổng số SĐT đã được gọi đi
    var totalCalled: Int = 0,

    // Tổng số cuộ gọi lỗi
    var totalFail: Int = 0
) {

    companion object {
        const val TABLE_NAME = "campaign"
        const val ID = "id"
        const val NAME = "name"
        const val LAST_PHONE_ID = "last_phone_id"
        const val TOTAL_IMPORTED = "total_imported"
        const val TOTAL_CALLED = "total_called"
        const val TOTAL_FAIL = "total_fail"
    }
}