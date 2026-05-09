package edu.villadarez.knockknock.features.visit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.villadarez.knockknock.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationAdapter(
    private var items: List<NotificationListItem>,
    private val onNotificationClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is NotificationListItem.Section -> VIEW_TYPE_SECTION
            is NotificationListItem.NotificationRow -> VIEW_TYPE_NOTIFICATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SECTION) {
            SectionViewHolder(inflater.inflate(R.layout.item_notification_section, parent, false))
        } else {
            NotificationViewHolder(inflater.inflate(R.layout.item_notification_card, parent, false), onNotificationClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is NotificationListItem.Section -> (holder as SectionViewHolder).bind(item.title)
            is NotificationListItem.NotificationRow -> (holder as NotificationViewHolder).bind(item.notification)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<NotificationListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvNotificationSection)

        fun bind(text: String) {
            title.text = text
        }
    }

    class NotificationViewHolder(
        itemView: View,
        private val onNotificationClick: (NotificationItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        private val time: TextView = itemView.findViewById(R.id.tvNotificationTime)
        private val message: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        private val unreadDot: View = itemView.findViewById(R.id.viewUnreadDot)

        fun bind(notification: NotificationItem) {
            title.text = notification.title
            message.text = notification.message
            time.text = formatRelativeTime(notification.createdAt)
            unreadDot.setBackgroundResource(if (notification.isRead) R.drawable.bg_read_dot else R.drawable.bg_unread_dot)
            itemView.alpha = if (notification.isRead) 0.78f else 1f
            itemView.setOnClickListener { onNotificationClick(notification) }
        }
    }

    companion object {
        private const val VIEW_TYPE_SECTION = 0
        private const val VIEW_TYPE_NOTIFICATION = 1

        fun buildItems(notifications: List<NotificationItem>): List<NotificationListItem> {
            val sorted = notifications.sortedByDescending { parseDate(it.createdAt)?.time ?: 0L }
            val grouped = sorted.groupBy { sectionForDate(parseDate(it.createdAt)) }
            val sections = listOf("Today", "Yesterday", "Earlier")

            return sections.flatMap { section ->
                val rows = grouped[section].orEmpty()
                if (rows.isEmpty()) {
                    emptyList()
                } else {
                    listOf(NotificationListItem.Section(section)) +
                        rows.map { NotificationListItem.NotificationRow(it) }
                }
            }
        }

        private fun sectionForDate(date: Date?): String {
            if (date == null) return "Earlier"

            val today = Calendar.getInstance().startOfDay()
            val notificationDay = Calendar.getInstance().apply { time = date }.startOfDay()
            val diffDays = TimeUnit.MILLISECONDS.toDays(today.timeInMillis - notificationDay.timeInMillis)

            return when (diffDays) {
                0L -> "Today"
                1L -> "Yesterday"
                else -> "Earlier"
            }
        }

        private fun formatRelativeTime(raw: String): String {
            val date = parseDate(raw) ?: return ""
            val diffMillis = System.currentTimeMillis() - date.time
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis).coerceAtLeast(0)

            return when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                minutes < 1440 -> "${TimeUnit.MINUTES.toHours(minutes)}h ago"
                minutes < 2880 -> "Yesterday"
                else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
            }
        }

        private fun parseDate(raw: String?): Date? {
            if (raw.isNullOrBlank()) return null
            val patterns = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss.SSSSSS",
                "yyyy-MM-dd HH:mm:ss"
            )

            return patterns.firstNotNullOfOrNull { pattern ->
                runCatching {
                    SimpleDateFormat(pattern, Locale.getDefault()).parse(raw.trim().removeSuffix("Z"))
                }.getOrNull()
            }
        }

        private fun Calendar.startOfDay(): Calendar {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            return this
        }
    }
}

sealed class NotificationListItem {
    data class Section(val title: String) : NotificationListItem()
    data class NotificationRow(val notification: NotificationItem) : NotificationListItem()
}
