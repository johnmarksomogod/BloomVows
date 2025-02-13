package com.example.plannerwedding

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HomePage : Fragment(R.layout.fragment_home_page) {

    private lateinit var daysLeftTextView: TextView
    private lateinit var venueTextView: TextView
    private lateinit var database: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        }
    }
}
