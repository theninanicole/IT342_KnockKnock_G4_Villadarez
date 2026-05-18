package edu.villadarez.knockknock.features.admin

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.villadarez.knockknock.R
import edu.villadarez.knockknock.core.network.RetrofitClient
import edu.villadarez.knockknock.databinding.ActivityAdminListBinding
import edu.villadarez.knockknock.features.visit.AdminHistoryItem
import edu.villadarez.knockknock.shared.session.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminStatusHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminListBinding
    private lateinit var sessionManager: SessionManager

    private var allHistory = emptyList<AdminHistoryItem>()
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        binding.tvScreenTitle.text = "Status History"
        binding.filterScroll.visibility = View.GONE
        setupNav()
        setupSearch()
        loadHistory()
    }

    private fun setupNav() {
        AdminNavHelper.setup(
            this,
            sessionManager,
            findViewById(R.id.tabStatusHistory),
            findViewById(R.id.tabHome),
            findViewById(R.id.tabAllVisits),
            findViewById(R.id.tabStatusHistory),
            findViewById(R.id.tabAccount),
            findViewById(R.id.tabLogout)
        )
    }

    private fun setupSearch() {
        binding.etSearchVisits.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString().orEmpty()
                renderHistory()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun loadHistory() {
        val token = sessionManager.fetchAuthToken() ?: return
        binding.progressAdminList.visibility = View.VISIBLE
        lifecycleScope.launch {
            allHistory = runCatching {
                RetrofitClient.visitService.getAdminHistory("Bearer $token").body()?.history.orEmpty()
            }.getOrDefault(emptyList())
                .filter { item ->
                    val (from, to) = parseTransition(item.transition)
                    !from.isNullOrBlank() && !to.isNullOrBlank() &&
                        !AdminUi.normalizeStatus(from).equals(AdminUi.normalizeStatus(to), ignoreCase = true)
                }
            renderHistory()
            binding.progressAdminList.visibility = View.GONE
        }
    }

    private fun renderHistory() {
        binding.containerItems.removeAllViews()
        val items = filteredHistory()

        if (items.isEmpty()) {
            binding.containerItems.addView(AdminUi.text(this, "No status history records found.", 14f, "#94A3B8"))
            return
        }

        items.groupBy { dateGroupLabel(it.timestamp) }.forEach { (dateLabel, groupItems) ->
            binding.containerItems.addView(dateHeader(dateLabel))
            groupItems.forEach { item ->
                binding.containerItems.addView(historyCard(item))
            }
        }
    }

    private fun filteredHistory(): List<AdminHistoryItem> {
        val query = searchQuery.trim().lowercase(Locale.US)
        if (query.isBlank()) return allHistory

        return allHistory.filter { item ->
            item.visitorName.orEmpty().lowercase(Locale.US).contains(query) ||
                item.referenceNumber.orEmpty().lowercase(Locale.US).contains(query)
        }
    }

    private fun historyCard(item: AdminHistoryItem): LinearLayout {
        val (from, to) = parseTransition(item.transition)
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBackground("#FFFFFF", "#E1E3E8", 22f)
            setPadding(
                AdminUi.dp(this@AdminStatusHistoryActivity, 28),
                AdminUi.dp(this@AdminStatusHistoryActivity, 24),
                AdminUi.dp(this@AdminStatusHistoryActivity, 28),
                AdminUi.dp(this@AdminStatusHistoryActivity, 24)
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, AdminUi.dp(this@AdminStatusHistoryActivity, 18))
            }

            addView(topRow(item))
            addView(transitionRow(from, to))
        }
    }

    private fun topRow(item: AdminHistoryItem): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP

            addView(LinearLayout(this@AdminStatusHistoryActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                addView(TextView(this@AdminStatusHistoryActivity).apply {
                    text = item.visitorName ?: "Visitor"
                    textSize = 17f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#111827"))
                    includeFontPadding = false
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                })

                addView(TextView(this@AdminStatusHistoryActivity).apply {
                    text = item.referenceNumber.orEmpty()
                    textSize = 15f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#8B8C93"))
                    includeFontPadding = false
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, AdminUi.dp(this@AdminStatusHistoryActivity, 6), AdminUi.dp(this@AdminStatusHistoryActivity, 12), 0)
                    }
                })
            })

            addView(TextView(this@AdminStatusHistoryActivity).apply {
                text = formatTime(item.timestamp)
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#8B8C93"))
                includeFontPadding = false
                gravity = Gravity.END
            })
        }
    }

    private fun transitionRow(from: String?, to: String?): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, AdminUi.dp(this@AdminStatusHistoryActivity, 18), 0, 0)
            }

            if (!from.isNullOrBlank()) {
                addView(AdminUi.statusPill(this@AdminStatusHistoryActivity, from))
            }
            if (!from.isNullOrBlank() && !to.isNullOrBlank()) {
                addView(TextView(this@AdminStatusHistoryActivity).apply {
                    text = "→"
                    textSize = 24f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#8B8C93"))
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    layoutParams = LinearLayout.LayoutParams(
                        AdminUi.dp(this@AdminStatusHistoryActivity, 48),
                        AdminUi.dp(this@AdminStatusHistoryActivity, 34)
                    )
                })
            }
            if (!to.isNullOrBlank()) {
                addView(AdminUi.statusPill(this@AdminStatusHistoryActivity, to))
            }
        }
    }

    private fun dateHeader(label: String): TextView {
        return TextView(this).apply {
            text = label
            textSize = 18f
            letterSpacing = 0.08f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#8B8C93"))
            includeFontPadding = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, AdminUi.dp(this@AdminStatusHistoryActivity, 16))
            }
        }
    }

    private fun parseTransition(transition: String?): Pair<String?, String?> {
        if (transition.isNullOrBlank()) return null to null
        val parts = when {
            transition.contains("→") -> transition.split("→", limit = 2)
            transition.contains("->") -> transition.split("->", limit = 2)
            else -> return transition to null
        }
        return parts.getOrNull(0)?.trim() to parts.getOrNull(1)?.trim()
    }

    private fun dateGroupLabel(value: String?): String {
        val date = parseDate(value) ?: return "UNKNOWN DATE"
        val dateText = SimpleDateFormat("MMMM d, yyyy", Locale.US).format(date).uppercase(Locale.US)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        val itemDay = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
        return if (itemDay == today) "TODAY · $dateText" else dateText
    }

    private fun formatTime(value: String?): String {
        val date = parseDate(value) ?: return "-"
        return SimpleDateFormat("h:mm a", Locale.US).format(date)
    }

    private fun parseDate(value: String?): Date? {
        if (value.isNullOrBlank()) return null
        val normalized = value.trim()
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (pattern in patterns) {
            val parsed = runCatching {
                SimpleDateFormat(pattern, Locale.US).parse(normalized)
            }.getOrNull()
            if (parsed != null) return parsed
        }
        return null
    }

    private fun roundedBackground(fill: String, stroke: String?, radiusDp: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor(fill))
            cornerRadius = radiusDp * resources.displayMetrics.density
            if (stroke != null) {
                setStroke(AdminUi.dp(this@AdminStatusHistoryActivity, 1), Color.parseColor(stroke))
            }
        }
    }
}
