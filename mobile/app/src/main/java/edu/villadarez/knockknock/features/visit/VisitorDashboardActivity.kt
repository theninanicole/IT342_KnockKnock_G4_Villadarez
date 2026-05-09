package edu.villadarez.knockknock.features.visit

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.villadarez.knockknock.BuildConfig
import edu.villadarez.knockknock.R
import edu.villadarez.knockknock.core.network.RetrofitClient
import edu.villadarez.knockknock.databinding.ActivityVisitorDashboardBinding
import edu.villadarez.knockknock.features.auth.LoginActivity
import edu.villadarez.knockknock.shared.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VisitorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVisitorDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var visitAdapter: VisitAdapter
    
    // For new visit modal
    private var selectedFileUri: Uri? = null
    private var selectedFileName: String = "No file selected"
    private var tvSelectedFileNameRef: TextView? = null
    private var selectedCondoId: String? = null
    private val supabaseHttpClient = OkHttpClient()
    private val FILE_PICKER_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupRecyclerView()
        setupBottomNav()
        setupNewVisitButton()

        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        NotificationBadgeHelper.refresh(lifecycleScope, sessionManager, binding.badgeNotificationCount)
    }

    private fun setupRecyclerView() {
        visitAdapter = VisitAdapter(emptyList()) { visit ->
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

                // Also tint the top icon to match the state
                val drawables = tab.compoundDrawablesRelative
                drawables.forEachIndexed { index, d ->
                    d?.mutate()?.setTint(color)
                }
                tab.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    drawables[0], drawables[1], drawables[2], drawables[3]
                )
            }
        }

        // Default selection is Home because this is the dashboard screen.
        setActiveTab(binding.tabHome)

        binding.tabHome.setOnClickListener {
            setActiveTab(binding.tabHome)
            // Already on the dashboard.
        }

        binding.tabMyVisits.setOnClickListener {
            setActiveTab(binding.tabMyVisits)
            startActivity(Intent(this, MyVisitsActivity::class.java))
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

    private fun setupNewVisitButton() {
        binding.btnNewVisit.setOnClickListener {
            lifecycleScope.launch {
                showNewVisitModal()
            }
        }
    }

    private suspend fun showNewVisitModal() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_new_visit, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Get references to form fields
        val spinnerCondo = dialogView.findViewById<Spinner>(R.id.spinnerCondo)
        val etUnitNumber = dialogView.findViewById<EditText>(R.id.etUnitNumber)
        val etVisitDate = dialogView.findViewById<EditText>(R.id.etVisitDate)
        val etPurpose = dialogView.findViewById<EditText>(R.id.etPurpose)
        val btnUploadID = dialogView.findViewById<View>(R.id.btnUploadID)
        val tvSelectedFileName = dialogView.findViewById<TextView>(R.id.tvSelectedFileName)
        val btnCloseNewVisit = dialogView.findViewById<ImageView>(R.id.btnCloseNewVisit)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

        // Store reference for onActivityResult
        tvSelectedFileNameRef = tvSelectedFileName

        // Reset file selection
        selectedFileUri = null
        selectedFileName = "No file selected"
        tvSelectedFileName.text = selectedFileName

        // Fetch and populate condominiums
        val token = sessionManager.fetchAuthToken()
        if (!token.isNullOrBlank()) {
            val bearer = "Bearer $token"
            try {
                val response = RetrofitClient.visitService.getCondominiums(bearer)
                if (response.isSuccessful) {
                    val condos = response.body() ?: emptyList()
                    val condoNames = condos.map { it.name }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, condoNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCondo.adapter = adapter

                    // Set spinner selection listener
                    spinnerCondo.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                            selectedCondoId = condos[position].condoId
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                            selectedCondoId = null
                        }
                    }

                    // Set initial selection
                    if (condos.isNotEmpty()) {
                        spinnerCondo.setSelection(0)
                        selectedCondoId = condos[0].condoId
                    }
                } else {
                    Toast.makeText(this, "Failed to load condominiums", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error loading condominiums: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Close button
        btnCloseNewVisit.setOnClickListener {
            dialog.dismiss()
        }

        // Upload ID file
        btnUploadID.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "application/pdf"))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            startActivityForResult(intent, FILE_PICKER_REQUEST)
        }

        // Submit button
        btnSubmit.setOnClickListener {
            val unitNumber = etUnitNumber.text.toString().trim()
            val visitDate = etVisitDate.text.toString().trim()
            val purpose = etPurpose.text.toString().trim()

            if (unitNumber.isBlank()) {
                Toast.makeText(this, "Please enter unit number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (visitDate.isBlank()) {
                Toast.makeText(this, "Please enter visit date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (purpose.isBlank()) {
                Toast.makeText(this, "Please enter purpose of visit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedCondoId.isNullOrBlank()) {
                Toast.makeText(this, "Please select a condominium", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val fileUri = selectedFileUri
            if (fileUri == null) {
                Toast.makeText(this, "Please upload your ID file", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create visit
            lifecycleScope.launch {
                createVisit(unitNumber, visitDate, purpose, fileUri, dialog)
            }
        }

        dialog.show()
        configureNewVisitDialogWindow(dialog)
    }

    private fun configureNewVisitDialogWindow(dialog: AlertDialog) {
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

    private suspend fun createVisit(
        unitNumber: String,
        visitDate: String,
        purpose: String,
        fileUri: Uri,
        dialog: AlertDialog
    ) {
        try {
            val token = sessionManager.fetchAuthToken()
            if (token.isNullOrBlank()) {
                Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
                return
            }

            val bearer = "Bearer $token"

            // Use the selected condominium ID from the spinner
            val condoId = selectedCondoId ?: run {
                Toast.makeText(this, "Please select a condominium", Toast.LENGTH_SHORT).show()
                return
            }

            // Convert date from MM/DD/YYYY to YYYY-MM-DD format
            val formattedDate = try {
                val parts = visitDate.split("/")
                if (parts.size != 3) {
                    Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
                    return
                }
                val month = parts[0].padStart(2, '0')
                val day = parts[1].padStart(2, '0')
                "${parts[2]}-$month-$day"
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
                return
            }

            val parsedVisitDate = parseVisitDate(formattedDate)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            if (parsedVisitDate == null || parsedVisitDate.before(today)) {
                Toast.makeText(this, "Visit date must be today or later", Toast.LENGTH_SHORT).show()
                return
            }

            System.out.println("[VisitorDashboard] Creating visit before Supabase file upload")
            
            val response = RetrofitClient.visitService.createVisitWithoutFile(
                bearer,
                condoId,
                unitNumber,
                formattedDate,
                purpose
            )

            System.out.println("[VisitorDashboard] API Response: ${response.code()}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                System.err.println("[VisitorDashboard] Error response: $errorBody")
                Toast.makeText(this, "Failed to create visit: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                return
            }

            val visitId = response.body()?.visitId
            if (visitId.isNullOrBlank()) {
                Toast.makeText(this, "Visit created but no visit ID was returned", Toast.LENGTH_LONG).show()
                return
            }

            val uploadResult = uploadFileToSupabase(fileUri, visitId)
            val metadataResponse = RetrofitClient.visitService.saveVisitFileMetadata(
                bearer,
                visitId,
                FileUploadRequest(
                    visitId = visitId,
                    filePath = uploadResult.path,
                    fileUrl = uploadResult.publicUrl,
                    fileName = uploadResult.originalFileName,
                    fileType = uploadResult.fileType
                )
            )

            if (!metadataResponse.isSuccessful) {
                val errorBody = metadataResponse.errorBody()?.string() ?: "Unknown error"
                System.err.println("[VisitorDashboard] File metadata error response: $errorBody")
                Toast.makeText(this, "File uploaded, but saving file details failed", Toast.LENGTH_LONG).show()
                return
            }

            Toast.makeText(this, "Visit created successfully", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            
            // Refresh visits list
            lifecycleScope.launch {
                loadDashboardData()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating visit: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private suspend fun uploadFileToSupabase(fileUri: Uri, visitId: String): SupabaseUploadResult {
        val supabaseUrl = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
        val supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim()
        val bucketName = BuildConfig.SUPABASE_STORAGE_BUCKET.ifBlank { "kk_files" }

        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) {
            throw IllegalStateException("Supabase mobile config is missing")
        }

        val mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
        val fileBytes = withContext(Dispatchers.IO) {
            contentResolver.openInputStream(fileUri)?.use { input ->
                input.readBytes()
            } ?: throw IllegalStateException("Could not open selected file")
        }

        if (fileBytes.isEmpty()) {
            throw IllegalStateException("Selected file is empty")
        }

        val safeName = sanitizeFileName(selectedFileName)
        val fileName = "${System.currentTimeMillis()}_$safeName"
        val filePath = "visitors_id/$visitId/$fileName"
        val requestBody = fileBytes.toRequestBody(mimeType.toMediaType())
        val uploadUrl = "$supabaseUrl/storage/v1/object/$bucketName/$filePath"

        System.out.println("[VisitorDashboard] Uploading file directly to Supabase: $filePath")

        val request = Request.Builder()
            .url(uploadUrl)
            .header("apikey", supabaseKey)
            .header("Authorization", "Bearer $supabaseKey")
            .header("Content-Type", mimeType)
            .header("Cache-Control", "3600")
            .header("x-upsert", "false")
            .post(requestBody)
            .build()

        withContext(Dispatchers.IO) {
            supabaseHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                System.out.println("[VisitorDashboard] Supabase upload response: ${response.code}")

                if (!response.isSuccessful) {
                    System.err.println("[VisitorDashboard] Supabase upload error: $responseBody")
                    throw IllegalStateException("Supabase upload failed: ${response.code} - $responseBody")
                }
            }
        }

        return SupabaseUploadResult(
            path = filePath,
            publicUrl = "$supabaseUrl/storage/v1/object/public/$bucketName/$filePath",
            originalFileName = selectedFileName,
            fileType = mimeType
        )
    }

    private fun sanitizeFileName(fileName: String): String {
        val fallbackName = "visitor_id"
        return fileName
            .takeIf { it.isNotBlank() && it != "No file selected" }
            ?.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            ?.takeIf { it.isNotBlank() }
            ?: fallbackName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.data ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                runCatching {
                    contentResolver.takePersistableUriPermission(
                        selectedFileUri!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
            selectedFileName = getFileName(selectedFileUri) ?: "Unknown file"
            tvSelectedFileNameRef?.text = selectedFileName
        }
    }

    private fun getFileName(uri: Uri?): String? {
        if (uri == null) return null
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null).use {
                if (it != null && it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    private fun loadDashboardData() {
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
                // Load current user
                val meResponse = RetrofitClient.instance.getCurrentUser(bearer)
                if (meResponse.isSuccessful) {
                    val user = meResponse.body()?.user

                    val firstName = user?.fullName?.split(" ")?.firstOrNull() ?: "Visitor"
                    binding.tvWelcomeTitle.text = "Welcome, $firstName!"

                    val initials = user?.fullName
                        ?.split(" ")
                        ?.filter { it.isNotBlank() }
                        ?.take(2)
                        ?.joinToString("") { it.first().uppercase() }

                    binding.tvAvatarInitials.text = initials ?: firstName.firstOrNull()?.uppercase()?.toString() ?: "V"
                }

                // Ensure role is VISITOR
                val role = meResponse.body()?.user?.role
                if (role != null && !role.equals("VISITOR", ignoreCase = true)) {
                    Toast.makeText(this@VisitorDashboardActivity, "This screen is for visitors only", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@VisitorDashboardActivity, LoginActivity::class.java))
                    finish()
                    return@launch
                }

                // Load visits
                val visitsResponse = RetrofitClient.visitService.getMyVisits(bearer)
                if (visitsResponse.isSuccessful) {
                    val visits = visitsResponse.body()?.visits
                        ?.filter { isTodayOrUpcoming(it.visitDate) }
                        ?.sortedBy { parseVisitDate(it.visitDate) }
                        ?: emptyList()
                    updateVisits(visits)
                } else {
                    Toast.makeText(this@VisitorDashboardActivity, "Failed to load visits", Toast.LENGTH_SHORT).show()
                    updateVisits(emptyList())
                }
            } catch (e: Exception) {
                Toast.makeText(this@VisitorDashboardActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                updateVisits(emptyList())
            } finally {
                binding.progressVisits.visibility = View.GONE
            }
        }
    }

    private fun isTodayOrUpcoming(dateString: String?): Boolean {
        val visitDate = parseVisitDate(dateString) ?: return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return !visitDate.before(today)
    }

    private fun parseVisitDate(dateString: String?): java.util.Date? {
        if (dateString.isNullOrBlank()) return null

        val dateOnly = dateString.take(10)
        val inputFormats = listOf(
            "yyyy-MM-dd",
            "MM/dd/yyyy"
        )

        return inputFormats.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.getDefault()).apply {
                    isLenient = false
                }.parse(dateOnly)
            }.getOrNull()
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
        configureNewVisitDialogWindow(dialog)
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
                loadDashboardData()
            } else {
                val error = response.errorBody()?.string().orEmpty()
                Toast.makeText(this, "Failed to cancel visit", Toast.LENGTH_SHORT).show()
                System.err.println("[VisitorDashboard] Cancel visit failed: $error")
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
        configureNewVisitDialogWindow(dialog)
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
                loadDashboardData()
            } else {
                val error = response.errorBody()?.string().orEmpty()
                Toast.makeText(this, "Failed to update visit", Toast.LENGTH_SHORT).show()
                System.err.println("[VisitorDashboard] Update visit failed: $error")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error updating visit: ${e.message}", Toast.LENGTH_SHORT).show()
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

private data class SupabaseUploadResult(
    val path: String,
    val publicUrl: String,
    val originalFileName: String,
    val fileType: String
)
