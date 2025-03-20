package com.example.plannerwedding

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NewPasswordFragment : Fragment() {
    private lateinit var newPasswordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var savePasswordButton: Button
    private lateinit var backButton: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val TAG = "NewPasswordFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_password, container, false)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        newPasswordLayout = view.findViewById(R.id.newPasswordLayout)
        confirmPasswordLayout = view.findViewById(R.id.confirmPasswordLayout)
        savePasswordButton = view.findViewById(R.id.savePasswordButton)
        backButton = view.findViewById(R.id.backButton)

        // Set click listeners
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        savePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (validatePasswords(newPassword, confirmPassword)) {
                resetPassword(newPassword)
            }
        }

        return view
    }

    private fun validatePasswords(newPassword: String, confirmPassword: String): Boolean {
        // Reset previous errors
        newPasswordLayout.error = null
        confirmPasswordLayout.error = null

        // Check if passwords are empty
        if (newPassword.isEmpty()) {
            newPasswordLayout.error = "Please enter a new password"
            return false
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.error = "Please confirm your new password"
            return false
        }

        // Check if passwords match
        if (newPassword != confirmPassword) {
            confirmPasswordLayout.error = "Passwords do not match"
            return false
        }

        // Check password length
        if (newPassword.length < 6) {
            newPasswordLayout.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    private fun resetPassword(newPassword: String) {
        (activity as? MainActivity)?.showLoader()

        // Get stored email from shared preferences
        val sharedPref = requireActivity().getSharedPreferences("PasswordReset", 0)
        val email = sharedPref.getString("reset_email", "")

        if (email.isNullOrEmpty()) {
            (activity as? MainActivity)?.hideLoader()
            showErrorDialog("Email not found. Please restart the password reset process.")
            return
        }

        // First, try to authenticate the current user
        val currentUser = auth.currentUser

        if (currentUser != null && currentUser.email == email) {
            // User is already signed in, update password directly
            currentUser.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Password updated successfully for authenticated user")
                        updatePasswordInDatabase(email, newPassword)
                    } else {
                        handlePasswordUpdateError(task.exception)
                    }
                }
        } else {
            // User is not signed in, need to sign in with email first
            // We'll use a special verification token stored in SharedPreferences
            val verificationToken = sharedPref.getString("verification_code", "")
            if (verificationToken.isNullOrEmpty()) {
                (activity as? MainActivity)?.hideLoader()
                showErrorDialog("Verification information missing. Please restart the process.")
                return
            }

            // In a real app, you'd have a backend endpoint to validate the token
            // For this demo, we'll simulate a successful backend verification
            // and update the password in the database
            updatePasswordInDatabase(email, newPassword)
        }
    }

    private fun updatePasswordInDatabase(email: String, newPassword: String) {
        // Find the user in Firestore
        db.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    (activity as? MainActivity)?.hideLoader()
                    showErrorDialog("User not found in database.")
                    return@addOnSuccessListener
                }

                // Get the user document reference
                val userDoc = querySnapshot.documents[0].reference

                // Update the password in the database
                // In a real app, you'd hash the password before storing
                userDoc.update("password", newPassword)
                    .addOnSuccessListener {
                        Log.d(TAG, "Password updated in database")
                        (activity as? MainActivity)?.hideLoader()
                        showSuccessDialog("Your password has been updated successfully!")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error updating password in database", e)
                        (activity as? MainActivity)?.hideLoader()
                        showErrorDialog("Failed to update password in database.")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error finding user in database", e)
                (activity as? MainActivity)?.hideLoader()
                showErrorDialog("Error finding user in database.")
            }
    }

    private fun handlePasswordUpdateError(exception: Exception?) {
        (activity as? MainActivity)?.hideLoader()
        Log.e(TAG, "Password update error", exception)

        val errorMessage = when {
            exception?.message?.contains("requires recent authentication") == true ->
                "For security reasons, please sign in again before changing your password."

            else -> "Failed to update password. Please try again."
        }

        showErrorDialog(errorMessage)
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
            .setPositiveButton("OK") { _, _ ->
                // Clear stored password reset data
                val sharedPref = requireActivity().getSharedPreferences("PasswordReset", 0)
                with(sharedPref.edit()) {
                    clear()
                    apply()
                }

                // Navigate back to login
                findNavController().navigate(R.id.action_newPasswordFragment_to_loginFragment)
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.parseColor("#EDABAD"))
        }

        dialog.setCancelable(false)
        dialog.show()
    }
}
