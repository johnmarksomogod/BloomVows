package com.example.plannerwedding

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.FirebaseDatabase

class EnterVenue : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_enter_venue, container, false)

        val venueEditText = view.findViewById<EditText>(R.id.venueNameEditText)
        val submitButton = view.findViewById<Button>(R.id.submitButton)

        submitButton.setOnClickListener {
            val venueName = venueEditText.text.toString().trim()

            if (venueName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a venue name", Toast.LENGTH_SHORT).show()
            } else {
                val database =
                    FirebaseDatabase.getInstance("https://wedding-planner-f8418-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("wedding")

                database.child("venue").setValue(venueName)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Venue saved: $venueName")
                        findNavController().navigate(R.id.action_enterVenue_to_homePage)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save venue", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        return view
    }
}
