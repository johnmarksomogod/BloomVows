package com.example.plannerwedding

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HomePage : Fragment(R.layout.fragment_home_page) {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var daysLeftTextView: TextView
    private lateinit var venueTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var totalTasksText: TextView
    private lateinit var completedTasksText: TextView
    private lateinit var remainingTasksText: TextView
    private lateinit var totalBudgetText: TextView
    private lateinit var spentText: TextView
    private lateinit var remainingText: TextView

    private var totalTasks = 0
    private var completedTasks = 0
    private var totalBudget: Double = 0.0
    private var spentAmount: Double = 0.0
    private var remainingBudget: Double = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        daysLeftTextView = view.findViewById(R.id.DaysLeft)
        venueTextView = view.findViewById(R.id.Venue)
        progressBar = view.findViewById(R.id.todoProgress)
        totalTasksText = view.findViewById(R.id.totalTasks)
        completedTasksText = view.findViewById(R.id.completedTasks)
        remainingTasksText = view.findViewById(R.id.remainingTasks)
        totalBudgetText = view.findViewById(R.id.totalBudgetText)
        spentText = view.findViewById(R.id.spentText)
        remainingText = view.findViewById(R.id.remainingText)

        // Fetch and display wedding data
        fetchWeddingData()

        // Fetch and display task data
        loadTasksFromFirestore()

        // Fetch and display budget data
        loadBudgetDataFromFirestore()

        // CardView navigation
        setupNavigation(view)
    }

    private fun fetchWeddingData() {
        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
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
                }
                .addOnFailureListener {
                    daysLeftTextView.text = "Error loading data"
                    venueTextView.text = "Error loading venue"
                }
        }
    }

    private fun updateDaysLeft(weddingDateStr: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val weddingDate = dateFormat.parse(weddingDateStr)
            val currentDate = Calendar.getInstance().time
            val diff = weddingDate.time - currentDate.time
            val daysLeft = (diff / (1000 * 60 * 60 * 24)).toInt()

            daysLeftTextView.text = "$daysLeft Days until The Big Day"
        } catch (e: Exception) {
            daysLeftTextView.text = "Invalid date format"
        }
    }

    private fun loadTasksFromFirestore() {
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
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadBudgetDataFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Fetch the total budget from Firestore
                    totalBudget = document.getDouble("budget") ?: 0.0
                    spentAmount = 0.0

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
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to load budget items", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load total budget", Toast.LENGTH_SHORT).show()
            }
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
}
