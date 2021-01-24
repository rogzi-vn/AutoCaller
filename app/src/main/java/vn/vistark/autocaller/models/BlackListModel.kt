package vn.vistark.autocaller.models

// Thành phần đối tượng chiến dịch
class BlackListModel(
    // Mã, khóa chính
    var id: Int = 0,

    // Số điện thoại được nhập
    var phone: String? = ""
) {

    companion object {
        const val TABLE_NAME = "black_list"
        const val ID = "id"
        const val PHONE = "phone"
    }
}