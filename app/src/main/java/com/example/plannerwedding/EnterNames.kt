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

class EnterNames : Fragment() {
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
                    if (document.exists() && document.contains("brideName") && document.contains("groomName")) {
                        // Names already exist, navigate to the next screen
                        findNavController().navigate(R.id.action_enterNames_to_enterDate)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error checking names", Toast.LENGTH_SHORT).show()
                }
        }

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_enter_names, container, false)

        val brideNameEditText = view.findViewById<EditText>(R.id.brideNameInput)
        val groomNameEditText = view.findViewById<EditText>(R.id.groomNameInput)
        val nextButton = view.findViewById<Button>(R.id.nextButton)

        nextButton.setOnClickListener {
            val brideName = brideNameEditText.text.toString().trim()
            val groomName = groomNameEditText.text.toString().trim()

            if (brideName.isNotEmpty() && groomName.isNotEmpty() && user != null) {
                val userId = user.uid
                val userData = mapOf(
                    "brideName" to brideName,
                    "groomName" to groomName
                )

                // Save the names in Firestore under the current user's document
                db.collection("Users").document(userId)
                    .update(userData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Names saved successfully!", Toast.LENGTH_SHORT).show()
                        // Navigate to the next screen
                        findNavController().navigate(R.id.action_enterNames_to_enterDate)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save names", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Please enter both names", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
