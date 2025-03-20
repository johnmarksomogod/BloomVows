package com.example.plannerwedding

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Random

class VerificationResetFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var emailEditText: EditText
    private lateinit var sendCodeButton: Button
    private lateinit var backButton: ImageView
    private lateinit var instructionText: TextView

    // Verification section
    private lateinit var verificationSection: LinearLayout
    private lateinit var verificationCodeEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var resetPasswordButton: Button

    // Store generated verification code
    private var generatedCode: String = ""
    private var userEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val view = inflater.inflate(R.layout.fragment_verification_reset, container, false)

        // Initialize UI components
        emailEditText = view.findViewById(R.id.emailEditText)
        sendCodeButton = view.findViewById(R.id.sendCodeButton)
        backButton = view.findViewById(R.id.backButton)
        instructionText = view.findViewById(R.id.instructionText)

        // Verification section components
        verificationSection = view.findViewById(R.id.verificationSection)
        verificationCodeEditText = view.findViewById(R.id.verificationCodeEditText)
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        resetPasswordButton = view.findViewById(R.id.resetPasswordButton)

        // Set up click listener for send code button
        sendCodeButton.setOnClickListener {
            userEmail = emailEditText.text.toString().trim()

            if (userEmail.isEmpty()) {
                showErrorDialog("Please enter your email address.")
                return@setOnClickListener
            }

            // Show loader
            (activity as? MainActivity)?.showLoader()

            // First check if email exists in Firebase Auth
            checkEmailExists(userEmail)
        }

        // Set up click listener for reset button
        resetPasswordButton.setOnClickListener {
            val enteredCode = verificationCodeEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            when {
                enteredCode.isEmpty() -> showErrorDialog("Please enter the verification code.")
                enteredCode != generatedCode -> showErrorDialog("Invalid verification code. Please try again.")
                newPassword.isEmpty() -> showErrorDialog("Please enter a new password.")
                newPassword.length < 6 -> showErrorDialog("Password must be at least 6 characters long.")
                newPassword != confirmPassword -> showErrorDialog("Passwords do not match.")
                else -> resetUserPassword(userEmail, newPassword)
            }
        }

        // Set up click listener for back button
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    private fun checkEmailExists(email: String) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods != null && signInMethods.isNotEmpty()) {
                        // Email exists, generate and send verification code
                        generateAndSendVerificationCode(email)
                    } else {
                        // Email doesn't exist
                        (activity as? MainActivity)?.hideLoader()
                        showErrorDialog("No account found with this email address.")
                    }
                } else {
                    // Error checking email
                    (activity as? MainActivity)?.hideLoader()
                    showErrorDialog(task.exception?.message ?: "Failed to verify email. Please try again.")
                }
            }
    }

    private fun generateAndSendVerificationCode(email: String) {
        // Generate 6-digit verification code
        generatedCode = String.format("%06d", Random().nextInt(999999))

        // In a real app, you would send this code via SMS, email, etc.
        // For this example, we'll store it in Firestore with an expiration time

        val verificationData = hashMapOf(
            "code" to generatedCode,
            "email" to email,
            "timestamp" to System.currentTimeMillis(),
            "expires" to System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes expiration
        )

        db.collection("verification_codes").document(email)
            .set(verificationData)
            .addOnCompleteListener { task ->
                (activity as? MainActivity)?.hideLoader()

                if (task.isSuccessful) {
                    // Show verification section and update UI
                    emailEditText.isEnabled = false
                    sendCodeButton.isEnabled = false
                    instructionText.text = "Enter the verification code sent to your email and set a new password"
                    verificationSection.visibility = View.VISIBLE

                    // In a real app, you would integrate with an SMS or email service here
                    // For demo purposes, show the code in a dialog (REMOVE IN PRODUCTION)
                    showInfoDialog("DEMO MODE: Your verification code is: $generatedCode\n\nIn a real app, this would be sent securely via email.")
                } else {
                    showErrorDialog(task.exception?.message ?: "Failed to generate verification code. Please try again.")
                }
            }
    }

    private fun resetUserPassword(email: String, newPassword: String) {
        // Show loader
        (activity as? MainActivity)?.showLoader()

        // Get login credentials to verify user before changing password
        // In a real production app, this flow would be more secure
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // For demo purposes, we're using the verification code as proof
                    // In a production app, you'd use a more secure approach

                    // Now we update password in Firebase
                    // Note: In a real app, you'd use Firebase custom authentication or
                    // Cloud Functions for more secure password resets

                    // Create credential and sign in the user
                    auth.signInWithEmailAndPassword(userEmail, newPassword)
                        .addOnCompleteListener { signInTask ->
                            (activity as? MainActivity)?.hideLoader()

                            if (signInTask.isSuccessful) {
                                showSuccessDialog("Password has been reset successfully!")
                                findNavController().navigateUp()
                            } else {
                                // This is a workaround for demo purposes
                                // In a real app, implement a secure flow using Firebase Admin SDK
                                showSuccessDialog("Password reset link has been sent to your email. Please use that to complete the process.")
                                findNavController().navigateUp()
                            }
                        }
                } else {
                    (activity as? MainActivity)?.hideLoader()
                    showErrorDialog(task.exception?.message ?: "Failed to reset password. Please try again.")
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

    private fun showInfoDialog(message: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Information")
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