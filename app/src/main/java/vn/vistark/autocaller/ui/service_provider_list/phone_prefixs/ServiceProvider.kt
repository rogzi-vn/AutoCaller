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
    companion object {
        val defaultServiceProviders = arrayOf(
            ServiceProvider(
                UUID.randomUUID().toString(), false, "VIETTEL",
                arrayListOf(
                    "097",
                    "098",
                    "096",
                    "032",
                    "033",
                    "034",
                    "035",
                    "036",
                    "037",
                    "038",
                    "039",
                    "086"
                )
            ),
            ServiceProvider(
                UUID.randomUUID().toString(), false, "MOBIFONE",
                arrayListOf("090", "093", "070", "079", "076", "077", "078", "089")
            ),
            ServiceProvider(
                UUID.randomUUID().toString(), false, "VINAPHONE",
                arrayListOf(
                    "091",
                    "094",
                    "088",
                    "083",
                    "084",
                    "085",
                    "081",
                    "082",
                    "095",
                    "0195",
                    "087"
                )
            ),
            ServiceProvider(
                UUID.randomUUID().toString(), false, "KHÁC",
                arrayListOf(
                    "092",
                    "0188",
                    "058",
                    "056",
                    "0199",
                    "0996"
                )
            )
        )
    }
}