package edu.villadarez.knockknock.features.admin

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import edu.villadarez.knockknock.R
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.villadarez.knockknock.databinding.ActivityAdminListBinding
import edu.villadarez.knockknock.core.network.RetrofitClient
import edu.villadarez.knockknock.shared.session.SessionManager
import kotlinx.coroutines.launch

class AdminAllVisitsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminListBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)
        binding.tvScreenTitle.text = "All Visits"
        setupNav()
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

    private fun loadVisits() {
        val token = sessionManager.fetchAuthToken() ?: return
        binding.progressAdminList.visibility = View.VISIBLE
        lifecycleScope.launch {
            val visits = runCatching {
                RetrofitClient.visitService.getAdminVisits("Bearer $token").body()?.visits.orEmpty()
            }.getOrDefault(emptyList())
            binding.containerItems.removeAllViews()
            if (visits.isEmpty()) {
                binding.containerItems.addView(AdminUi.text(this@AdminAllVisitsActivity, "No visits found.", 14f, "#94A3B8"))
            } else {
                visits.forEachIndexed { index, visit ->
                    if (index > 0) AdminUi.addDivider(binding.containerItems)
                    val row = LinearLayout(this@AdminAllVisitsActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                    }
                    val info = LinearLayout(this@AdminAllVisitsActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }
                    info.addView(AdminUi.text(this@AdminAllVisitsActivity, visit.visitor?.fullName ?: "Visitor", 16f, "#0F172A", true))
                    info.addView(AdminUi.text(this@AdminAllVisitsActivity, "${visit.referenceNumber} · ${AdminUi.formatDate(visit.visitDate)}", 13f, "#64748B"))
                    row.addView(info)
                    row.addView(AdminUi.statusPill(this@AdminAllVisitsActivity, visit.status))
                    binding.containerItems.addView(row)
                }
            }
            binding.progressAdminList.visibility = View.GONE
        }
    }
}
