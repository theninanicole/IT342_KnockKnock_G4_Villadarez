package edu.villadarez.knockknock.features.visit

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import edu.villadarez.knockknock.R
import edu.villadarez.knockknock.core.network.RetrofitClient
import edu.villadarez.knockknock.databinding.ActivityAccountBinding
import edu.villadarez.knockknock.features.auth.ChangePasswordRequest
import edu.villadarez.knockknock.features.auth.LoginActivity
import edu.villadarez.knockknock.features.auth.UpdateProfileRequest
import edu.villadarez.knockknock.features.auth.User
import edu.villadarez.knockknock.shared.session.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding
    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupBottomNav()
        setupActions()
        setEditing(false)
        loadProfile()
    }

    override fun onResume() {
        super.onResume()
        NotificationBadgeHelper.refresh(lifecycleScope, sessionManager, binding.badgeNotificationCount)
    }

    private fun setupActions() {
        binding.btnEditProfile.setOnClickListener { setEditing(true) }
        binding.btnCancelEdit.setOnClickListener { cancelEdit() }
        binding.btnSaveProfile.setOnClickListener { saveProfile() }
        binding.btnUpdatePassword.setOnClickListener { changePassword() }
    }

    private fun loadProfile() {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            goToLogin()
            return
        }

        binding.progressAccount.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCurrentUser("Bearer $token")
                if (response.isSuccessful) {
                    val user = response.body()?.user
                    if (user != null) {
                        currentUser = user
                        bindProfile(user)
                    } else {
                        Toast.makeText(this@AccountActivity, "Unable to load profile", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AccountActivity, "Unable to load profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AccountActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressAccount.visibility = View.GONE
            }
        }
    }

    private fun bindProfile(user: User) {
        binding.tvInitials.text = getInitials(user.fullName)
        binding.etFullName.setText(user.fullName)
        binding.etContactNumber.setText(user.contactNumber.orEmpty())
        binding.tvEmail.text = user.email
        binding.tvJoinedDate.text = formatJoinedDate(user.createdAt)
        binding.passwordCard.visibility = if (user.authProvider.equals("google", ignoreCase = true)) View.GONE else View.VISIBLE
        setEditing(false)
    }

    private fun setEditing(editing: Boolean) {
        isEditing = editing
        binding.btnEditProfile.visibility = if (editing) View.GONE else View.VISIBLE
        binding.editActions.visibility = if (editing) View.VISIBLE else View.GONE
        setEditable(binding.etFullName, editing)
        setEditable(binding.etContactNumber, editing)
        if (editing) {
            binding.etFullName.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etFullName, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setEditable(field: EditText, editable: Boolean) {
        field.isFocusable = editable
        field.isFocusableInTouchMode = editable
        field.isCursorVisible = editable
        field.isLongClickable = editable
    }

    private fun cancelEdit() {
        currentUser?.let { bindProfile(it) }
        setEditing(false)
    }

    private fun saveProfile() {
        val token = sessionManager.fetchAuthToken() ?: return
        val fullName = binding.etFullName.text.toString().trim()
        val contactNumber = binding.etContactNumber.text.toString().trim()

        if (fullName.isBlank() || contactNumber.isBlank()) {
            Toast.makeText(this, "Please complete your profile details", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSaveProfile.isEnabled = false
        binding.btnSaveProfile.text = "Saving..."
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.updateProfile(
                    "Bearer $token",
                    UpdateProfileRequest(fullName, contactNumber)
                )
                if (response.isSuccessful) {
                    currentUser = currentUser?.copy(fullName = fullName, contactNumber = contactNumber)
                    binding.tvInitials.text = getInitials(fullName)
                    setEditing(false)
                    Toast.makeText(this@AccountActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AccountActivity, "Please enter a valid contact number", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AccountActivity, "We couldn't update your profile", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnSaveProfile.isEnabled = true
                binding.btnSaveProfile.text = "Save"
            }
        }
    }

    private fun changePassword() {
        val token = sessionManager.fetchAuthToken() ?: return
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, "Please complete all password fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 8) {
            Toast.makeText(this, "New password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnUpdatePassword.isEnabled = false
        binding.btnUpdatePassword.text = "Updating..."
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.changePassword(
                    "Bearer $token",
                    ChangePasswordRequest(currentPassword, newPassword, confirmPassword)
                )
                if (response.isSuccessful) {
                    clearPasswordFields()
                    Toast.makeText(this@AccountActivity, "Password changed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AccountActivity, "Your current password is incorrect", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AccountActivity, "We couldn't change your password", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnUpdatePassword.isEnabled = true
                binding.btnUpdatePassword.text = "Update Password"
            }
        }
    }

    private fun clearPasswordFields() {
        binding.etCurrentPassword.text?.clear()
        binding.etNewPassword.text?.clear()
        binding.etConfirmPassword.text?.clear()
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

        setActiveTab(binding.tabAccount)

        binding.tabHome.setOnClickListener {
            setActiveTab(binding.tabHome)
            startActivity(Intent(this, VisitorDashboardActivity::class.java))
            finish()
        }

        binding.tabMyVisits.setOnClickListener {
            setActiveTab(binding.tabMyVisits)
            startActivity(Intent(this, MyVisitsActivity::class.java))
            finish()
        }

        binding.tabNotifications.setOnClickListener {
            setActiveTab(binding.tabNotifications)
            startActivity(Intent(this, NotificationsActivity::class.java))
            finish()
        }

        binding.tabAccount.setOnClickListener {
            setActiveTab(binding.tabAccount)
        }

        binding.tabLogout.setOnClickListener {
            setActiveTab(binding.tabLogout)
            sessionManager.saveAuthToken("")
            goToLogin()
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun getInitials(name: String): String {
        return name
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "U" }
    }

    private fun formatJoinedDate(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
        )

        for (pattern in patterns) {
            runCatching {
                val parser = SimpleDateFormat(pattern, Locale.US)
                parser.timeZone = TimeZone.getDefault()
                val date = parser.parse(value)
                if (date != null) {
                    return SimpleDateFormat("MMMM d, yyyy", Locale.US).format(date)
                }
            }
        }

        return value.substringBefore("T")
    }
}
