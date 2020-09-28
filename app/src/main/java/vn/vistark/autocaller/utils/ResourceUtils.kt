package vn.vistark.autocaller.utils

import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception

class ResourceUtils {
    companion object {
        fun readText(context: AppCompatActivity, textResourceId: Int): String {
            try {
                val inp: InputStream = context.resources.openRawResource(textResourceId)
                val reader = BufferedReader(InputStreamReader(inp))
                return reader.readText()
            } catch (e: Exception) {

            }
            return ""
        }
    }
}