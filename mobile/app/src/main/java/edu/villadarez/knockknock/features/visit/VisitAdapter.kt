package edu.villadarez.knockknock.features.visit

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import edu.villadarez.knockknock.R
import edu.villadarez.knockknock.features.visit.VisitSummary
import java.text.SimpleDateFormat
import java.util.Locale

class VisitAdapter(
    private var items: List<VisitSummary>,
    private val onCardClick: (VisitSummary) -> Unit = {}
) : RecyclerView.Adapter<VisitAdapter.VisitViewHolder>() {

    class VisitViewHolder(itemView: View, private val onCardClick: (VisitSummary) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val tvCondoName: TextView = itemView.findViewById(R.id.tvCondoName)
        val tvDetails: TextView = itemView.findViewById(R.id.tvDetails)
        val tvStatusPill: TextView = itemView.findViewById(R.id.tvStatusPill)

        fun bind(item: VisitSummary) {
            itemView.setOnClickListener { onCardClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visit_card, parent, false)
        return VisitViewHolder(view, onCardClick)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.tvCondoName.text = item.condoName ?: "Unknown Condo"

        val dateString = item.visitDate.let { formatDate(it) }
        val details = listOfNotNull(item.unitNumber, dateString)
            .joinToString(" • ")
        holder.tvDetails.text = details

        val status = (item.status ?: "Scheduled").uppercase(Locale.getDefault())
        holder.tvStatusPill.text = when (status) {
            "SCHEDULED" -> "Scheduled"
            "CHECKED-IN" -> "Checked-in"
            "CHECKED-OUT" -> "Checked-out"
            "CANCELLED" -> "Cancelled"
            "MISSED" -> "Missed"
            else -> status.lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }

        val (backgroundColor, textColor) = when (status) {
            "SCHEDULED" -> R.color.status_scheduled_bg to R.color.status_scheduled_text
            "CHECKED-IN" -> R.color.status_checked_in_bg to R.color.status_checked_in_text
            "CHECKED-OUT" -> R.color.status_checked_out_bg to R.color.status_checked_out_text
            "CANCELLED" -> R.color.status_cancelled_bg to R.color.status_cancelled_text
            "MISSED" -> R.color.status_missed_bg to R.color.status_missed_text
            else -> R.color.status_scheduled_bg to R.color.status_scheduled_text
        }

        val pill = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            // Create a fully rounded pill effect using proper dp-to-px conversion
            val cornerRadiusDp = 50f
            val density = holder.itemView.resources.displayMetrics.density
            cornerRadius = cornerRadiusDp * density
            setColor(ContextCompat.getColor(holder.itemView.context, backgroundColor))
        }
        holder.tvStatusPill.background = pill
        holder.tvStatusPill.setTextColor(ContextCompat.getColor(holder.itemView.context, textColor))
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<VisitSummary>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatDate(raw: String): String {
        return try {
            // Backend typically returns ISO date-time, we only need the date
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd"
            )
            val parsed = inputFormats.firstNotNullOfOrNull { pattern ->
                runCatching { SimpleDateFormat(pattern, Locale.getDefault()).parse(raw) }.getOrNull()
            } ?: return raw

            SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(parsed)
        } catch (_: Exception) {
            raw
        }
    }
}
