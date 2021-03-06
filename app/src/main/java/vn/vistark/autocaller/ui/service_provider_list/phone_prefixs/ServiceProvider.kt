package vn.vistark.autocaller.ui.service_provider_list.phone_prefixs

import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

data class ServiceProvider(
    var id: String = "",
    var state: Boolean = false,
    var serviceProviderName: String = "",
    var phonePrefixs: ArrayList<String> = ArrayList()
) {
}