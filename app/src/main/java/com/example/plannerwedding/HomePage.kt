package com.example.plannerwedding

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import java.util.*

class HomePage : Fragment(R.layout.fragment_home_page), ScheduleAdapter.OnScheduleClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var daysLeftTextView: TextView
    private lateinit var venueTextView: TextView
    private lateinit var coupleNamesTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var totalTasksText: TextView
    private lateinit var completedTasksText: TextView
    private lateinit var remainingTasksText: TextView
    private lateinit var totalBudgetText: TextView
    private lateinit var spentText: TextView
    private lateinit var remainingText: TextView

    // Schedule-related variables
    private lateinit var recyclerView: RecyclerView
    private lateinit var filterSpinner: Spinner
    private lateinit var scheduleAdapter: ScheduleAdapter
    private var scheduleList: MutableList<ScheduleItem> = mutableListOf()
    private var filteredScheduleList: MutableList<ScheduleItem> = mutableListOf()
    private var currentFilter: String = "All"
    private val datesWithSchedules: HashSet<CalendarDay> = hashSetOf()

    private var totalTasks = 0
    private var completedTasks = 0
    private var totalBudget: Double = 0.0
    private var spentAmount: Double = 0.0
    private var remainingBudget: Double = 0.0

    // Loader counter to track simultaneous operations
    private var loadingOperations = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        daysLeftTextView = view.findViewById(R.id.DaysLeft)
        venueTextView = view.findViewById(R.id.Venue)
        coupleNamesTextView = view.findViewById(R.id.coupleNamesText)
        progressBar = view.findViewById(R.id.todoProgress)
        totalTasksText = view.findViewById(R.id.totalTasks)
        completedTasksText = view.findViewById(R.id.completedTasks)
        remainingTasksText = view.findViewById(R.id.remainingTasks)
        totalBudgetText = view.findViewById(R.id.totalBudgetText)
        spentText = view.findViewById(R.id.spentText)
        remainingText = view.findViewById(R.id.remainingText)

        // Initialize Schedule views
        recyclerView = view.findViewById(R.id.recyclerViewSchedules)
        filterSpinner = view.findViewById(R.id.filterSpinner)

        // Set up Schedule RecyclerView
        setupScheduleRecyclerView()

        // Set up filter spinner listener
        setupFilterSpinner()

        // Show loader before starting data fetching
        showLoader()

        // Fetch and display wedding data
        fetchWeddingData()

        // Fetch and display task data
        loadTasksFromFirestore()

        // Fetch and display budget data
        loadBudgetDataFromFirestore()

        // Fetch and display schedule data
        fetchSchedulesFromFirestore()

        // CardView navigation
        setupNavigation(view)

        // Set up fragment result listener for wedding date updates
        setupWeddingDateListener()
    }

    private fun setupWeddingDateListener() {
        // Listen for wedding date updates from any fragment
        parentFragmentManager.setFragmentResultListener("WEDDING_DATE_UPDATED", viewLifecycleOwner) { _, _ ->
            // Refresh wedding data when notified
            fetchWeddingData()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh wedding data whenever the fragment resumes
        fetchWeddingData()
    }

    private fun setupScheduleRecyclerView() {
        // Initialize the adapter with null for calendarView since we don't have one in HomePage
        scheduleAdapter = ScheduleAdapter(requireContext(), filteredScheduleList, this, null, datesWithSchedules)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = scheduleAdapter
    }

    private fun setupFilterSpinner() {
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = when (position) {
                    0 -> "All"
                    1 -> "Pending"
                    2 -> "Completed"
                    3 -> "Expired"
                    else -> "All"
                }
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun fetchWeddingData() {
        incrementLoadingOperations()
        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Get bride and groom names
                        val brideName = document.getString("brideName") ?: ""
                        val groomName = document.getString("groomName") ?: ""

                        // Update welcome text with names
                        if (brideName.isNotEmpty() && groomName.isNotEmpty()) {
                            coupleNamesTextView.text = "$brideName & $groomName"
                        } else {
                            coupleNamesTextView.text = "Setup your wedding details"
                        }

                        val weddingDateStr = document.getString("weddingDate") ?: ""
                        val venue = document.getString("venue") ?: "No venue set"

                        // Update venue display
                        venueTextView.text = if (venue != "No venue set") {
                            "at $venue"
                        } else {
                            "Venue not set"
                        }

                        // Update days left display
                        if (weddingDateStr.isNotEmpty()) {
                            updateDaysLeft(weddingDateStr)
                        } else {
                            daysLeftTextView.text = "No date set"
                        }
                    }
                    decrementLoadingOperations()
                }
                .addOnFailureListener { e ->
                    daysLeftTextView.text = "Error loading data"
                    venueTextView.text = "Error loading venue"
                    coupleNamesTextView.text = "Error loading names"
                    decrementLoadingOperations()
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateDaysLeft(weddingDateStr: String) {
        try {
            // Parse the wedding date using the correct format (dd/MM/yyyy)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val weddingDate = dateFormat.parse(weddingDateStr) ?: return

            // Get current date with time set to 00:00:00 for accurate day calculation
            val todayCalendar = Calendar.getInstance()
            todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
            todayCalendar.set(Calendar.MINUTE, 0)
            todayCalendar.set(Calendar.SECOND, 0)
            todayCalendar.set(Calendar.MILLISECOND, 0)

            // Set wedding date to calendar with time set to 00:00:00
            val weddingCalendar = Calendar.getInstance()
            weddingCalendar.time = weddingDate
            weddingCalendar.set(Calendar.HOUR_OF_DAY, 0)
            weddingCalendar.set(Calendar.MINUTE, 0)
            weddingCalendar.set(Calendar.SECOND, 0)
            weddingCalendar.set(Calendar.MILLISECOND, 0)

            // Calculate the difference in milliseconds and convert to days
            val diffInMillis = weddingCalendar.timeInMillis - todayCalendar.timeInMillis
            val daysLeft = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

            // Update the text view with appropriate message
            daysLeftTextView.text = when {
                daysLeft < 0 -> "Wedding day has passed!"
                daysLeft == 0 -> "Wedding day is today!"
                daysLeft == 1 -> "1 Day until The Big Day"
                else -> "$daysLeft Days until The Big Day"
            }
        } catch (e: Exception) {
            // If there's an error parsing the date, show an error message
            daysLeftTextView.text = "Invalid date format"
            e.printStackTrace() // Log the error for debugging
        }
    }

    private fun loadTasksFromFirestore() {
        incrementLoadingOperations()
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId)
            .collection("Tasks")
            .get()
            .addOnSuccessListener { snapshot ->
                totalTasks = 0
                completedTasks = 0

                if (snapshot.isEmpty) {
                    updateProgress()
                }

                for (document in snapshot.documents) {
                    val task = document.toObject(Task::class.java)
                    task?.let {
                        totalTasks++
                        if (it.completed) {
                            completedTasks++
                        }
                    }
                }
                updateProgress()
                decrementLoadingOperations()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
                decrementLoadingOperations()
            }
    }

    private fun loadBudgetDataFromFirestore() {
        incrementLoadingOperations()
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Fetch the total budget from Firestore
                    totalBudget = document.getDouble("budget") ?: 0.0
                    spentAmount = 0.0

                    incrementLoadingOperations() // For the nested query
                    db.collection("Users").document(userId).collection("Budget")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            for (document in snapshot.documents) {
                                val budgetItem = document.toObject(BudgetItem::class.java)
                                budgetItem?.let {
                                    if (it.paid) spentAmount += it.amount
                                }
                            }
                            updateProgress() // Update the progress after fetching budget data
                            decrementLoadingOperations() // For the nested query
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to load budget items", Toast.LENGTH_SHORT).show()
                            decrementLoadingOperations() // For the nested query
                        }
                }
                decrementLoadingOperations() // For the outer query
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load total budget", Toast.LENGTH_SHORT).show()
                decrementLoadingOperations() // For the outer query
            }
    }

    private fun fetchSchedulesFromFirestore() {
        incrementLoadingOperations()
        val user = auth.currentUser ?: return
        val userId = user.uid

        db.collection("Users").document(userId).collection("Schedules")
            .get()
            .addOnSuccessListener { documents ->
                scheduleList.clear()

                for (document in documents) {
                    val schedule = document.toObject(ScheduleItem::class.java)
                    // Update the status based on current date
                    schedule.updateStatus()
                    scheduleList.add(schedule)
                }

                // Apply filters to show schedules
                applyFilters()
                decrementLoadingOperations()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load schedules", Toast.LENGTH_SHORT).show()
                decrementLoadingOperations()
            }
    }

    private fun applyFilters() {
        filteredScheduleList.clear()

        // Apply status filter
        filteredScheduleList.addAll(
            when (currentFilter) {
                "Completed" -> scheduleList.filter { it.status == "Completed" }
                "Pending" -> scheduleList.filter { it.status == "Pending" }
                "Expired" -> scheduleList.filter { it.status == "Expired" }
                else -> scheduleList // "All" filter
            }
        )

        // Sort schedules by date (newest first)
        filteredScheduleList.sortBy {
            val dateParts = it.scheduleDate.split("/")
            if (dateParts.size == 3) {
                val month = dateParts[0].toInt()
                val day = dateParts[1].toInt()
                val year = dateParts[2].toInt()
                "${year}${month.toString().padStart(2, '0')}${day.toString().padStart(2, '0')}"
            } else {
                "99991231" // Default to far future for invalid dates
            }
        }

        // Update the adapter
        scheduleAdapter.notifyDataSetChanged()
    }

    private fun updateProgress() {
        // Update task progress bar
        val remainingTasks = totalTasks - completedTasks
        val taskProgress = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

        progressBar.progress = taskProgress
        totalTasksText.text = "Total Tasks: $totalTasks"
        completedTasksText.text = "Completed: $completedTasks"
        remainingTasksText.text = "Remaining: $remainingTasks"

        // Update budget progress
        remainingBudget = totalBudget - spentAmount
        val budgetProgress = if (totalBudget > 0) (spentAmount * 100) / totalBudget else 0

        // Update the progress bar for budget and text
        val budgetProgressBar: ProgressBar = requireView().findViewById(R.id.budgetProgress)  // Corrected view reference
        budgetProgressBar.progress = budgetProgress.toInt() // Ensure it's an Int, not a Double

        totalBudgetText.text = "Total Budget: ₱${totalBudget.toInt()}" // Using toInt() for rounding the value
        spentText.text = "Spent: ₱${spentAmount.toInt()}"  // Using toInt() for rounding the value
        remainingText.text = "Remaining: ₱${remainingBudget.toInt()}"  // Using toInt() for rounding the value
    }

    private fun setupNavigation(view: View) {
        val inviteGuestCard: CardView = view.findViewById(R.id.inviteguest)
        val weddingThemeCard: CardView = view.findViewById(R.id.WeddingTheme)
        val weddingTimelineCard: CardView = view.findViewById(R.id.wedddingtimeline)

        inviteGuestCard.setOnClickListener {
            findNavController().navigate(R.id.action_homePage_to_guestPage)
        }

        weddingThemeCard.setOnClickListener {
            findNavController().navigate(R.id.action_homePage_to_weddingThemePage)
        }

        weddingTimelineCard.setOnClickListener {
            findNavController().navigate(R.id.action_homePage_to_timelinePage)
        }
    }

    // ScheduleAdapter.OnScheduleClickListener implementation
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

        showLoader()
        val user = auth.currentUser
        user?.let {
            db.collection("Users").document(user.uid)
                .collection("Schedules").document(removedSchedule.scheduleId)
                .delete()
                .addOnSuccessListener {
                    scheduleAdapter.notifyItemRemoved(position)
                    // Refresh after deletion
                    fetchSchedulesFromFirestore()
                    hideLoader()
                }
                .addOnFailureListener {
                    hideLoader()
                }
        }
    }

    override fun onScheduleEdit(scheduleItem: ScheduleItem) {
        // Navigate to calendar fragment where editing is implemented
        findNavController().navigate(R.id.action_homePage_to_calendarFragment)
    }

    private fun updateScheduleInFirestore(scheduleItem: ScheduleItem, position: Int) {
        showLoader()
        val user = auth.currentUser
        user?.let {
            db.collection("Users").document(it.uid)
                .collection("Schedules").document(scheduleItem.scheduleId)
                .update("status", scheduleItem.status)
                .addOnSuccessListener {
                    scheduleAdapter.notifyItemChanged(position)

                    // If the current filter would exclude this item, reapply filters
                    if (currentFilter != "All" && currentFilter != scheduleItem.status) {
                        applyFilters()
                    }
                    hideLoader()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update schedule.", Toast.LENGTH_SHORT).show()
                    hideLoader()
                }
        }
    }

    // Loader handling methods
    private fun showLoader() {
        (activity as? MainActivity)?.showLoader()
    }

    private fun hideLoader() {
        (activity as? MainActivity)?.hideLoader()
    }

    // Keep track of concurrent loading operations
    private fun incrementLoadingOperations() {
        loadingOperations++
        showLoader()
    }

    private fun decrementLoadingOperations() {
        loadingOperations--
        if (loadingOperations <= 0) {
            loadingOperations = 0
            hideLoader()
        }
    }
}