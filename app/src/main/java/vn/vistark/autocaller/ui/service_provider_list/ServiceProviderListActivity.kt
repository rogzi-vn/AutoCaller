package vn.vistark.autocaller.ui.service_provider_list

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.android.synthetic.main.activity_service_provider_list.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.ui.service_provider_list.phone_prefixs.PhonePrefixActivity

class ServiceProviderListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_provider_list)

        // Thiết lập tiêu đề
        supportActionBar?.title = "Danh sách nhà mạng"

        // Hiển thị nút trở về
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        serviceProviderCount.text =
            "Danh sách nhà mạng đã tạo (${AppStorage.ServiceProviders.size})"

        initRv()
    }

    private fun initRv() {
        serviceProvicerRv.layoutManager = LinearLayoutManager(this)
        serviceProvicerRv.setHasFixedSize(true)

        val adapter = ServiceProviderAdapter()
        availableRv()
        adapter.onClick = {
            val intent = Intent(this, PhonePrefixActivity::class.java)
            intent.putExtra("SERVICE_PROVIDER_ID", it.id)
            startActivity(intent)
            finish()
        }
        adapter.onLongClick = {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Bạn có thực sự muốn xóa nhà mạng ${it.serviceProviderName}? (Không thể hoàn tác)")
                .setContentText("XÓA ĐẦU SỐ")
                .setCancelText("Quay lại")
                .setConfirmText("Xóa ngay")
                .showCancelButton(true)
                .setCancelClickListener { sDialog -> sDialog.cancel() }
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    sDialog.cancel()
                    removeServiceProvider(it.id)
                }
                .show()
        }
        serviceProvicerRv.adapter = adapter
    }

    fun availableRv() {
        if (AppStorage.ServiceProviders.isEmpty()) {
            serviceProvicerRv.visibility = View.GONE
        } else {
            serviceProvicerRv.visibility = View.VISIBLE
        }
    }

    private fun removeServiceProvider(id: String) {
        AppStorage.ServiceProviders =
            AppStorage.ServiceProviders.filter { x -> x.id != id }.toTypedArray()
        val intent = Intent(this, ServiceProviderListActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Trở về khi nhấn nút back
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    // Khởi tạo và chèn menu vào thanh ứng dụng ở trên, bên phải
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_right_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.trAddIcon -> {
                val intent = Intent(this, PhonePrefixActivity::class.java)
                intent.putExtra("SERVICE_PROVIDER_ID", "")
                startActivity(intent)
                finish()
            }
            else -> {
//                Toasty.error(this, "Không tìm thấy tùy chọn này", Toasty.LENGTH_SHORT, true).show()
            }
        }
        return false
    }
}