package com.example.plannerwedding

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TimelineFragment : Fragment() {

    private lateinit var timelineBackButton: ImageView
    private lateinit var addEventButton: LinearLayout
    private lateinit var timelineRecyclerView: RecyclerView
    private lateinit var timelineAdapter: TimelineAdapter
    private val timelineList = mutableListOf<TimelineActivity>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timeline_page, container, false)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        timelineBackButton = view.findViewById(R.id.timelineBackButton)
        addEventButton = view.findViewById(R.id.addEventButton)
        timelineRecyclerView = view.findViewById(R.id.timelineRecyclerView)

        // Set up RecyclerView
        timelineAdapter = TimelineAdapter(
            timelineList,
            { event, position -> showEditDialog(event, position) },  // Edit action
            { event, position -> showDeleteConfirmationDialog(event, position) }  // Delete action
        )
        timelineRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        timelineRecyclerView.adapter = timelineAdapter

        // Handle the back button click
        timelineBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Add event button
        addEventButton.setOnClickListener {
            val dialog = TimelineDialogFragment { title, time ->
                val newEvent = TimelineActivity(title, time)
                saveEventToFirestore(newEvent)
            }
            dialog.show(childFragmentManager, "TimelineDialogFragment")
        }

        loadEventsFromFirestore()

        return view
    }

    // Load the timeline events from Firestore
    private fun loadEventsFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val events = document.get("timeline") as? List<Map<String, Any>> ?: mutableListOf()
            timelineList.clear()

            // Convert event data from Firestore format to TimelineActivity object
            events.forEach { event ->
                val title = event["title"] as? String ?: ""
                val time = event["time"] as? String ?: ""
                val newEvent = TimelineActivity(title, time)
                timelineList.add(newEvent)
            }

            sortEvents() // Sort events after loading from Firestore
            timelineAdapter.notifyDataSetChanged()
        }
    }

    // Sort the events by time in AM-PM order
    private fun sortEvents() {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        timelineList.sortWith { event1, event2 ->
            try {
                val time1 = timeFormat.parse(event1.time)
                val time2 = timeFormat.parse(event2.time)
                time1?.compareTo(time2) ?: 0
            } catch (e: ParseException) {
                e.printStackTrace()
                0 // Default comparison if parsing fails
            }
        }
    }

    // Save the event to Firestore
    private fun saveEventToFirestore(event: TimelineActivity) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(userId)

        // Fetch the current event list from Firestore
        userRef.get().addOnSuccessListener { document ->
            val events = document.get("timeline") as? List<Map<String, Any>> ?: mutableListOf()
            val updatedEvents = events.toMutableList()

            // Add the new event to the list
            updatedEvents.add(mapOf(
                "title" to event.title,
                "time" to event.time
            ))

            // Update Firestore with the new event list
            userRef.update("timeline", updatedEvents)
                .addOnSuccessListener {
                    timelineList.add(event) // Add the new event to the list
                    sortEvents() // Sort events after adding
                    timelineAdapter.notifyItemInserted(timelineList.size - 1) // Update the UI
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add event", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Show delete confirmation dialog
    private fun showDeleteConfirmationDialog(event: TimelineActivity, position: Int) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setMessage("Are you sure you want to delete this event?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> deleteEvent(event, position) }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }

        val alert = alertDialogBuilder.create()
        alert.show()

        alert.window?.setBackgroundDrawableResource(android.R.color.white)

        val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveButton.setTextColor(Color.parseColor("#EDABAD"))
        negativeButton.setTextColor(Color.parseColor("#EDABAD"))
    }

    // Delete the event from Firestore and remove it from the UI
    private fun deleteEvent(event: TimelineActivity, position: Int) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(userId)

        // Fetch current event list and remove the event
        userRef.get().addOnSuccessListener { document ->
            val events = document.get("timeline") as? MutableList<Map<String, Any>> ?: mutableListOf()

            // Remove event from list
            events.removeAt(position)

            // Update Firestore with the new list
            userRef.update("timeline", events)
                .addOnSuccessListener {
                    timelineList.removeAt(position) // Remove from UI
                    timelineAdapter.notifyItemRemoved(position) // Update RecyclerView
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to delete event", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Show edit dialog with pre-populated data
    private fun showEditDialog(event: TimelineActivity, position: Int) {
        val dialog = TimelineDialogFragment { title, time ->
            // Show confirmation dialog before saving changes
            showSaveConfirmationDialog(title, time, event, position)
        }

        dialog.setEventData(event.title, event.time)
        dialog.show(childFragmentManager, "TimelineEditDialogFragment")
    }

    // Confirmation dialog to confirm saving changes
    private fun showSaveConfirmationDialog(newTitle: String, newTime: String, event: TimelineActivity, position: Int) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setMessage("Are you sure you want to save these changes?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                event.title = newTitle
                event.time = newTime
                updateEventInFirestore(event, position)
            }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }

        alertDialog.create().show()
    }

    // Update the event in Firestore after editing
    private fun updateEventInFirestore(event: TimelineActivity, position: Int) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(userId)

        // Fetch current event list and update the event
        userRef.get().addOnSuccessListener { document ->
            val events = document.get("timeline") as? MutableList<Map<String, Any>> ?: mutableListOf()

            // Update the event
            val updatedEvent = mapOf(
                "title" to event.title,
                "time" to event.time
            )
            events[position] = updatedEvent

            // Update Firestore with the updated list
            userRef.update("timeline", events)
                .addOnSuccessListener {
                    timelineList[position] = event // Update the UI list
                    timelineAdapter.notifyItemChanged(position) // Notify RecyclerView
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update event", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
