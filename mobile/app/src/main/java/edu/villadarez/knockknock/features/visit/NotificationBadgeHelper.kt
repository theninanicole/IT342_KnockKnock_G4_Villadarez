package edu.villadarez.knockknock.features.visit

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import edu.villadarez.knockknock.core.network.RetrofitClient
import edu.villadarez.knockknock.shared.session.SessionManager
import kotlinx.coroutines.launch

object NotificationBadgeHelper {

    fun refresh(
        lifecycleScope: LifecycleCoroutineScope,
        sessionManager: SessionManager,
        badgeView: TextView
    ) {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            badgeView.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            val unreadCount = runCatching {
                val response = RetrofitClient.visitService.getNotifications("Bearer $token", 50)
                if (response.isSuccessful) {
                    response.body()
                        ?.notifications
                        .orEmpty()
                        .count { !it.isRead }
                } else {
                    0
                }
            }.getOrDefault(0)

            if (unreadCount > 0) {
                badgeView.text = if (unreadCount > 99) "99+" else unreadCount.toString()
                badgeView.visibility = View.VISIBLE
            } else {
                badgeView.visibility = View.GONE
            }
        }
    }
}
