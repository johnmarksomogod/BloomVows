package com.example.plannerwedding

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

/**
 * ForgotPasswordFragment - Handles the first step of the password reset process
 * where the user enters their email address to receive a verification code
 */
class ForgotPasswordFragment : Fragment() {
    private lateinit var emailEditText: EditText
    private lateinit var sendCodeButton: Button
    private lateinit var backButton: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val TAG = "ForgotPasswordFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        emailEditText = view.findViewById(R.id.emailEditText)
        sendCodeButton = view.findViewById(R.id.sendCodeButton)
        backButton = view.findViewById(R.id.backButton)

        // Set click listeners
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        sendCodeButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty() && isValidEmail(email)) {
                sendVerificationCode(email)
            } else {
                showErrorDialog("Please enter a valid email address.")
            }
        }

        return view
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun sendVerificationCode(email: String) {
        (activity as? MainActivity)?.showLoader()

        // Generate a 6-digit verification code
        val verificationCode = generateVerificationCode()

        // Store the email and verification code in SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("PasswordReset", 0)
        with(sharedPref.edit()) {
            putString("reset_email", email)
            putString("verification_code", verificationCode)
            putLong("code_timestamp", System.currentTimeMillis())
            apply()
        }

        // Check if the user exists in Firestore before sending the code
        db.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // User doesn't exist
                    (activity as? MainActivity)?.hideLoader()
                    showErrorDialog("No account found with this email address.")
                } else {
                    // User exists, send verification code
                    // In a real app, you'd send the actual code via email or SMS
                    // For this implementation, we'll use Firebase's password reset email
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            (activity as? MainActivity)?.hideLoader()
                            if (task.isSuccessful) {
                                Log.d(TAG, "Password reset email sent successfully")
                                // For demonstration, we'll show the verification code in the success dialog
                                // In a real app, you would NOT show this to the user
                                showSuccessDialog("Verification code sent to your email.\n\nFor testing purposes, your code is: $verificationCode")

                                // Navigate to the verification code fragment
                                findNavController().navigate(R.id.action_forgotPasswordFragment_to_verificationCodeFragment)
                            } else {
                                // Handle errors
                                val exception = task.exception
                                Log.e(TAG, "Error sending reset email", exception)
                                val errorMessage = when (exception) {
                                    is FirebaseAuthInvalidUserException -> "No account found with this email address."
                                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                                    else -> "Failed to send verification code. Please try again."
                                }
                                showErrorDialog(errorMessage)
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                (activity as? MainActivity)?.hideLoader()
                Log.e(TAG, "Error checking user existence", exception)
                showErrorDialog("An error occurred. Please try again.")
            }
    }

    private fun generateVerificationCode(): String {
        // Generate a 6-digit random code
        return (100000 + (Math.random() * 900000).toInt()).toString()
    }

    private fun showErrorDialog(message: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> }
            .create()

        dialog.setOnShowListener {
            val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.parseColor("#EDABAD"))
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
            positiveButton.setTextColor(Color.parseColor("#EDABAD"))
        }

        dialog.show()
    }
}
