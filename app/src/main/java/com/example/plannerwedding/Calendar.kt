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
            val selectedDate = "${date.month + 1}/${date.day}/${date.year}" // Format the selected date
            showSchedulesForSelectedDate(selectedDate) // Show schedules only for the selected date
        }

        // Add listener for filterSpinner changes
        filterSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> showAllSchedules()  // Show All schedules
                    1 -> showPendingSchedules()  // Show Pending schedules
                    2 -> showCompletedSchedules()  // Show Completed schedules
                    3 -> showExpiredSchedules()  // Show Expired schedules
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Handle the case when no item is selected, if necessary
            }
        })

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

            scheduleRef.document(scheduleItem.scheduleId).set(scheduleItem)
                .addOnSuccessListener {
                    if (!scheduleList.any { it.scheduleId == scheduleItem.scheduleId }) {
                        scheduleList.add(scheduleItem)
                        filterSchedulesByDate(calendarView.selectedDate) // Refresh the schedule list based on the selected date
                        scheduleAdapter.notifyDataSetChanged()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add schedule.", Toast.LENGTH_SHORT).show()
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
                    scheduleList.add(schedule)

                    val dateParts = schedule.scheduleDate.split("/")
                    val calendarDay = CalendarDay.from(
                        dateParts[2].toInt(),
                        dateParts[0].toInt() - 1,
                        dateParts[1].toInt()
                    )
                    datesWithSchedules.add(calendarDay)
                }

                // Initially display all schedules (before any date selection)
                filterSchedulesByDate(calendarView.selectedDate)

                scheduleAdapter.notifyDataSetChanged()
                calendarView.addDecorator(DotDecorator(datesWithSchedules))
            }
        }
    }

    // Filter the schedules by the selected date
    private fun filterSchedulesByDate(selectedDate: CalendarDay?) {
        filteredScheduleList.clear()

        selectedDate?.let {
            val selectedDateString = "${it.month + 1}/${it.day}/${it.year}"
            filteredScheduleList.addAll(scheduleList.filter { schedule ->
                schedule.scheduleDate == selectedDateString
            })
        }

        applyFilter() // Apply the filter based on the spinner selection
    }

    // Apply filter based on spinner selection
    private fun applyFilter() {
        val selectedFilter = filterSpinner.selectedItem.toString()

        filteredScheduleList.clear()
        filteredScheduleList.addAll(scheduleList.filter { schedule ->
            when (selectedFilter) {
                "Completed" -> schedule.status == "Completed"
                "Pending" -> schedule.status == "Pending"
                "Expired" -> schedule.status == "Expired"  // "Expired" instead of "Overdue"
                else -> true // Default: show all schedules
            }
        })

        scheduleAdapter.notifyDataSetChanged() // Refresh the adapter to show the filtered schedules
    }

    // Function to display schedules for the selected date
    private fun showSchedulesForSelectedDate(selectedDate: String) {
        val filteredSchedules = scheduleList.filter { it.scheduleDate == selectedDate }

        if (filteredSchedules.isNotEmpty()) {
            Toast.makeText(context, "Showing schedules for $selectedDate", Toast.LENGTH_SHORT).show()
            filteredScheduleList.clear()
            filteredScheduleList.addAll(filteredSchedules)
        } else {
            Toast.makeText(context, "No schedules for $selectedDate", Toast.LENGTH_SHORT).show()
        }

        scheduleAdapter.notifyDataSetChanged()  // Notify the adapter to refresh the list
    }

    // Show All schedules
    private fun showAllSchedules() {
        filteredScheduleList.clear()
        filteredScheduleList.addAll(scheduleList)
        scheduleAdapter.notifyDataSetChanged()
    }

    // Show Pending schedules
    private fun showPendingSchedules() {
        val filteredSchedules = scheduleList.filter { it.status == "Pending" }
        filteredScheduleList.clear()
        filteredScheduleList.addAll(filteredSchedules)
        scheduleAdapter.notifyDataSetChanged()
    }

    // Show Completed schedules
    private fun showCompletedSchedules() {
        val filteredSchedules = scheduleList.filter { it.status == "Completed" }
        filteredScheduleList.clear()
        filteredScheduleList.addAll(filteredSchedules)
        scheduleAdapter.notifyDataSetChanged()
    }

    // Show Expired schedules (replacing Overdue)
    private fun showExpiredSchedules() {
        val currentDate = CalendarDay.today()
        val filteredSchedules = scheduleList.filter {
            val dateParts = it.scheduleDate.split("/")
            val scheduleDate = CalendarDay.from(
                dateParts[2].toInt(),
                dateParts[0].toInt() - 1,
                dateParts[1].toInt()
            )
            scheduleDate.isBefore(currentDate) && it.status != "Completed" // Expired if date is in the past and not completed
        }

        filteredScheduleList.clear()
        filteredScheduleList.addAll(filteredSchedules)
        scheduleAdapter.notifyDataSetChanged()
    }

    // Schedule item edit, delete, or complete actions
    override fun onScheduleComplete(position: Int) {
        val scheduleItem = filteredScheduleList[position]
        scheduleItem.status = "Completed"
        updateScheduleInFirestore(scheduleItem, position)
    }

    override fun onScheduleDelete(position: Int) {
        val removedSchedule = filteredScheduleList[position]
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
        openScheduleDialog()
    }

    // Update schedule in Firestore after editing
    private fun updateScheduleInFirestore(scheduleItem: ScheduleItem, position: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseFirestore.getInstance().collection("Users").document(it.uid)
                .collection("Schedules").document(scheduleItem.scheduleId)
                .update("status", "Completed")
                .addOnSuccessListener {
                    scheduleAdapter.notifyItemChanged(position)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update schedule.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
