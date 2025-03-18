package com.example.plannerwedding

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var sendResetButton: Button
    private lateinit var backButton: ImageView  // Changed from ImageButton to ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        // Initialize UI components
        emailEditText = view.findViewById(R.id.emailEditText)
        sendResetButton = view.findViewById(R.id.sendResetButton)
        backButton = view.findViewById(R.id.backButton)  // This now refers to an ImageView

        // Set up click listener for send reset button
        sendResetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                showErrorDialog("Please enter your email address.")
                return@setOnClickListener
            }

            sendPasswordResetEmail(email)
        }

        // Set up click listener for back button
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    private fun sendPasswordResetEmail(email: String) {
        // Show loader
        (activity as? MainActivity)?.showLoader()

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                // Hide loader
                (activity as? MainActivity)?.hideLoader()

                if (task.isSuccessful) {
                    showSuccessDialog("Password reset link has been sent to your email.")
                    // Navigate back to login after successful reset email
                    findNavController().navigateUp()
                } else {
                    showErrorDialog(task.exception?.message ?: "Failed to send password reset email. Please try again.")
                }
            }
    }

    private fun showErrorDialog(message: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> }
            .create()

        dialog.setOnShowListener {
            val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.parseColor("#EDABAD")) // Button color applied
        }

        dialog.show()
    }

    private fun showSuccessDialog(message: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> }
            .create()

        dialog.setOnShowListener {
            val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.parseColor("#EDABAD")) // Button color applied
        }

        dialog.show()
    }
}