package com.example.plannerwedding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plannerwedding.R

class TimelineAdapter(private val timelineList: MutableList<TimelineActivity>) : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    class TimelineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activityText: TextView = view.findViewById(R.id.activityText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_timeline_activity, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val activity = timelineList[position]
        holder.activityText.text = "${activity.title} - ${activity.time}"
    }

    override fun getItemCount(): Int = timelineList.size
}
