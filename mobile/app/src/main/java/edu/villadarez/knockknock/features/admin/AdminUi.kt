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
        val code = normalizeStatus(status)
        val (label, backgroundRes, textColor) = when (code) {
            "SCHEDULED" -> Triple("Scheduled", R.drawable.bg_status_pill_scheduled, R.color.status_scheduled_text)
            "CHECKED-IN" -> Triple("Checked-In", R.drawable.bg_status_pill_checked_in, R.color.status_checked_in_text)
            "CHECKED-OUT" -> Triple("Checked-Out", R.drawable.bg_status_pill_checked_out, R.color.status_checked_out_text)
            "CANCELLED" -> Triple("Cancelled", R.drawable.bg_status_pill_cancelled, R.color.status_cancelled_text)
            "MISSED" -> Triple("Missed", R.drawable.bg_status_pill_missed, R.color.status_missed_text)
            else -> Triple(status.ifBlank { "Unknown" }, R.drawable.bg_status_pill_checked_out, R.color.status_checked_out_text)
        }

        return TextView(context).apply {
            text = label
            setTextColor(ContextCompat.getColor(context, textColor))
            setTypeface(null, Typeface.BOLD)
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            background = ContextCompat.getDrawable(context, backgroundRes)
            includeFontPadding = false
            minWidth = dp(context, 92)
            setPadding(dp(context, 16), 0, dp(context, 16), 0)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(context, 34))
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

    fun formatMonthYear(value: String?): String {
        if (value.isNullOrBlank()) return "UNSCHEDULED"
        val input = value.substringBefore("T")
        return runCatching {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(input)
            SimpleDateFormat("MMMM yyyy", Locale.US).format(date!!).uppercase(Locale.US)
        }.getOrDefault(input.uppercase(Locale.US))
    }

    fun normalizeStatus(status: String?): String {
        return status.orEmpty().trim().uppercase(Locale.US).replace("_", "-")
    }

    fun isToday(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        return value.substringBefore("T") == today
    }

    fun dp(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
}
