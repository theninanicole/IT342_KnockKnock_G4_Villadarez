package edu.villadarez.knockknock.features.visit

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.villadarez.knockknock.R
import edu.villadarez.knockknock.core.network.RetrofitClient
import edu.villadarez.knockknock.databinding.ActivityMyVisitsBinding
import edu.villadarez.knockknock.features.auth.LoginActivity
import edu.villadarez.knockknock.shared.session.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MyVisitsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyVisitsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var visitAdapter: VisitAdapter
    private var currentVisit: VisitSummary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyVisitsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupRecyclerView()
        setupBottomNav()

        loadVisitsData()
    }

    override fun onResume() {
        super.onResume()
        NotificationBadgeHelper.refresh(lifecycleScope, sessionManager, binding.badgeNotificationCount)
    }

    private fun setupRecyclerView() {
        visitAdapter = VisitAdapter(emptyList()) { visit ->
            currentVisit = visit
            showVisitDetailsModal(visit)
        }
        binding.rvVisits.layoutManager = LinearLayoutManager(this)
        binding.rvVisits.adapter = visitAdapter
    }

    private fun setupBottomNav() {
        val tabs = listOf(
            binding.tabHome,
            binding.tabMyVisits,
            binding.tabNotifications,
            binding.tabAccount,
            binding.tabLogout
        )

        fun setActiveTab(active: TextView) {
            val activeColor = ContextCompat.getColor(this, R.color.brand_blue_primary)
            val inactiveColor = ContextCompat.getColor(this, R.color.nav_unselected)

            tabs.forEach { tab ->
                val isActive = tab == active
                val color = if (isActive) activeColor else inactiveColor

                tab.setTextColor(color)
                tab.setTypeface(null, if (isActive) Typeface.BOLD else Typeface.NORMAL)

                val drawables = tab.compoundDrawablesRelative
                drawables.forEach { drawable ->
                    drawable?.mutate()?.setTint(color)
                }
                tab.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    drawables[0], drawables[1], drawables[2], drawables[3]
                )
            }
        }

        setActiveTab(binding.tabMyVisits)

        binding.tabHome.setOnClickListener {
            setActiveTab(binding.tabHome)
            startActivity(Intent(this, VisitorDashboardActivity::class.java))
            finish()
        }

        binding.tabMyVisits.setOnClickListener {
            setActiveTab(binding.tabMyVisits)
        }

        binding.tabNotifications.setOnClickListener {
            setActiveTab(binding.tabNotifications)
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        binding.tabAccount.setOnClickListener {
            setActiveTab(binding.tabAccount)
            startActivity(Intent(this, AccountActivity::class.java))
        }

        binding.tabLogout.setOnClickListener {
            setActiveTab(binding.tabLogout)
            sessionManager.saveAuthToken("")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadVisitsData() {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val bearer = "Bearer $token"

        binding.progressVisits.visibility = View.VISIBLE
        binding.tvEmptyVisits.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val meResponse = RetrofitClient.instance.getCurrentUser(bearer)
                if (meResponse.isSuccessful) {
                    val user = meResponse.body()?.user
                    val firstName = user?.fullName?.split(" ")?.firstOrNull() ?: "Visitor"
                }

                val role = meResponse.body()?.user?.role
                if (role != null && !role.equals("VISITOR", ignoreCase = true)) {
                    Toast.makeText(this@MyVisitsActivity, "This screen is for visitors only", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@MyVisitsActivity, LoginActivity::class.java))
                    finish()
                    return@launch
                }

                val visitsResponse = RetrofitClient.visitService.getMyVisits(bearer)
                if (visitsResponse.isSuccessful) {
                    val visits = visitsResponse.body()?.visits ?: emptyList()
                    updateVisits(visits)
                } else {
                    Toast.makeText(this@MyVisitsActivity, "Failed to load visits", Toast.LENGTH_SHORT).show()
                    updateVisits(emptyList())
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyVisitsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                updateVisits(emptyList())
            } finally {
                binding.progressVisits.visibility = View.GONE
            }
        }
    }

    private fun updateVisits(visits: List<VisitSummary>) {
        if (visits.isEmpty()) {
            binding.tvEmptyVisits.visibility = View.VISIBLE
        } else {
            binding.tvEmptyVisits.visibility = View.GONE
        }
        visitAdapter.updateData(visits)
    }

    private fun showVisitDetailsModal(visit: VisitSummary) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_visit_details, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set up the modal content
        val tvCondoName = dialogView.findViewById<TextView>(R.id.tvCondoNameDetail)
        val tvUnitNumber = dialogView.findViewById<TextView>(R.id.tvUnitNumberDetail)
        val tvVisitDate = dialogView.findViewById<TextView>(R.id.tvVisitDateDetail)
        val tvPurpose = dialogView.findViewById<TextView>(R.id.tvPurposeDetail)
        val tvIDDetail = dialogView.findViewById<TextView>(R.id.tvIDDetail)
        val ivQRCode = dialogView.findViewById<ImageView>(R.id.ivQRCode)
        val qrSection = dialogView.findViewById<View>(R.id.qrSection)
        val btnCloseModal = dialogView.findViewById<ImageView>(R.id.btnCloseModal)
        val btnCancelVisit = dialogView.findViewById<TextView>(R.id.btnCancelVisit)
        val btnEdit = dialogView.findViewById<TextView>(R.id.btnEdit)
        val btnGenerateQR = dialogView.findViewById<TextView>(R.id.btnGenerateQR)
        val btnSendQrEmail = dialogView.findViewById<TextView>(R.id.btnSendQrEmail)

        // Populate fields
        tvCondoName.text = visit.condoName ?: "Unknown"
        tvUnitNumber.text = visit.unitNumber ?: "N/A"
        tvVisitDate.text = formatDateForDetails(visit.visitDate)
        tvPurpose.text = visit.purpose ?: "N/A"
        updateQrSection(visit.qrImageUrl, qrSection, ivQRCode, btnGenerateQR)

        // Fetch and display files
        lifecycleScope.launch {
            loadVisitFiles(visit.id, tvIDDetail)
        }

        // Close button
        btnCloseModal.setOnClickListener {
            dialog.dismiss()
        }

        // Cancel Visit button
        btnCancelVisit.setOnClickListener {
            confirmCancelVisit(visit.id, dialog)
        }

        // Edit button
        btnEdit.setOnClickListener {
            showEditVisitModal(visit, dialog)
        }

        // Generate QR button
        btnGenerateQR.setOnClickListener {
            lifecycleScope.launch {
                generateQrForVisit(visit.id, qrSection, ivQRCode, btnGenerateQR)
            }
        }

        btnSendQrEmail.setOnClickListener {
            lifecycleScope.launch {
                sendQrEmail(visit.id)
            }
        }

        dialog.show()
        configureDetailsDialogWindow(dialog)
    }

    private fun confirmCancelVisit(visitId: String, detailsDialog: AlertDialog) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Visit")
            .setMessage("Are you sure you want to cancel this visit?")
            .setNegativeButton("No", null)
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    cancelVisit(visitId, detailsDialog)
                }
            }
            .show()
    }

    private suspend fun cancelVisit(visitId: String, detailsDialog: AlertDialog) {
        try {
            val token = sessionManager.fetchAuthToken()
            if (token.isNullOrBlank()) {
                Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
                return
            }

            val response = RetrofitClient.visitService.cancelVisit("Bearer $token", visitId)
            if (response.isSuccessful) {
                Toast.makeText(this, "Visit cancelled", Toast.LENGTH_SHORT).show()
                detailsDialog.dismiss()
                loadVisitsData()
            } else {
                val error = response.errorBody()?.string().orEmpty()
                Toast.makeText(this, "Failed to cancel visit", Toast.LENGTH_SHORT).show()
                System.err.println("[MyVisits] Cancel visit failed: $error")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error cancelling visit: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditVisitModal(visit: VisitSummary, detailsDialog: AlertDialog) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_visit, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val etUnitNumber = dialogView.findViewById<EditText>(R.id.etEditUnitNumber)
        val etVisitDate = dialogView.findViewById<EditText>(R.id.etEditVisitDate)
        val etPurpose = dialogView.findViewById<EditText>(R.id.etEditPurpose)
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnCloseEditVisit)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveEditVisit)

        etUnitNumber.setText(visit.unitNumber)
        etVisitDate.setText(formatDateForDetails(visit.visitDate))
        etPurpose.setText(visit.purpose.orEmpty())

        btnClose.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val unitNumber = etUnitNumber.text.toString().trim()
            val visitDate = etVisitDate.text.toString().trim()
            val purpose = etPurpose.text.toString().trim()

            if (unitNumber.isBlank() || visitDate.isBlank() || purpose.isBlank()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                updateVisit(visit.id, unitNumber, visitDate, purpose, dialog, detailsDialog)
            }
        }

        dialog.show()
        configureDetailsDialogWindow(dialog)
    }

    private suspend fun updateVisit(
        visitId: String,
        unitNumber: String,
        visitDate: String,
        purpose: String,
        editDialog: AlertDialog,
        detailsDialog: AlertDialog
    ) {
        try {
            val token = sessionManager.fetchAuthToken()
            if (token.isNullOrBlank()) {
                Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
                return
            }

            val response = RetrofitClient.visitService.updateVisit(
                "Bearer $token",
                visitId,
                UpdateVisitRequest(
                    unitNumber = unitNumber,
                    visitDate = visitDate,
                    purpose = purpose
                )
            )

            if (response.isSuccessful) {
                Toast.makeText(this, "Visit updated", Toast.LENGTH_SHORT).show()
                editDialog.dismiss()
                detailsDialog.dismiss()
                loadVisitsData()
            } else {
                val error = response.errorBody()?.string().orEmpty()
                Toast.makeText(this, "Failed to update visit", Toast.LENGTH_SHORT).show()
                System.err.println("[MyVisits] Update visit failed: $error")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error updating visit: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureDetailsDialogWindow(dialog: AlertDialog) {
        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            window.setGravity(Gravity.BOTTOM)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(0.22f)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.attributes = window.attributes.apply {
                    blurBehindRadius = 24
                    flags = flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                }
            }
        }
    }

    private fun updateQrSection(qrImageUrl: String?, qrSection: View, ivQRCode: ImageView, btnGenerateQR: TextView) {
        if (qrImageUrl.isNullOrBlank()) {
            qrSection.visibility = View.GONE
            btnGenerateQR.visibility = View.VISIBLE
            return
        }

        qrSection.visibility = View.VISIBLE
        btnGenerateQR.visibility = View.GONE
        com.bumptech.glide.Glide.with(this)
            .load(qrImageUrl)
            .fitCenter()
            .into(ivQRCode)
    }

    private suspend fun generateQrForVisit(visitId: String, qrSection: View, ivQRCode: ImageView, btnGenerateQR: TextView) {
        try {
            val token = sessionManager.fetchAuthToken()
            if (token.isNullOrBlank()) {
                Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
                return
            }

            val response = RetrofitClient.visitService.generateVisitQr("Bearer $token", visitId)
            if (response.isSuccessful) {
                updateQrSection(response.body()?.qrImageUrl, qrSection, ivQRCode, btnGenerateQR)
            } else {
                Toast.makeText(this, "Failed to generate QR", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error generating QR: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun sendQrEmail(visitId: String) {
        try {
            val token = sessionManager.fetchAuthToken()
            if (token.isNullOrBlank()) {
                Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
                return
            }

            val response = RetrofitClient.visitService.sendVisitQrEmail("Bearer $token", visitId)
            if (response.isSuccessful) {
                Toast.makeText(this, "QR sent to email", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to send QR email", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error sending email: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun loadVisitFiles(visitId: String, tvIDDetail: TextView) {
        try {
            val token = sessionManager.fetchAuthToken()
            if (token.isNullOrBlank()) {
                Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
                return
            }

            val bearer = "Bearer $token"
            val response = RetrofitClient.visitService.getVisitFiles(visitId, bearer)

            if (response.isSuccessful) {
                val files = response.body() ?: emptyList()
                if (files.isNotEmpty()) {
                    val firstFile = files[0]
                    tvIDDetail.text = firstFile.fileName.ifBlank { "ID file" }
                } else {
                    tvIDDetail.text = "No ID file uploaded"
                }
            } else {
                println("Failed to fetch files: ${response.code()}")
            }
        } catch (e: Exception) {
            println("Error loading files: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun formatDateForDetails(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "N/A"
        return dateString.take(10)
    }

    private fun formatDateForDisplay(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "N/A"
        return try {
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd"
            )
            val parsed = inputFormats.firstNotNullOfOrNull { pattern ->
                runCatching { SimpleDateFormat(pattern, Locale.getDefault()).parse(dateString) }.getOrNull()
            } ?: return dateString

            SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(parsed)
        } catch (_: Exception) {
            dateString
        }
    }
}
