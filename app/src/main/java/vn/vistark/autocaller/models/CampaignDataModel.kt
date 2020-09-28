package vn.vistark.autocaller.models

class CampaignDataModel(
    // Mã khóa chính
    var id: Int = 0,

    // Mã khóa ngoại đến Campaign
    var campaignId: Int = 0,

    // Số điện thoại được nhập
    var phone: String? = "",

    // Trạng thái cuối cùng khi gọi đến số này
    var callState: Int = 0,

    // Vị trí thứ tự của số này trong danh sách số của chiến dịch
    var indexInCampaign: Int = 0,

    // Số đã được gọi hay chưa
    var isCalled: Boolean = false
) {

    companion object {
        const val TABLE_NAME = "campaign_data"
        const val ID = "id"
        const val CAMPAIGN_ID = "campaign_id"
        const val PHONE = "phone"
        const val CALL_STATE = "call_state"
        const val INDEX_IN_CAMPAIGN = "index_in_campaign"
        const val IS_CALLED = "is_called"
    }


}