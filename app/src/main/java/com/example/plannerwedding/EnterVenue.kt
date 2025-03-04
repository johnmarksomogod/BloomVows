package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EnterVenue : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists() && document.contains("venue")) {
                        // Venue already exists, navigate to HomePage
                        findNavController().navigate(R.id.action_enterVenue_to_homePage)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error checking venue", Toast.LENGTH_SHORT).show()
                }
        }

        val view = inflater.inflate(R.layout.fragment_enter_venue, container, false)

        val venueEditText = view.findViewById<EditText>(R.id.venueNameEditText)
        val submitButton = view.findViewById<Button>(R.id.submitButton)

        submitButton.setOnClickListener {
            val venueName = venueEditText.text.toString().trim()
            if (venueName.isEmpty() && user != null) {
                Toast.makeText(requireContext(), "Please enter a venue name", Toast.LENGTH_SHORT).show()
            } else if (user != null) {
                val userId = user.uid
                db.collection("Users").document(userId)
                    .update("venue", venueName)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Venue saved successfully!", Toast.LENGTH_SHORT).show()
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