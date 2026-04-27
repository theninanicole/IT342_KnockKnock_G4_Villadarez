package edu.villadarez.knockknock.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.villadarez.knockknock.R
import edu.villadarez.knockknock.models.VisitSummary
import java.text.SimpleDateFormat
import java.util.Locale

class VisitAdapter(
    private var items: List<VisitSummary>
) : RecyclerView.Adapter<VisitAdapter.VisitViewHolder>() {

    class VisitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCondoName: TextView = itemView.findViewById(R.id.tvCondoName)
        val tvDetails: TextView = itemView.findViewById(R.id.tvDetails)
        val tvStatusPill: TextView = itemView.findViewById(R.id.tvStatusPill)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visit_card, parent, false)
        return VisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val item = items[position]
        holder.tvCondoName.text = item.condoName ?: "Unknown Condo"

        val dateString = item.visitDate.let { formatDate(it) }
        val details = listOfNotNull(item.unitNumber, dateString)
            .joinToString(" \\u2022 ")
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
