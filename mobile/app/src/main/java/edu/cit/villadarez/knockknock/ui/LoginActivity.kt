package edu.cit.villadarez.knockknock.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.cit.villadarez.knockknock.databinding.ActivityLoginBinding
import edu.cit.villadarez.knockknock.api.RetrofitClient
import edu.cit.villadarez.knockknock.models.LoginRequest
import edu.cit.villadarez.knockknock.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // WIRING: Redirect to Register Screen
        binding.tvSignUpToggle.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnSignIn.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val request = LoginRequest(email, password)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.login(request)
                if (response.isSuccessful && response.body() != null) {
                    sessionManager.saveAuthToken(response.body()!!.token)
                    Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()

                    // Navigate to Visitor Dashboard
                    startActivity(Intent(this@LoginActivity, VisitorDashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
