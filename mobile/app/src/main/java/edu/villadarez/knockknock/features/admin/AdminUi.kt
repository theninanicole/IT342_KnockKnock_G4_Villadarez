package edu.villadarez.knockknock.features.admin

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import edu.villadarez.knockknock.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AdminUi {
    fun addDivider(container: LinearLayout) {
        val divider = View(container.context).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#E2E8F0"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply {
                setMargins(0, 14, 0, 14)
            }
        }
        container.addView(divider)
    }

    fun statusPill(context: Context, status: String): TextView {
        return TextView(context).apply {
            text = status.replace("-", " ").lowercase().replaceFirstChar { it.uppercase() }
            setTextColor(ContextCompat.getColor(context, R.color.status_scheduled_text))
            setTypeface(null, Typeface.BOLD)
            textSize = 12f
            gravity = android.view.Gravity.CENTER
            background = ContextCompat.getDrawable(context, R.drawable.bg_status_pill_scheduled)
            layoutParams = LinearLayout.LayoutParams(dp(context, 132), dp(context, 34))
        }
    }

    fun text(context: Context, value: String, size: Float, color: String, bold: Boolean = false): TextView {
        return TextView(context).apply {
            text = value
            textSize = size
            setTextColor(android.graphics.Color.parseColor(color))
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    fun formatDate(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        val input = value.substringBefore("T")
        return runCatching {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(input)
            SimpleDateFormat("MMMM d, yyyy", Locale.US).format(date!!)
        }.getOrDefault(input)
    }

    fun isToday(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        return value.substringBefore("T") == today
    }

    fun dp(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
}
