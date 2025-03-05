package com.example.plannerwedding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimelineAdapter(
    private val timelineList: MutableList<TimelineActivity>,
    private val onEditClick: (TimelineActivity, Int) -> Unit,
    private val onDeleteClick: (TimelineActivity, Int) -> Unit
) : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    class TimelineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activityText: TextView = view.findViewById(R.id.activityText)
        val editEvent: ImageView = view.findViewById(R.id.editevent)
        val deleteEvent: ImageView = view.findViewById(R.id.deleteevent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_timeline_activity, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val event = timelineList[position]
        holder.activityText.text = "${event.title} - ${event.time}"

        // Handle edit event
        holder.itemView.setOnClickListener {
            onEditClick(event, position) // Trigger the edit action when clicking the card
        }

        // Handle delete event
        holder.deleteEvent.setOnClickListener {
            onDeleteClick(event, position) // Trigger the delete action when clicking the delete button
        }

        // Optionally handle edit button if needed
        holder.editEvent.setOnClickListener {
            onEditClick(event, position) // Trigger the edit dialog when clicking the edit button
        }
    }

    override fun getItemCount(): Int = timelineList.size
}
