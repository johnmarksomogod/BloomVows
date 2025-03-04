package com.example.plannerwedding

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class Users : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_users, container, false)

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("WeddingPrefs", 0)

        // Wedding Budget Section
        val weddingBudget = view.findViewById<LinearLayout>(R.id.WeddingBudget)
        weddingBudget.setOnClickListener {
            val dialog = EditSectionDialogFragment(
                "Wedding Budget", sharedPreferences.getString("totalBudget", "No Budget") ?: "No Budget"
            )
            dialog.setOnSaveListener { newValue ->
                updateUI("Wedding Budget", newValue)
                saveToSharedPreferences("totalBudget", newValue)
            }
            dialog.show(parentFragmentManager, "EditWeddingBudgetDialog")
        }

        // Wedding Date Section
        val weddingDate = view.findViewById<LinearLayout>(R.id.WeddingDate)
        weddingDate.setOnClickListener {
            val dialog = EditSectionDialogFragment(
                "Wedding Date", sharedPreferences.getString("weddingDate", "No Date") ?: "No Date"
            )
            dialog.setOnSaveListener { newValue ->
                updateUI("Wedding Date", newValue)
                saveToSharedPreferences("weddingDate", newValue)
            }
            dialog.show(parentFragmentManager, "EditWeddingDateDialog")
        }

        // Wedding Venue Section
        val weddingVenue = view.findViewById<LinearLayout>(R.id.WeddingVenue)
        weddingVenue.setOnClickListener {
            val dialog = EditSectionDialogFragment(
                "Wedding Venue", sharedPreferences.getString("venue", "No Venue") ?: "No Venue"
            )
            dialog.setOnSaveListener { newValue ->
                updateUI("Wedding Venue", newValue)
                saveToSharedPreferences("venue", newValue)
            }
            dialog.show(parentFragmentManager, "EditWeddingVenueDialog")
        }

        // Load saved data into the views
        loadSavedData(view)

        return view
    }

    private fun saveToSharedPreferences(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun loadSavedData(view: View) {
        // Load saved data from SharedPreferences
        val weddingDate = sharedPreferences.getString("weddingDate", "No date set")
        val weddingBudget = sharedPreferences.getString("totalBudget", "No budget set")
        val weddingVenue = sharedPreferences.getString("venue", "No venue set")

        // Set the values to the TextViews
        val weddingDateTextView = view.findViewById<TextView>(R.id.WeddingDateTextView)
        val weddingBudgetTextView = view.findViewById<TextView>(R.id.WeddingBudgetTextView)
        val weddingVenueTextView = view.findViewById<TextView>(R.id.WeddingVenueTextView)

        weddingDateTextView.text = weddingDate
        weddingBudgetTextView.text = weddingBudget
        weddingVenueTextView.text = weddingVenue
    }

    private fun updateUI(sectionTitle: String, newValue: String) {
        when (sectionTitle) {
            "Wedding Budget" -> {
                val weddingBudgetTextView = view?.findViewById<TextView>(R.id.WeddingBudgetTextView)
                weddingBudgetTextView?.text = newValue
            }
            "Wedding Date" -> {
                val weddingDateTextView = view?.findViewById<TextView>(R.id.WeddingDateTextView)
                weddingDateTextView?.text = newValue
            }
            "Wedding Venue" -> {
                val weddingVenueTextView = view?.findViewById<TextView>(R.id.WeddingVenueTextView)
                weddingVenueTextView?.text = newValue
            }
        }
    }
}
