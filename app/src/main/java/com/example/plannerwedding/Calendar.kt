package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewSchedules)
        calendarView = view.findViewById(R.id.materialCalendarView)
        addScheduleButton = view.findViewById(R.id.addScheduleButton)

        scheduleAdapter = ScheduleAdapter(requireContext(), scheduleList, this, calendarView, datesWithSchedules)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = scheduleAdapter

        calendarView.state().edit().setMinimumDate(CalendarDay.today()).commit()

        addScheduleButton.setOnClickListener {
            openScheduleDialog()
        }

        fetchSchedulesFromFirestore()

        return view
    }

    private fun openScheduleDialog() {
        val dialog = ScheduleDialogFragment()
        dialog.onScheduleAddedListener = object : ScheduleDialogFragment.OnScheduleAddedListener {
            override fun onScheduleAdded(scheduleItem: ScheduleItem) {
                addScheduleToFirestore(scheduleItem) // ✅ Directly add schedule to Firestore (No UI duplication)
            }
        }
        dialog.show(childFragmentManager, "AddScheduleDialog")
    }

    private fun addScheduleToFirestore(scheduleItem: ScheduleItem) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val db = FirebaseFirestore.getInstance()
            val scheduleRef = db.collection("Users").document(userId).collection("Schedules")

            scheduleRef.document(scheduleItem.scheduleId).set(scheduleItem)
                .addOnSuccessListener {
                    // ✅ Instead of fetching everything again, manually add to the list
                    if (!scheduleList.any { it.scheduleId == scheduleItem.scheduleId }) {
                        scheduleList.add(scheduleItem)
                        scheduleAdapter.notifyDataSetChanged()

                        val dateParts = scheduleItem.scheduleDate.split("/")
                        val calendarDay = CalendarDay.from(
                            dateParts[2].toInt(),
                            dateParts[0].toInt() - 1,
                            dateParts[1].toInt()
                        )
                        datesWithSchedules.add(calendarDay)
                        calendarView.addDecorator(DotDecorator(datesWithSchedules))
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add schedule.", Toast.LENGTH_SHORT).show()
                }
        }
    }

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

                scheduleAdapter.notifyDataSetChanged()
                calendarView.addDecorator(DotDecorator(datesWithSchedules))
            }
        }
    }

    override fun onScheduleComplete(position: Int) {
        val scheduleItem = scheduleList[position]
        scheduleItem.status = "Completed"
        updateScheduleInFirestore(scheduleItem, position)
    }

    override fun onScheduleDelete(position: Int) {
        val removedSchedule = scheduleList[position]
        scheduleList.removeAt(position)

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseFirestore.getInstance().collection("Users").document(user.uid)
                .collection("Schedules").document(removedSchedule.scheduleId)
                .delete()
                .addOnSuccessListener {
                    scheduleAdapter.notifyItemRemoved(position)
                    fetchSchedulesFromFirestore()
                }
        }
    }

    override fun onScheduleEdit(scheduleItem: ScheduleItem) {
        openScheduleDialog()
    }

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
