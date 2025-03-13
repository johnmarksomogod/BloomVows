package com.example.plannerwedding

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView

class ScheduleAdapter(
    private val context: Context,
    private val scheduleList: MutableList<ScheduleItem>,
    private val onScheduleClickListener: OnScheduleClickListener,
    private val calendarView: MaterialCalendarView?,
    private val datesWithSchedules: HashSet<CalendarDay>
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    interface OnScheduleClickListener {
        fun onScheduleComplete(position: Int)
        fun onScheduleDelete(position: Int)
        fun onScheduleEdit(scheduleItem: ScheduleItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.fragment_item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = scheduleList[position]

        // Update status before displaying
        schedule.updateStatus()

        holder.scheduleTitle.text = schedule.scheduleName
        holder.scheduleDate.text = "Scheduled Date: ${schedule.scheduleDate}"

        // Set status based on completed, pending, or expired
        when (schedule.status) {
            "Completed" -> {
                holder.scheduleStatus.text = "Completed"
                holder.scheduleStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
                holder.completeScheduleIcon.setImageResource(R.drawable.heart_filled)
            }
            "Expired" -> {
                holder.scheduleStatus.text = "Expired"
                holder.scheduleStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
                holder.completeScheduleIcon.setImageResource(R.drawable.broken_heart)
            }
            else -> { // "Pending"
                holder.scheduleStatus.text = "Pending"
                holder.scheduleStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
                holder.completeScheduleIcon.setImageResource(R.drawable.heart1)
            }
        }

        // Only allow completion for Pending or Expired items
        if (schedule.status != "Completed") {
            holder.completeScheduleIcon.setOnClickListener {
                showCompletionConfirmationDialog(schedule, position)
            }
        } else {
            // If already completed, maybe disable the click or add a different action
            holder.completeScheduleIcon.isClickable = false
        }

        holder.deleteScheduleIcon.setOnClickListener {
            showDeleteConfirmationDialog(schedule, position)
        }

        holder.itemView.setOnClickListener {
            onScheduleClickListener.onScheduleEdit(schedule)
        }
    }

    override fun getItemCount(): Int = scheduleList.size

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val scheduleTitle: TextView = itemView.findViewById(R.id.scheduleTitle)
        val scheduleDate: TextView = itemView.findViewById(R.id.scheduleDate)
        val scheduleStatus: TextView = itemView.findViewById(R.id.scheduleStatus)
        val completeScheduleIcon: ImageView = itemView.findViewById(R.id.completeScheduleIcon)
        val deleteScheduleIcon: ImageView = itemView.findViewById(R.id.deleteScheduleIcon)
    }

    private fun showDeleteConfirmationDialog(schedule: ScheduleItem, position: Int) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Delete Schedule")
        alertDialogBuilder.setMessage("Are you sure you want to delete this schedule?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                onScheduleClickListener.onScheduleDelete(position)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }

        val alert = alertDialogBuilder.create()
        alert.show()

        // Change the text color of the buttons
        val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveButton.setTextColor(Color.parseColor("#EDABAD"))
        negativeButton.setTextColor(Color.parseColor("#EDABAD"))

        // Set dialog message text color to black
        alert.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
    }

    private fun showCompletionConfirmationDialog(schedule: ScheduleItem, position: Int) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Mark as Completed")
        alertDialogBuilder.setMessage("Are you sure you want to mark this schedule as completed?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                onScheduleClickListener.onScheduleComplete(position)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }

        val alert = alertDialogBuilder.create()
        alert.show()

        // Set background color to white
        alert.window?.setBackgroundDrawableResource(android.R.color.white)

        // Change the text color of the buttons
        val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveButton.setTextColor(Color.parseColor("#EDABAD"))
        negativeButton.setTextColor(Color.parseColor("#EDABAD"))

        // Set dialog message text color to black
        alert.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
    }
}
