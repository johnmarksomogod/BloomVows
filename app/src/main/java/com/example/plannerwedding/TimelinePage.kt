package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plannerwedding.R

class TimelineFragment : Fragment() {

    private lateinit var timelineBackButton: ImageView // Declare lateinit for timelineBackButton
    private lateinit var addEventButton: LinearLayout
    private lateinit var timelineRecyclerView: RecyclerView
    private lateinit var timelineAdapter: TimelineAdapter
    private val timelineList = mutableListOf<TimelineActivity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timeline_page, container, false)

        // Initialize views
        timelineBackButton = view.findViewById(R.id.timelineBackButton) // Initialize the back button view
        addEventButton = view.findViewById(R.id.addEventButton)
        timelineRecyclerView = view.findViewById(R.id.timelineRecyclerView)

        // Set up RecyclerView
        timelineAdapter = TimelineAdapter(timelineList)
        timelineRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        timelineRecyclerView.adapter = timelineAdapter

        // Handle the back button click (navigate back to the previous screen)
        timelineBackButton.setOnClickListener {
            findNavController().navigateUp() // Navigate back to the previous fragment or activity
        }

        // Add event button
        addEventButton.setOnClickListener {
            val dialog = TimelineDialogFragment { title, time ->
                timelineList.add(TimelineActivity(title, time))
                timelineAdapter.notifyItemInserted(timelineList.size - 1)
            }
            dialog.show(childFragmentManager, "TimelineDialogFragment")
        }

        return view
    }
}
