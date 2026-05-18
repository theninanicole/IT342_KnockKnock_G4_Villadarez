package edu.villadarez.knockknock.features.admin

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.villadarez.knockknock.R
import edu.villadarez.knockknock.core.network.RetrofitClient
import edu.villadarez.knockknock.databinding.ActivityAdminListBinding
import edu.villadarez.knockknock.features.visit.AdminVisit
import edu.villadarez.knockknock.shared.session.SessionManager
import kotlinx.coroutines.launch
import java.util.Locale

class AdminAllVisitsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminListBinding
    private lateinit var sessionManager: SessionManager

    private val filters = listOf("All", "Scheduled", "Checked-in", "Checked-out", "Cancelled", "Missed")
    private var allVisits = emptyList<AdminVisit>()
    private var activeFilter = "All"
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)
        binding.tvScreenTitle.text = "All Visits"
        setupNav()
        setupSearch()
        renderFilters()
        loadVisits()
    }

    private fun setupNav() {
        AdminNavHelper.setup(
            this,
            sessionManager,
            findViewById(R.id.tabAllVisits),
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
                renderVisits()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun loadVisits() {
        val token = sessionManager.fetchAuthToken() ?: return
        binding.progressAdminList.visibility = View.VISIBLE
        lifecycleScope.launch {
            allVisits = runCatching {
                RetrofitClient.visitService.getAdminVisits("Bearer $token").body()?.visits.orEmpty()
            }.getOrDefault(emptyList())
            renderFilters()
            renderVisits()
            binding.progressAdminList.visibility = View.GONE
        }
    }

    private fun renderFilters() {
        binding.containerFilters.removeAllViews()
        filters.forEach { filter ->
            val button = TextView(this).apply {
                val count = countForFilter(filter)
                text = if (filter == "All") "All $count" else filter
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                includeFontPadding = false
                minWidth = AdminUi.dp(this@AdminAllVisitsActivity, if (filter == "All") 72 else 112)
                setPadding(AdminUi.dp(this@AdminAllVisitsActivity, 16), 0, AdminUi.dp(this@AdminAllVisitsActivity, 16), 0)
                background = pillBackground(filter == activeFilter)
                setTextColor(Color.parseColor(if (filter == activeFilter) "#FFFFFF" else "#8B8C93"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    AdminUi.dp(this@AdminAllVisitsActivity, 40)
                ).apply {
                    setMargins(0, 0, AdminUi.dp(this@AdminAllVisitsActivity, 10), 0)
                }
                setOnClickListener {
                    activeFilter = filter
                    renderFilters()
                    renderVisits()
                }
            }
            binding.containerFilters.addView(button)
        }
    }

    private fun renderVisits() {
        binding.containerItems.removeAllViews()
        val visits = filteredVisits()

        if (visits.isEmpty()) {
            binding.containerItems.addView(AdminUi.text(this, "No visits found.", 16f, "#8B8C93", true))
            return
        }

        visits.groupBy { AdminUi.formatMonthYear(it.visitDate) }.forEach { (month, monthVisits) ->
            binding.containerItems.addView(monthHeader(month))
            monthVisits.forEach { visit ->
                binding.containerItems.addView(visitCard(visit))
            }
        }
    }

    private fun filteredVisits(): List<AdminVisit> {
        val query = searchQuery.trim().lowercase(Locale.US)
        return allVisits.filter { visit ->
            val matchesFilter = activeFilter == "All" || AdminUi.normalizeStatus(visit.status) == filterStatusCode(activeFilter)
            val matchesSearch = query.isBlank() ||
                visit.visitor?.fullName.orEmpty().lowercase(Locale.US).contains(query) ||
                visit.referenceNumber.lowercase(Locale.US).contains(query)
            matchesFilter && matchesSearch
        }
    }

    private fun countForFilter(filter: String): Int {
        if (filter == "All") return allVisits.size
        val statusCode = filterStatusCode(filter)
        return allVisits.count { AdminUi.normalizeStatus(it.status) == statusCode }
    }

    private fun filterStatusCode(filter: String): String {
        return filter.uppercase(Locale.US).replace(" ", "-")
    }

    private fun monthHeader(month: String): TextView {
        return TextView(this).apply {
            text = month
            textSize = 20f
            letterSpacing = 0.08f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#8B8C93"))
            includeFontPadding = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, AdminUi.dp(this@AdminAllVisitsActivity, 18))
            }
        }
    }

    private fun visitCard(visit: AdminVisit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = roundedBackground("#FFFFFF", "#E1E3E8", 22f)
            setPadding(
                AdminUi.dp(this@AdminAllVisitsActivity, 20),
                AdminUi.dp(this@AdminAllVisitsActivity, 20),
                AdminUi.dp(this@AdminAllVisitsActivity, 20),
                AdminUi.dp(this@AdminAllVisitsActivity, 20)
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AdminUi.dp(this@AdminAllVisitsActivity, 112)
            ).apply {
                setMargins(0, 0, 0, AdminUi.dp(this@AdminAllVisitsActivity, 18))
            }

            addView(infoColumn(visit))
            addView(AdminUi.statusPill(this@AdminAllVisitsActivity, visit.status))
        }
    }

    private fun infoColumn(visit: AdminVisit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, AdminUi.dp(this@AdminAllVisitsActivity, 12), 0)
            }

            addView(TextView(this@AdminAllVisitsActivity).apply {
                text = visit.visitor?.fullName ?: "Visitor"
                textSize = 17f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#111827"))
                includeFontPadding = false
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            })

            addView(TextView(this@AdminAllVisitsActivity).apply {
                text = visit.referenceNumber
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#8B8C93"))
                includeFontPadding = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, AdminUi.dp(this@AdminAllVisitsActivity, 5), 0, 0)
                }
            })

            addView(dateRow(visit))
        }
    }

    private fun dateRow(visit: AdminVisit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, AdminUi.dp(this@AdminAllVisitsActivity, 7), 0, 0)
            }

            addView(ImageView(this@AdminAllVisitsActivity).apply {
                setImageResource(R.drawable.ic_calendar_small)
                layoutParams = LinearLayout.LayoutParams(
                    AdminUi.dp(this@AdminAllVisitsActivity, 18),
                    AdminUi.dp(this@AdminAllVisitsActivity, 18)
                ).apply {
                    setMargins(0, 0, AdminUi.dp(this@AdminAllVisitsActivity, 8), 0)
                }
            })

            addView(TextView(this@AdminAllVisitsActivity).apply {
                text = AdminUi.formatDate(visit.visitDate)
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#5F5F61"))
                includeFontPadding = false
            })
        }
    }

    private fun pillBackground(selected: Boolean): GradientDrawable {
        return if (selected) {
            roundedBackground("#111827", null, 18f)
        } else {
            roundedBackground("#FFFFFF", "#D9DDE5", 18f)
        }
    }

    private fun roundedBackground(fill: String, stroke: String?, radiusDp: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor(fill))
            cornerRadius = radiusDp * resources.displayMetrics.density
            if (stroke != null) {
                setStroke(AdminUi.dp(this@AdminAllVisitsActivity, 1), Color.parseColor(stroke))
            }
        }
    }

}
