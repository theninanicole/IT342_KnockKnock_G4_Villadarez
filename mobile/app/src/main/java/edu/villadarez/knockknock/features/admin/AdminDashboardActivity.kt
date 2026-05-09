package edu.villadarez.knockknock.features.admin

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import edu.villadarez.knockknock.R
import edu.villadarez.knockknock.core.network.RetrofitClient
import edu.villadarez.knockknock.databinding.ActivityAdminDashboardBinding
import edu.villadarez.knockknock.databinding.DialogAdminVisitDetailsBinding
import edu.villadarez.knockknock.features.auth.LoginActivity
import edu.villadarez.knockknock.features.visit.AdminVisit
import edu.villadarez.knockknock.shared.session.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var sessionManager: SessionManager
    private var visits: List<AdminVisit> = emptyList()
    private var referenceLookupJob: Job? = null
    private var lastVerifiedReference: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupNav()
        setupActions()
        loadAdminIdentity()
        loadVisits()
    }

    private fun setupNav() {
        AdminNavHelper.setup(
            this,
            sessionManager,
            findViewById(R.id.tabHome),
            findViewById(R.id.tabHome),
            findViewById(R.id.tabAllVisits),
            findViewById(R.id.tabStatusHistory),
            findViewById(R.id.tabAccount),
            findViewById(R.id.tabLogout)
        )
    }

    private fun setupActions() {
        binding.btnScan.setOnClickListener {
            Toast.makeText(this, "QR scanner coming next", Toast.LENGTH_SHORT).show()
        }

        binding.etReferenceSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                verifyReference(binding.etReferenceSearch.text.toString(), showErrors = true)
                true
            } else {
                false
            }
        }

        binding.etReferenceSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                scheduleReferenceLookup(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun loadAdminIdentity() {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            val response = runCatching { RetrofitClient.instance.getCurrentUser("Bearer $token") }.getOrNull()
            val user = response?.body()?.user
            binding.tvAdminInitials.text = user?.fullName?.let { initials(it) } ?: "A"
            binding.tvAdminGreeting.text = "Hello, ${user?.fullName?.substringBefore(" ") ?: "Admin"}"
        }
    }

    private fun loadVisits() {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding.progressAdmin.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.visitService.getAdminVisits("Bearer $token")
                if (response.isSuccessful) {
                    visits = response.body()?.visits.orEmpty()
                    renderLists()
                } else {
                    Toast.makeText(this@AdminDashboardActivity, "Unable to load admin visits", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminDashboardActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressAdmin.visibility = View.GONE
            }
        }
    }

    private fun renderLists() {
        val checkedIn = visits.filter { it.status.equals("CHECKED-IN", true) }
        val today = visits.filter { AdminUi.isToday(it.visitDate) && !it.status.equals("CHECKED-IN", true) }

        renderCheckedIn(checkedIn)
        renderToday(today)
    }

    private fun renderCheckedIn(items: List<AdminVisit>) {
        binding.containerCheckedIn.removeAllViews()
        if (items.isEmpty()) {
            binding.containerCheckedIn.addView(AdminUi.text(this, "No visitors are currently checked in.", 14f, "#94A3B8"))
            return
        }
        items.forEachIndexed { index, visit ->
            if (index > 0) AdminUi.addDivider(binding.containerCheckedIn)
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            info.addView(AdminUi.text(this, visit.visitor?.fullName ?: "Visitor", 17f, "#000000", true))
            info.addView(AdminUi.text(this, visit.referenceNumber, 14f, "#64748B"))
            val action = TextView(this).apply {
                text = "Check-Out"
                textSize = 12f
                setTextColor(Color.parseColor("#64748B"))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                background = ContextCompat.getDrawable(this@AdminDashboardActivity, R.drawable.bg_admin_pill_light)
                layoutParams = LinearLayout.LayoutParams(AdminUi.dp(context, 122), AdminUi.dp(context, 34))
                setOnClickListener { checkOut(visit.visitId) }
            }
            row.addView(info)
            row.addView(action)
            binding.containerCheckedIn.addView(row)
        }
    }

    private fun renderToday(items: List<AdminVisit>) {
        binding.containerTodaysVisitors.removeAllViews()
        if (items.isEmpty()) {
            binding.containerTodaysVisitors.addView(AdminUi.text(this, "No visitors scheduled for today.", 14f, "#94A3B8"))
            return
        }
        items.forEachIndexed { index, visit ->
            if (index > 0) AdminUi.addDivider(binding.containerTodaysVisitors)
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setOnClickListener { openDetails(visit.visitId) }
            }
            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            info.addView(AdminUi.text(this, visit.visitor?.fullName ?: "Visitor", 17f, "#000000", true))
            info.addView(AdminUi.text(this, visit.referenceNumber, 14f, "#64748B"))
            row.addView(info)
            row.addView(AdminUi.statusPill(this, visit.status))
            binding.containerTodaysVisitors.addView(row)
        }
    }

    private fun scheduleReferenceLookup(reference: String) {
        val trimmed = reference.trim()
        referenceLookupJob?.cancel()
        if (trimmed.length < 8 || trimmed == lastVerifiedReference) return

        referenceLookupJob = lifecycleScope.launch {
            delay(850)
            verifyReference(trimmed, showErrors = false)
        }
    }

    private fun verifyReference(reference: String, showErrors: Boolean) {
        val token = sessionManager.fetchAuthToken() ?: return
        val trimmed = reference.trim()
        if (trimmed.isBlank()) {
            if (showErrors) Toast.makeText(this, "Enter a reference number", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.visitService.getVisitByReference("Bearer $token", trimmed)
                if (response.isSuccessful && response.body() != null) {
                    lastVerifiedReference = trimmed
                    showVisitDetails(response.body()!!)
                } else {
                    if (showErrors) Toast.makeText(this@AdminDashboardActivity, "No eligible visit found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (showErrors) Toast.makeText(this@AdminDashboardActivity, "Unable to verify this visit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openDetails(visitId: String) {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            val response = runCatching { RetrofitClient.visitService.getVisitById("Bearer $token", visitId) }.getOrNull()
            val visit = response?.body() ?: visits.firstOrNull { it.visitId == visitId }
            if (visit != null) showVisitDetails(visit)
        }
    }

    private fun showVisitDetails(visit: AdminVisit) {
        val dialogBinding = DialogAdminVisitDetailsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        dialogBinding.tvReferenceNumber.text = visit.referenceNumber
        dialogBinding.tvVisitorName.text = visit.visitor?.fullName ?: "Visitor"
        dialogBinding.tvUnitNumber.text = visit.unitNumber ?: "-"
        dialogBinding.tvVisitDate.text = AdminUi.formatDate(visit.visitDate)
        dialogBinding.tvPurpose.text = visit.purpose ?: "-"
        dialogBinding.tvFileName.text = "Loading ID..."
        dialogBinding.btnCheckIn.visibility = if (visit.status.equals("SCHEDULED", true)) View.VISIBLE else View.GONE
        dialogBinding.footerDivider.visibility = dialogBinding.btnCheckIn.visibility

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnCheckIn.setOnClickListener {
            checkIn(visit.visitId, dialog)
        }

        loadVisitFile(visit.visitId, dialogBinding)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setOnShowListener {
            dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        }
        dialog.show()
    }

    private fun loadVisitFile(visitId: String, dialogBinding: DialogAdminVisitDetailsBinding) {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            val files = runCatching {
                RetrofitClient.visitService.getVisitFiles(visitId, "Bearer $token").body().orEmpty()
            }.getOrDefault(emptyList())
            val file = files.firstOrNull()
            dialogBinding.tvFileName.text = file?.fileName ?: "No ID uploaded"
            dialogBinding.btnViewFile.visibility = if (file?.fileUrl.isNullOrBlank()) View.GONE else View.VISIBLE
            dialogBinding.fileRow.setOnClickListener {
                file?.fileUrl?.let { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
            }
        }
    }

    private fun checkIn(visitId: String, dialog: AlertDialog) {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            val response = runCatching { RetrofitClient.visitService.checkInVisit("Bearer $token", visitId) }.getOrNull()
            if (response?.isSuccessful == true) {
                Toast.makeText(this@AdminDashboardActivity, "Visitor checked in", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                loadVisits()
            } else {
                Toast.makeText(this@AdminDashboardActivity, "Unable to check in visitor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkOut(visitId: String) {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            val response = runCatching { RetrofitClient.visitService.checkOutVisit("Bearer $token", visitId) }.getOrNull()
            if (response?.isSuccessful == true) {
                Toast.makeText(this@AdminDashboardActivity, "Visitor checked out", Toast.LENGTH_SHORT).show()
                loadVisits()
            } else {
                Toast.makeText(this@AdminDashboardActivity, "Unable to check out visitor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initials(name: String): String =
        name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "A" }
}
