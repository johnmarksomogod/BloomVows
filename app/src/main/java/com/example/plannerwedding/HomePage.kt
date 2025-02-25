package com.example.plannerwedding

import android.os.Bundle
<<<<<<< HEAD
import android.view.View
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
=======
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HomePage : Fragment(R.layout.fragment_home_page) {

<<<<<<< HEAD
    // Declare the views to hold the text and progress bar
    private lateinit var daysLeftTextView: TextView
    private lateinit var venueTextView: TextView
    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var todoProgressBar: ProgressBar
    private lateinit var completedTasksTextView: TextView
    private lateinit var remainingTasksTextView: TextView
    private lateinit var totalTasksTextView: TextView
    private lateinit var inviteGuestCardView: CardView
    private lateinit var weddingTimelineCardView: CardView // Wedding Timeline CardView
=======
    private lateinit var daysLeftTextView: TextView
    private lateinit var venueTextView: TextView
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
    private lateinit var database: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

<<<<<<< HEAD
        // Initialize views
        daysLeftTextView = view.findViewById(R.id.DaysLeft)
        venueTextView = view.findViewById(R.id.Venue)
        budgetProgressBar = view.findViewById(R.id.budgetProgress)
        todoProgressBar = view.findViewById(R.id.todoProgress)
        completedTasksTextView = view.findViewById(R.id.completedTasks)
        remainingTasksTextView = view.findViewById(R.id.remainingTasks)
        totalTasksTextView = view.findViewById(R.id.totalTasks)
        inviteGuestCardView = view.findViewById(R.id.inviteguest)
        weddingTimelineCardView = view.findViewById(R.id.wedddingtimeline) // Initialize the Wedding Timeline CardView

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance("https://wedding-planner-f8418-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("wedding")

        // Retrieve wedding date from Firebase and update Days Left
        database.child("weddingDate").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val weddingDateStr = snapshot.getValue(String::class.java)
                if (weddingDateStr != null) {
                    updateDaysLeft(weddingDateStr)
                } else {
                    daysLeftTextView.text = "No date set"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                daysLeftTextView.text = "Error loading date"
            }
        })

        // Retrieve venue from Firebase
        database.child("venue").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val venueStr = snapshot.getValue(String::class.java)
                if (venueStr != null) {
                    venueTextView.text = "At $venueStr"
                } else {
                    venueTextView.text = "No venue set"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                venueTextView.text = "Error loading venue"
            }
        })

        // Set click listener for the invite guest CardView
        inviteGuestCardView.setOnClickListener {
            // Navigate to the Guest Overview page
            findNavController().navigate(R.id.action_homePage_to_guestPage)
        }

        // Set click listener for the Wedding Timeline CardView
        weddingTimelineCardView.setOnClickListener {
            // Navigate to the Timeline Fragment
            findNavController().navigate(R.id.action_homePage_to_timelinePage)
        }

        // Retrieve budget progress from Firebase (example for dynamic data)
        database.child("budgetProgress").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val progress = snapshot.getValue(Int::class.java) ?: 0
                budgetProgressBar.progress = progress
            }

            override fun onCancelled(error: DatabaseError) {
                budgetProgressBar.progress = 0
            }
        })

        // Retrieve task progress from Firebase (example for dynamic data)
        database.child("tasksCompleted").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val completedTasks = snapshot.getValue(Int::class.java) ?: 0
                completedTasksTextView.text = "Completed: $completedTasks"
            }

            override fun onCancelled(error: DatabaseError) {
                completedTasksTextView.text = "Completed: 0"
            }
        })

        database.child("totalTasks").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalTasks = snapshot.getValue(Int::class.java) ?: 0
                totalTasksTextView.text = "Total Tasks: $totalTasks"
                remainingTasksTextView.text = "Remaining: ${totalTasks - completedTasksTextView.text.split(":")[1].trim().toInt()}"
            }

            override fun onCancelled(error: DatabaseError) {
                remainingTasksTextView.text = "Remaining: 0"
                totalTasksTextView.text = "Total Tasks: 0"
            }
        })
    }

    // Update Days Left from the wedding date
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
=======
        class BudgetFragment : Fragment(R.layout.fragment_budget) {
            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)

                daysLeftTextView = view.findViewById(R.id.DaysLeft)
                venueTextView = view.findViewById(R.id.Venue)

                database =
                    FirebaseDatabase.getInstance("https://wedding-planner-f8418-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("wedding")

                // Retrieve wedding date from Firebase
                database.child("weddingDate")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val weddingDateStr = snapshot.getValue(String::class.java)
                            if (weddingDateStr != null) {
                                Log.d("Firebase", "Wedding Date Fetched: $weddingDateStr")
                                updateDaysLeft(weddingDateStr)
                            } else {
                                daysLeftTextView.text = "No date set"
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            daysLeftTextView.text = "Error loading date"
                            Log.e("Firebase", "Database Error: ${error.message}")
                        }
                    })

                // Retrieve venue from Firebase
                database.child("venue").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val venueStr = snapshot.getValue(String::class.java)
                        if (venueStr != null) {
                            Log.d("Firebase", "Venue Fetched: $venueStr")
                            venueTextView.text = "At $venueStr"
                        } else {
                            venueTextView.text = "No venue set"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        venueTextView.text = "Error loading venue"
                        Log.e("Firebase", "Database Error: ${error.message}")
                    }
                })
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
                    Log.e("Firebase", "Date Parsing Error: ${e.message}")
                }
            }
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
        }
    }
}
