package com.example.plannerwedding

import android.os.Bundle
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

class CalendarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val addEventIcon = view.findViewById<ImageView>(R.id.addEventIcon)
        val upcomingSchedules = view.findViewById<LinearLayout>(R.id.upcomingSchedules)

        // Handle date selection
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            Toast.makeText(requireContext(), "Selected: $selectedDate", Toast.LENGTH_SHORT).show()
        }

        // Handle add event button click
        addEventIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Add Event Clicked", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
