package edu.villadarez.knockknock.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.villadarez.knockknock.R
import edu.villadarez.knockknock.api.RetrofitClient
import edu.villadarez.knockknock.databinding.ActivityVisitorDashboardBinding
import edu.villadarez.knockknock.models.VisitSummary
import edu.villadarez.knockknock.utils.SessionManager
import kotlinx.coroutines.launch

class VisitorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVisitorDashboardBinding
    private lateinit var sessionManager: SessionManager
    private val visitAdapter = VisitAdapter(emptyList())

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

        // Default selection is Home
        setActiveTab(binding.tabHome)

        binding.tabHome.setOnClickListener {
            setActiveTab(binding.tabHome)
            // TODO: stay on home dashboard
        }

        binding.tabMyVisits.setOnClickListener {
            setActiveTab(binding.tabMyVisits)
            // TODO: navigate to full My Visits screen
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
            Toast.makeText(this, "New Visit form coming soon", Toast.LENGTH_SHORT).show()
        }
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
        if (visits.isEmpty()) {
            binding.tvEmptyVisits.visibility = View.VISIBLE
        } else {
            binding.tvEmptyVisits.visibility = View.GONE
        }
        visitAdapter.updateData(visits)
    }
}
