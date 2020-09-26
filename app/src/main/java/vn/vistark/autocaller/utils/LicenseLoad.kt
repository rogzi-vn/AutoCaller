package vn.vistark.autocaller.utils

import android.os.AsyncTask
import com.google.gson.Gson
import vn.vistark.autocaller.models.app_license.AppLicense
import java.net.URL

class LicenseLoad : AsyncTask<Void, Void, AppLicense>() {

    companion object {
        val LICENSE_JSON_ADDRESS: String =
            "https://raw.githubusercontent.com/futureskyprojects/CodeExperience/master/AutoCallerLicense.json"
    }

    var onFinished: ((AppLicense) -> Unit)? = null
    override fun doInBackground(vararg p0: Void?): AppLicense {
        return try {
            val json = URL(LICENSE_JSON_ADDRESS).readText(Charsets.UTF_8)
            Gson().fromJson(json, AppLicense::class.java)
        } catch (e: Exception) {
            AppLicense()
        }
    }

    override fun onPostExecute(result: AppLicense?) {
        super.onPostExecute(result)
        onFinished?.invoke(result ?: AppLicense())
    }
}