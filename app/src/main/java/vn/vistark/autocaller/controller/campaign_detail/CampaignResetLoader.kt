package vn.vistark.autocaller.controller.campaign_detail

import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import cn.pedant.SweetAlert.SweetAlertDialog
import vn.vistark.autocaller.views.campaign_detail.CampaignDetailActivity

class CampaignResetLoader(val context: CampaignDetailActivity) :
    LoaderManager.LoaderCallbacks<Boolean> {

    // Hiển thị thể hiện việc đang reset
    private var loadingDialog: SweetAlertDialog? = null

    private fun cancelDialog() {
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            loadingDialog!!.dismissWithAnimation()
            loadingDialog!!.cancel()
        }
    }

    private fun showDialog() {
        cancelDialog()
        loadingDialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText("Đang tiến hành làm mới dữ liệu")
            .setContentText("ĐANG LÀM MỚI")
            .showCancelButton(false)
        loadingDialog?.setCancelable(false)
        loadingDialog?.show()
    }

    private fun showSuccessDialog() {
        cancelDialog()
        loadingDialog = SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("Hoàn tất làm mới. Bấm xác nhận để quay về màn hình danh sách chiến dịch.")
            .setContentText("HOÀN TẤT")
            .showCancelButton(false)
            .setConfirmButton("Xác nhận") {
                it.dismissWithAnimation()
                it.cancel()
                context.goBack()
            }
        loadingDialog?.show()
    }

    private fun showFailDialog() {
        cancelDialog()
        loadingDialog = SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Làm mới chưa được. Bấm xác nhận để quay về màn hình danh sách chiến dịch.")
            .setContentText("CHƯA THÀNH CÔNG")
            .showCancelButton(false)
            .setConfirmButton("Xác nhận") {
                it.dismissWithAnimation()
                it.cancel()
                context.goBack()
            }
        loadingDialog?.show()
    }

    init {
        val loaderManager = LoaderManager.getInstance(context)
        loaderManager.initLoader(3344, null, this)
        showDialog()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Boolean> {
        return CampaignResetATL(context)
    }

    override fun onLoadFinished(loader: Loader<Boolean>, data: Boolean) {
        cancelDialog()

        if (data) {
            showSuccessDialog()
        } else {
            showFailDialog()
        }
    }

    override fun onLoaderReset(loader: Loader<Boolean>) {
        // Bỏ qua
    }

}