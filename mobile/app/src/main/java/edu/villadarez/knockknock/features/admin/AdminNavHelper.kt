package edu.villadarez.knockknock.features.admin

import android.content.Intent
import android.graphics.Typeface
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import edu.villadarez.knockknock.R
import edu.villadarez.knockknock.features.auth.LoginActivity
import edu.villadarez.knockknock.features.visit.AccountActivity
import edu.villadarez.knockknock.shared.session.SessionManager

object AdminNavHelper {
    fun setup(
        activity: AppCompatActivity,
        sessionManager: SessionManager,
        activeTab: TextView,
        home: TextView,
        allVisits: TextView,
        history: TextView,
        account: TextView,
        logout: TextView
    ) {
        val tabs = listOf(home, allVisits, history, account, logout)
        val activeColor = ContextCompat.getColor(activity, R.color.brand_blue_primary)
        val inactiveColor = ContextCompat.getColor(activity, R.color.nav_unselected)

        tabs.forEach { tab ->
            val isActive = tab == activeTab
            val color = if (isActive) activeColor else inactiveColor
            tab.setTextColor(color)
            tab.setTypeface(null, if (isActive) Typeface.BOLD else Typeface.NORMAL)
            val drawables = tab.compoundDrawablesRelative
            drawables.forEach { it?.mutate()?.setTint(color) }
            tab.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3])
        }

        home.setOnClickListener {
            if (activity !is AdminDashboardActivity) {
                activity.startActivity(Intent(activity, AdminDashboardActivity::class.java))
                activity.finish()
            }
        }
        allVisits.setOnClickListener {
            if (activity !is AdminAllVisitsActivity) {
                activity.startActivity(Intent(activity, AdminAllVisitsActivity::class.java))
                activity.finish()
            }
        }
        history.setOnClickListener {
            if (activity !is AdminStatusHistoryActivity) {
                activity.startActivity(Intent(activity, AdminStatusHistoryActivity::class.java))
                activity.finish()
            }
        }
        account.setOnClickListener {
            activity.startActivity(Intent(activity, AccountActivity::class.java))
        }
        logout.setOnClickListener {
            sessionManager.saveAuthToken("")
            activity.startActivity(Intent(activity, LoginActivity::class.java))
            activity.finish()
        }
    }
}
