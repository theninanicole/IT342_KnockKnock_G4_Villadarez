package edu.villadarez.knockknock.features.visit

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import okhttp3.RequestBody
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
    private var layoutUploadPromptRef: View? = null
    private var layoutSelectedFileRef: View? = null
    private var selectedCondoId: String? = null
    private val FILE_PICKER_REQUEST = 1001
    private val storageHttpClient = OkHttpClient()

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
            // TODO: navigate to notifications screen
        }

        binding.tabAccount.setOnClickListener {
            setActiveTab(binding.tabAccount)
            // TODO: navigate to account/profile screen
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
        val layoutUploadPrompt = dialogView.findViewById<View>(R.id.layoutUploadPrompt)
        val layoutSelectedFile = dialogView.findViewById<View>(R.id.layoutSelectedFile)
        val btnCloseNewVisit = dialogView.findViewById<ImageView>(R.id.btnCloseNewVisit)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

        // Store reference for onActivityResult
        tvSelectedFileNameRef = tvSelectedFileName
        layoutUploadPromptRef = layoutUploadPrompt
        layoutSelectedFileRef = layoutSelectedFile

        // Reset file selection
        selectedFileUri = null
        selectedFileName = "No file selected"
        updateSelectedFileUi()

        // Close button
        btnCloseNewVisit.setOnClickListener {
            dialog.dismiss()
            tvSelectedFileNameRef = null
            layoutUploadPromptRef = null
            layoutSelectedFileRef = null
        }

        // Upload ID file
        btnUploadID.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
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

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        }
        dialog.show()

        // Fetch and populate condominiums after the sheet is visible.
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
                val month = parts[0].toInt()
                val day = parts[1].toInt()
                val year = parts[2].toInt()
                String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
                return
            }

            var fileName = selectedFileName
            if (fileName.isBlank() || fileName == "No file selected") {
                Toast.makeText(this, "Please upload your ID file", Toast.LENGTH_SHORT).show()
                return
            }

            System.out.println("[VisitorDashboard] Creating visit")

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
                Toast.makeText(this, "Failed to get visit ID", Toast.LENGTH_SHORT).show()
                return
            }

            System.out.println("[VisitorDashboard] Visit created with ID: $visitId")
            uploadFileForVisit(fileUri, visitId)

            Toast.makeText(this, "Visit created successfully", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            
            // Refresh visits list
            lifecycleScope.launch {
                loadDashboardData()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating visit: ${e.message ?: e.javaClass.simpleName}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private suspend fun uploadFileForVisit(fileUri: Uri, visitId: String) {
        try {
            val token = sessionManager.fetchAuthToken()
            if (token.isNullOrBlank()) {
                System.err.println("[VisitorDashboard] Not authenticated for file upload")
                return
            }

            val bearer = "Bearer $token"
            val originalFileName = selectedFileName
            var fileName = originalFileName
            
            if (fileName.isBlank() || fileName == "No file selected") {
                System.err.println("[VisitorDashboard] Invalid file name")
                return
            }
            
            fileName = "${System.currentTimeMillis()}_${sanitizeStorageFileName(fileName)}"
            val filePath = "visitors_id/$visitId/$fileName"
            val fileType = contentResolver.getType(fileUri) ?: "application/octet-stream"

            System.out.println("[VisitorDashboard] Starting Supabase upload for visit $visitId - path: $filePath")

            val publicUrl = withContext(Dispatchers.IO) {
                val fileBytes = try {
                    contentResolver.openInputStream(fileUri)?.use { input ->
                        input.readBytes()
                    } ?: throw IllegalStateException("Could not open file")
                } catch (e: Exception) {
                    throw IllegalStateException("Unable to read selected ID file", e)
                }

                if (fileBytes.isEmpty()) {
                    throw IllegalStateException("Selected ID file is empty")
                }

                System.out.println("[VisitorDashboard] File bytes read: ${fileBytes.size} bytes")

                val supabaseUrl = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
                val supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim()
                val bucket = BuildConfig.SUPABASE_STORAGE_BUCKET.ifBlank { "kk_files" }
                if (supabaseUrl.isBlank() || supabaseKey.isBlank()) {
                    throw IllegalStateException("Supabase storage is not configured")
                }

                val uploadUrl = "$supabaseUrl/storage/v1/object/$bucket/$filePath"
                val requestBody = RequestBody.create(fileType.toMediaType(), fileBytes)
                val request = Request.Builder()
                    .url(uploadUrl)
                    .header("apikey", supabaseKey)
                    .header("Content-Type", fileType)
                    .header("Cache-Control", "3600")
                    .header("x-upsert", "false")
                    .post(requestBody)
                    .build()

                storageHttpClient.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()
                    System.out.println("[VisitorDashboard] Supabase upload response: ${response.code}")
                    if (!response.isSuccessful) {
                        System.err.println("[VisitorDashboard] Supabase upload failed: ${response.code} - $responseBody")
                        throw IllegalStateException("Supabase upload failed: ${response.code}")
                    }
                }

                "$supabaseUrl/storage/v1/object/public/$bucket/$filePath"
            }

            val metadataResponse = RetrofitClient.visitService.saveVisitFileMetadata(
                bearer,
                visitId,
                FileUploadRequest(
                    visitId = visitId,
                    filePath = filePath,
                    fileUrl = publicUrl,
                    fileName = originalFileName,
                    fileType = fileType
                )
            )

            if (metadataResponse.isSuccessful) {
                System.out.println("[VisitorDashboard] File metadata saved successfully")
            } else {
                val errorBody = metadataResponse.errorBody()?.string() ?: "Unknown error"
                System.err.println("[VisitorDashboard] Metadata save failed: ${metadataResponse.code()} - $errorBody")
                throw IllegalStateException("File uploaded, but metadata save failed")
            }
        } catch (e: Exception) {
            System.err.println("[VisitorDashboard] Error uploading file: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun sanitizeStorageFileName(name: String): String {
        val justName = name.substringAfterLast('/').substringAfterLast('\\')
        val dotIndex = justName.lastIndexOf('.')
        val base = if (dotIndex > 0) justName.substring(0, dotIndex) else justName
        val ext = if (dotIndex > 0) justName.substring(dotIndex) else ""
        val safeBase = base.replace("[^a-zA-Z0-9-_]".toRegex(), "_")
        return (safeBase + ext).ifBlank { "file" }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.data
            selectedFileName = getFileName(selectedFileUri) ?: "Unknown file"
            updateSelectedFileUi()
        }
    }

    private fun updateSelectedFileUi() {
        val hasFile = selectedFileUri != null && selectedFileName != "No file selected"
        layoutUploadPromptRef?.visibility = if (hasFile) View.GONE else View.VISIBLE
        layoutSelectedFileRef?.visibility = if (hasFile) View.VISIBLE else View.GONE
        tvSelectedFileNameRef?.text = if (hasFile) selectedFileName else ""
    }

    private fun getFileName(uri: Uri?): String? {
        if (uri == null) return null
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow("_display_name"))
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
                    val visits = visitsResponse.body()?.visits ?: emptyList()
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

    private fun updateVisits(visits: List<VisitSummary>) {
        val upcomingVisits = visits
            .filter { isUpcomingDashboardVisit(it) }
            .sortedBy { visitDateKey(it.visitDate).orEmpty() }

        if (upcomingVisits.isEmpty()) {
            binding.tvEmptyVisits.visibility = View.VISIBLE
        } else {
            binding.tvEmptyVisits.visibility = View.GONE
        }
        visitAdapter.updateData(upcomingVisits)
    }

    private fun isUpcomingDashboardVisit(visit: VisitSummary): Boolean {
        val status = visit.status.trim().uppercase(Locale.US).replace("_", "-")
        if (status != "SCHEDULED" && status != "CHECKED-IN") return false

        val visitKey = visitDateKey(visit.visitDate) ?: return false
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        return visitKey >= todayKey
    }

    private fun visitDateKey(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val datePart = value.substringBefore("T").trim()
        if (Regex("\\d{4}-\\d{2}-\\d{2}").matches(datePart)) return datePart

        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "MMMM d, yyyy",
            "MM/dd/yyyy"
        )
        val parsed = formats.firstNotNullOfOrNull { pattern ->
            runCatching { SimpleDateFormat(pattern, Locale.US).parse(value) }.getOrNull()
        } ?: return null

        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(parsed)
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
        val btnCloseModal = dialogView.findViewById<ImageView>(R.id.btnCloseModal)
        val btnCancelVisit = dialogView.findViewById<TextView>(R.id.btnCancelVisit)
        val btnEdit = dialogView.findViewById<TextView>(R.id.btnEdit)
        val btnGenerateQR = dialogView.findViewById<TextView>(R.id.btnGenerateQR)

        // Populate fields
        tvCondoName.text = visit.condoName ?: "Unknown"
        tvUnitNumber.text = visit.unitNumber ?: "N/A"
        tvVisitDate.text = formatDateForDisplay(visit.visitDate)
        tvPurpose.text = visit.purpose ?: "N/A"

        // Fetch and display files
        lifecycleScope.launch {
            loadVisitFiles(visit.id, ivQRCode, tvIDDetail)
        }

        // Close button
        btnCloseModal.setOnClickListener {
            dialog.dismiss()
        }

        // Cancel Visit button
        btnCancelVisit.setOnClickListener {
            Toast.makeText(this, "Cancel visit feature coming soon", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // Edit button
        btnEdit.setOnClickListener {
            Toast.makeText(this, "Edit visit feature coming soon", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // Generate QR button
        btnGenerateQR.setOnClickListener {
            Toast.makeText(this, "Generate QR feature coming soon", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
        configureDetailsDialogWindow(dialog)
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

        val sheet = dialog.findViewById<View>(R.id.visitDetailsSheet)
        val scroll = dialog.findViewById<View>(R.id.detailsScroll)
        sheet?.post {
            val maxSheetHeight = (resources.displayMetrics.heightPixels * 0.86f).toInt()
            val overflow = sheet.height - maxSheetHeight
            if (overflow > 0 && scroll != null) {
                sheet.layoutParams = sheet.layoutParams.apply {
                    height = maxSheetHeight
                }
                scroll.layoutParams = scroll.layoutParams.apply {
                    height = (scroll.height - overflow).coerceAtLeast(dpToPx(180))
                }
            }
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private suspend fun loadVisitFiles(visitId: String, ivQRCode: ImageView, tvIDDetail: TextView) {
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
                    // Load the file image using Glide
                    com.bumptech.glide.Glide.with(this)
                        .load(firstFile.fileUrl)
                        .centerCrop()
                        .into(ivQRCode)
                    // Display file name from database
                    tvIDDetail.text = firstFile.fileName
                    ivQRCode.visibility = android.view.View.VISIBLE
                }
            } else {
                println("Failed to fetch files: ${response.code()}")
            }
        } catch (e: Exception) {
            println("Error loading files: ${e.message}")
            e.printStackTrace()
        }
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
