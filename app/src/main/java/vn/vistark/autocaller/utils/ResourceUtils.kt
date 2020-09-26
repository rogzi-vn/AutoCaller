package vn.vistark.autocaller.utils

import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.Exception

class ResourceUtils {
    companion object {
        fun readText(context: AppCompatActivity, textResourceId: Int): String {
            try {
                val inp: InputStream = context.resources.openRawResource(textResourceId)
                return ByteArrayOutputStream().write(inp.readBytes()).toString()
            } catch (e: Exception) {

            }
            return ""
        }
    }
}