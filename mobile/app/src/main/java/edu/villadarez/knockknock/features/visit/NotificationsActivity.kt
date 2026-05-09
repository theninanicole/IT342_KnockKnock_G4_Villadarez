package edu.villadarez.knockknock.features.visit

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
import edu.villadarez.knockknock.core.network.RetrofitClient
import edu.villadarez.knockknock.databinding.ActivityNotificationsBinding
import edu.villadarez.knockknock.features.auth.LoginActivity
import edu.villadarez.knockknock.shared.session.SessionManager
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupRecyclerView()
        setupBottomNav()
        loadNotifications()
    }

    override fun onResume() {
        super.onResume()
        NotificationBadgeHelper.refresh(lifecycleScope, sessionManager, binding.badgeNotificationCount)
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList()) { notification ->
            markNotificationAsRead(notification)
        }
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
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

        setActiveTab(binding.tabNotifications)

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
        }

        binding.tabAccount.setOnClickListener {
            setActiveTab(binding.tabAccount)
            startActivity(Intent(this, AccountActivity::class.java))
            finish()
        }

        binding.tabLogout.setOnClickListener {
            setActiveTab(binding.tabLogout)
            sessionManager.saveAuthToken("")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadNotifications() {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding.progressNotifications.visibility = View.VISIBLE
        binding.tvEmptyNotifications.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.visitService.getNotifications("Bearer $token", 50)
                if (response.isSuccessful) {
                    val notifications = response.body()?.notifications.orEmpty()
                    updateNotifications(notifications)
                } else {
                    Toast.makeText(this@NotificationsActivity, "Failed to load notifications", Toast.LENGTH_SHORT).show()
                    updateNotifications(emptyList())
                }
            } catch (e: Exception) {
                Toast.makeText(this@NotificationsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                updateNotifications(emptyList())
            } finally {
                binding.progressNotifications.visibility = View.GONE
            }
        }
    }

    private fun updateNotifications(notifications: List<NotificationItem>) {
        binding.tvEmptyNotifications.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
        adapter.updateData(NotificationAdapter.buildItems(notifications))
        updateNotificationBadge(notifications)
    }

    private fun updateNotificationBadge(notifications: List<NotificationItem>) {
        val unreadCount = notifications.count { !it.isRead }
        if (unreadCount > 0) {
            binding.badgeNotificationCount.text = if (unreadCount > 99) "99+" else unreadCount.toString()
            binding.badgeNotificationCount.visibility = View.VISIBLE
        } else {
            binding.badgeNotificationCount.visibility = View.GONE
        }
    }

    private fun markNotificationAsRead(notification: NotificationItem) {
        if (notification.isRead) return

        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            runCatching {
                RetrofitClient.visitService.markNotificationRead("Bearer $token", notification.notifId)
            }
            loadNotifications()
        }
    }
}
