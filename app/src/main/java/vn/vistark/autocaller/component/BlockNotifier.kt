package vn.vistark.autocaller.component

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import vn.vistark.autocaller.R

class BlockNotifier(v: View) {

    private val bnRlRoot: RelativeLayout? = v.findViewById(R.id.bnRlRoot)
    private val bnIvIcon: AppCompatImageView? = v.findViewById(R.id.bnIvIcon)
    private val bnTvContent: TextView? = v.findViewById(R.id.bnTvContent)

    fun show(message: String, imgResId: Int) {
        bnIvIcon?.post {
            bnIvIcon.setImageResource(imgResId)
        }
        show(message)
    }

    fun show(message: String) {
        bnTvContent?.post {
            bnTvContent.text = message
        }
        show()
    }

    fun show() {
        bnRlRoot?.post {
            bnRlRoot.visibility = View.VISIBLE
        }
    }

    fun hide() {
        bnRlRoot?.post {
            bnRlRoot.visibility = View.GONE
        }
    }
}