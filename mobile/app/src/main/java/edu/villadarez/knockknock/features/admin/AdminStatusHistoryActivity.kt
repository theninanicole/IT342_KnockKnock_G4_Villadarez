package edu.villadarez.knockknock.features.admin

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import edu.villadarez.knockknock.R
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.villadarez.knockknock.databinding.ActivityAdminListBinding
import edu.villadarez.knockknock.core.network.RetrofitClient
import edu.villadarez.knockknock.shared.session.SessionManager
import kotlinx.coroutines.launch

class AdminStatusHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminListBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)
        binding.tvScreenTitle.text = "Status History"
        setupNav()
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

    private fun loadHistory() {
        val token = sessionManager.fetchAuthToken() ?: return
        binding.progressAdminList.visibility = View.VISIBLE
        lifecycleScope.launch {
            val history = runCatching {
                RetrofitClient.visitService.getAdminHistory("Bearer $token").body()?.history.orEmpty()
            }.getOrDefault(emptyList())
            binding.containerItems.removeAllViews()
            if (history.isEmpty()) {
                binding.containerItems.addView(AdminUi.text(this@AdminStatusHistoryActivity, "No status history records found.", 14f, "#94A3B8"))
            } else {
                history.forEachIndexed { index, item ->
                    if (index > 0) AdminUi.addDivider(binding.containerItems)
                    val row = LinearLayout(this@AdminStatusHistoryActivity).apply {
                        orientation = LinearLayout.VERTICAL
                    }
                    row.addView(AdminUi.text(this@AdminStatusHistoryActivity, item.visitorName ?: "Visitor", 16f, "#0F172A", true))
                    row.addView(AdminUi.text(this@AdminStatusHistoryActivity, item.referenceNumber.orEmpty(), 13f, "#64748B"))
                    row.addView(AdminUi.text(this@AdminStatusHistoryActivity, item.transition ?: "Status updated", 14f, "#2563EB", true))
                    row.addView(AdminUi.text(this@AdminStatusHistoryActivity, item.timestamp?.replace("T", " ") ?: "-", 12f, "#94A3B8"))
                    binding.containerItems.addView(row)
                }
            }
            binding.progressAdminList.visibility = View.GONE
        }
    }
}
