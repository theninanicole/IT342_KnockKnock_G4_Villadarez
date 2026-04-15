package edu.cit.villadarez.knockknock.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.cit.villadarez.knockknock.R
import edu.cit.villadarez.knockknock.api.RetrofitClient
import edu.cit.villadarez.knockknock.databinding.ActivityRegisterBinding
import edu.cit.villadarez.knockknock.models.RegisterVisitorRequest
import edu.cit.villadarez.knockknock.models.RegisterCondoAdminRequest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Navigation back to Login
        binding.tvSignIn.setOnClickListener {
            finish()
        }

        // 2. Toggle Admin/Visitor UI
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnVisitor -> {
                        binding.tvSubtitle.text = "Register as a visitor"
                        binding.layoutCondoDetails.visibility = View.GONE
                    }
                    R.id.btnAdmin -> {
                        binding.tvSubtitle.text = "Register as a condominium administrator"
                        binding.layoutCondoDetails.visibility = View.VISIBLE
                    }
                }
            }
        }

        // 3. Register Button Click
        binding.btnCreateAccount.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val contactNumber = binding.etContactNumber.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Determine Role based on toggle group
        val isItemAdmin = binding.toggleGroup.checkedButtonId == R.id.btnAdmin

        // General Validation
        if (fullName.isEmpty() || email.isEmpty() || contactNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // API Call based on role
        lifecycleScope.launch {
            try {
                val response = if (isItemAdmin) {
                    // Admin specific validation
                    val condoName = binding.etCondoName.text.toString().trim()
                    val condoAddress = binding.etCondoAddress.text.toString().trim()

                    if (condoName.isEmpty() || condoAddress.isEmpty()) {
                        Toast.makeText(this@RegisterActivity, "Please fill in condominium details", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val adminRequest = RegisterCondoAdminRequest(
                        fullName,
                        email,
                        contactNumber,
                        password,
                        confirmPassword,
                        condoName,
                        condoAddress
                    )
                    RetrofitClient.instance.registerCondoAdmin(adminRequest)
                } else {
                    val visitorRequest = RegisterVisitorRequest(
                        fullName,
                        email,
                        contactNumber,
                        password,
                        confirmPassword
                    )
                    RetrofitClient.instance.registerVisitor(visitorRequest)
                }

                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Account Created Successfully!", Toast.LENGTH_LONG).show()

                    // Navigate to Login screen
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = when (response.code()) {
                        409 -> "Email already exists"
                        else -> "Registration Failed: ${response.code()}"
                    }
                    Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Connection Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
