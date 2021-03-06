package vn.vistark.autocaller.ui.service_provider_list.phone_prefixs

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_phone_prefix.*
import vn.vistark.autocaller.R
import vn.vistark.autocaller.models.storages.AppStorage
import vn.vistark.autocaller.ui.service_provider_list.ServiceProviderListActivity
import java.util.*

class PhonePrefixActivity : AppCompatActivity() {

    var sp: ServiceProvider = ServiceProvider()
    lateinit var adapter: PhoneNumberPrefixAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_prefix)

        // Thiết lập tiêu đề
        supportActionBar?.title = "Danh sách nhà mạng"

        // Hiển thị nút trở về
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        initSerialize()

        initRv()

        initDynamicAddPrefix()
    }

    private fun initDynamicAddPrefix() {
        addPhonePrefixConfirm.isEnabled = !prefixInput.text.isEmpty()
        prefixInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                addPhonePrefixConfirm.isEnabled = !prefixInput.text.isEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        addPhonePrefixConfirm.setOnClickListener {
            val prefix = prefixInput.text.toString()
            if (prefix.trim().isEmpty()) {
                Toasty.error(this, "Vui lòng nhập đầu số hợp lệ").show()
            } else {
                sp.phonePrefixs.add(prefix)
                adapter.notifyDataSetChanged()
                availableRv()
                prefixInput.text.clear()
                addPhonePrefixConfirm.isEnabled = !prefixInput.text.isEmpty()
            }
        }
    }

    private fun initSerialize() {
        val serviceProviderId = intent.getStringExtra("SERVICE_PROVIDER_ID")
        AppStorage.ServiceProviders.forEach {
            if (it.id == serviceProviderId) {
                sp = it
            }
        }
        if (sp.id.isEmpty()) {
            sp = ServiceProvider(UUID.randomUUID().toString())
        }
    }

    private fun initRv() {
        phonePrefixRv.layoutManager = LinearLayoutManager(this)
        phonePrefixRv.setHasFixedSize(true)

        edtServiceProviderName.setText(sp.serviceProviderName)
        abSwitchServiceState.isChecked = sp.state
        abSwitchServiceState.setOnClickListener {
            sp.state = abSwitchServiceState.isChecked
        }

        adapter = PhoneNumberPrefixAdapter(sp.phonePrefixs)
        availableRv()
        adapter.onClick = {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Bạn có thực sự muốn xóa đầu số '${it}' khỏi nhà mạng hiện tại?")
                .setContentText("XÓA ĐẦU SỐ")
                .setCancelText("Quay lại")
                .setConfirmText("Xóa ngay")
                .showCancelButton(true)
                .setCancelClickListener { sDialog -> sDialog.cancel() }
                .setConfirmClickListener { sDialog ->
                    sp.phonePrefixs.remove(it)
                    adapter.notifyDataSetChanged()
                    availableRv()
                    sDialog.dismissWithAnimation()
                    sDialog.cancel()
                }
                .show()
        }
        phonePrefixRv.adapter = adapter
    }

    fun availableRv() {
        if (sp.phonePrefixs.size <= 0)
            phonePrefixRv.visibility = View.GONE
        else
            phonePrefixRv.visibility = View.VISIBLE
    }

    // Trở về khi nhấn nút back
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    // Khởi tạo và chèn menu vào thanh ứng dụng ở trên, bên phải
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_right_save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.trSaveIcon -> {
                saveAction()
            }
            else -> {
//                Toasty.error(this, "Không tìm thấy tùy chọn này", Toasty.LENGTH_SHORT, true).show()
            }
        }
        return false
    }

    private fun saveAction() {
        if (edtServiceProviderName.text.isEmpty()) {
            Toasty.error(this, "Vui lòng cung cấp tên nhà mạng").show()
        } else if (AppStorage.ServiceProviders.any { x ->
                x.serviceProviderName.equals(
                    edtServiceProviderName.text.toString(),
                    true
                ) && x.id != sp.id
            }) {
            Toasty.error(this, "Tên nhà mạng đã bị trùng").show()
        } else if (sp.phonePrefixs.size <= 0) {
            Toasty.error(this, "Vui lòng cung cấp các đầu số thuộc nhà mạng này").show()
        } else {
            sp.serviceProviderName = edtServiceProviderName.text.toString()
            findAndUpdate()
            Toasty.success(
                this,
                "Đã lưu danh sách đầu số cho nhà mạng \"${sp.serviceProviderName}\" thành công"
            ).show()
            onBackPressed()
        }
    }

    private fun findAndUpdate() {
        AppStorage.ServiceProviders = AppStorage.ServiceProviders
            .filter { x -> x.id != sp.id }
            .plus(sp)
            .toTypedArray()
            .apply {
                this.sortBy { x -> x.serviceProviderName }
            }
    }

    override fun onBackPressed() {
        val intent = Intent(this, ServiceProviderListActivity::class.java)
        startActivity(intent)
        finish()
    }
}