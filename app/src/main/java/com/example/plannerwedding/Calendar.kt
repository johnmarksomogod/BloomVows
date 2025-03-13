package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView

class CalendarFragment : Fragment(), ScheduleAdapter.OnScheduleClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private var scheduleList: MutableList<ScheduleItem> = mutableListOf()
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var addScheduleButton: ImageView
    private lateinit var filterSpinner: Spinner

    private val datesWithSchedules: HashSet<CalendarDay> = hashSetOf()
    private var filteredScheduleList: MutableList<ScheduleItem> = mutableListOf()
    private var currentFilter: String = "All"
    private var selectedDateString: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewSchedules)
        calendarView = view.findViewById(R.id.materialCalendarView)
        addScheduleButton = view.findViewById(R.id.addScheduleButton)
        filterSpinner = view.findViewById(R.id.filterSpinner)

        scheduleAdapter = ScheduleAdapter(requireContext(), filteredScheduleList, this, calendarView, datesWithSchedules)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = scheduleAdapter

        calendarView.state().edit().setMinimumDate(CalendarDay.today()).commit()

        addScheduleButton.setOnClickListener {
            openScheduleDialog()
        }

        // Add listener for calendar date selection
        calendarView.setOnDateChangedListener { _, date, _ ->
            val selectedDateString = "${date.month + 1}/${date.day}/${date.year}" // Format the selected date

            // Check if the same date is clicked again
            if (selectedDateString == this.selectedDateString) {
                // If the same date is clicked, clear the date filter and show all schedules
                this.selectedDateString = null
                applyFilters() // Apply filters (this will show all schedules)
            } else {
                // If a new date is selected, apply the date filter
                this.selectedDateString = selectedDateString
                applyFilters() // Apply both date and status filters
            }
        }

        // Add listener for filterSpinner changes
        filterSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentFilter = when (position) {
                    0 -> "All"
                    1 -> "Pending"
                    2 -> "Completed"
                    3 -> "Expired"
                    else -> "All"
                }
                applyFilters() // Apply both date and status filters
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Handle the case when no item is selected, if necessary
            }
        })

        // Reset filter when clicking outside the calendar
        view.findViewById<View>(R.id.fragment_root_view).setOnTouchListener { _, _ ->
            // Clear the selected date when clicking outside the calendar
            selectedDateString = null
            applyFilters() // Apply filters to show all schedules
            true
        }

        fetchSchedulesFromFirestore()

        return view
    }

    // Open dialog to add schedule
    private fun openScheduleDialog() {
        val dialog = ScheduleDialogFragment()
        dialog.onScheduleAddedListener = object : ScheduleDialogFragment.OnScheduleAddedListener {
            override fun onScheduleAdded(scheduleItem: ScheduleItem) {
                addScheduleToFirestore(scheduleItem) // Directly add schedule to Firestore
            }
        }
        dialog.show(childFragmentManager, "AddScheduleDialog")
    }

    // Add schedule to Firestore
    private fun addScheduleToFirestore(scheduleItem: ScheduleItem) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val db = FirebaseFirestore.getInstance()
            val scheduleRef = db.collection("Users").document(userId).collection("Schedules")

            // Update status before saving to ensure it's correct
            scheduleItem.updateStatus()

            scheduleRef.document(scheduleItem.scheduleId).set(scheduleItem)
                .addOnSuccessListener {
                    if (!scheduleList.any { it.scheduleId == scheduleItem.scheduleId }) {
                        scheduleList.add(scheduleItem)
                        addDateWithSchedule(scheduleItem.scheduleDate)
                        applyFilters() // Apply filters after adding new item
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add schedule.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Add date to the hash set for the calendar decorator
    private fun addDateWithSchedule(dateString: String) {
        val dateParts = dateString.split("/")
        if (dateParts.size == 3) {
            try {
                val calendarDay = CalendarDay.from(
                    dateParts[2].toInt(),
                    dateParts[0].toInt() - 1,
                    dateParts[1].toInt()
                )
                datesWithSchedules.add(calendarDay)
                calendarView.addDecorator(DotDecorator(datesWithSchedules))
            } catch (e: Exception) {
                // Handle parsing exceptions
            }
        }
    }

    // Fetch schedules from Firestore
    private fun fetchSchedulesFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()
            val scheduleRef = db.collection("Users").document(userId).collection("Schedules")

            scheduleRef.get().addOnSuccessListener { documents ->
                scheduleList.clear()
                datesWithSchedules.clear()

                for (document in documents) {
                    val schedule = document.toObject(ScheduleItem::class.java)
                    // Update the status based on current date
                    schedule.updateStatus()
                    scheduleList.add(schedule)

                    // Add to calendar dates with events
                    addDateWithSchedule(schedule.scheduleDate)
                }

                // Apply filters based on current selection
                applyFilters()
            }
        }
    }

    // Apply both date and status filters
    private fun applyFilters() {
        filteredScheduleList.clear()

        // First filter by date if a date is selected
        val dateFilteredList = if (selectedDateString != null) {
            scheduleList.filter { it.scheduleDate == selectedDateString }
        } else {
            scheduleList
        }

        // Then apply status filter
        filteredScheduleList.addAll(
            when (currentFilter) {
                "Completed" -> dateFilteredList.filter { it.status == "Completed" }
                "Pending" -> dateFilteredList.filter { it.status == "Pending" }
                "Expired" -> dateFilteredList.filter { it.status == "Expired" }
                else -> dateFilteredList // "All" filter
            }
        )

        // Update the UI
        if (filteredScheduleList.isEmpty() && selectedDateString != null) {
            Toast.makeText(context, "No ${currentFilter.lowercase()} schedules for $selectedDateString", Toast.LENGTH_SHORT).show()
        }

        scheduleAdapter.notifyDataSetChanged()
    }

    // Schedule item edit, delete, or complete actions
    override fun onScheduleComplete(position: Int) {
        val scheduleItem = filteredScheduleList[position]

        // Update the item's status
        scheduleItem.status = "Completed"

        // Find the position in the original list
        val originalPosition = scheduleList.indexOfFirst { it.scheduleId == scheduleItem.scheduleId }
        if (originalPosition != -1) {
            scheduleList[originalPosition].status = "Completed"
        }

        updateScheduleInFirestore(scheduleItem, position)
    }

    override fun onScheduleDelete(position: Int) {
        val removedSchedule = filteredScheduleList[position]

        // Find the position in the original list
        val originalPosition = scheduleList.indexOfFirst { it.scheduleId == removedSchedule.scheduleId }
        if (originalPosition != -1) {
            scheduleList.removeAt(originalPosition)
        }

        filteredScheduleList.removeAt(position)

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseFirestore.getInstance().collection("Users").document(user.uid)
                .collection("Schedules").document(removedSchedule.scheduleId)
                .delete()
                .addOnSuccessListener {
                    scheduleAdapter.notifyItemRemoved(position)
                    fetchSchedulesFromFirestore() // Refresh after deletion
                }
        }
    }

    override fun onScheduleEdit(scheduleItem: ScheduleItem) {
        // Here you might want to implement editing functionality
        // For now, we'll just open a new dialog
        openScheduleDialog()
    }

    // Update schedule in Firestore after editing
    private fun updateScheduleInFirestore(scheduleItem: ScheduleItem, position: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseFirestore.getInstance().collection("Users").document(it.uid)
                .collection("Schedules").document(scheduleItem.scheduleId)
                .update("status", scheduleItem.status)
                .addOnSuccessListener {
                    scheduleAdapter.notifyItemChanged(position)

                    // If the current filter would exclude this item, reapply filters
                    if (currentFilter != "All" && currentFilter != scheduleItem.status) {
                        applyFilters()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update schedule.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
