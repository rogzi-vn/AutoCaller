package vn.vistark.autocaller.controller.campaign_detail

import androidx.loader.content.AsyncTaskLoader
import vn.vistark.autocaller.models.repositories.CampaignDataRepository
import vn.vistark.autocaller.models.repositories.CampaignRepository
import vn.vistark.autocaller.views.campaign_detail.CampaignDetailActivity

class CampaignResetATL(val context: CampaignDetailActivity) : AsyncTaskLoader<Boolean>(context) {

    init {
        onContentChanged()
    }

    override fun onStartLoading() {
        if (takeContentChanged())
            forceLoad()
    }

    override fun onStopLoading() {
        cancelLoad()
    }

    override fun loadInBackground(): Boolean {
        if (context.campaign == null)
            return false
        // Thiết lập các trạng thái về mặc định cho toàn bộ dữ liệu của chiến dịch
        var res = CampaignDataRepository(context).reset(context.campaign!!.id)

        // Nếu dữ liệu rest chưa hết, bảo họ reset lại
        if (res <= 0)
            return false

        // Tiến hành reset sang chiến dịch gốc
        res = CampaignRepository(context).reset(context.campaign!!.id)

        // nếu không đủ số hàng bị tác động
        if (res <= 0)
            return false

        // Nếu không có vấn đề gì, trả về kết quả đúng và kết thúc
        return true
    }

}