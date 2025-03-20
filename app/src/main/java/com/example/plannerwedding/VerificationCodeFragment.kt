package com.example.plannerwedding

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class VerificationCodeFragment : Fragment() {
    private lateinit var digit1: EditText
    private lateinit var digit2: EditText
    private lateinit var digit3: EditText
    private lateinit var digit4: EditText
    private lateinit var digit5: EditText
    private lateinit var digit6: EditText
    private lateinit var verifyCodeButton: Button
    private lateinit var resendCodeText: TextView
    private lateinit var backButton: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_verification_code, container, false)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        digit1 = view.findViewById(R.id.digit1)
        digit2 = view.findViewById(R.id.digit2)
        digit3 = view.findViewById(R.id.digit3)
        digit4 = view.findViewById(R.id.digit4)
        digit5 = view.findViewById(R.id.digit5)
        digit6 = view.findViewById(R.id.digit6)
        verifyCodeButton = view.findViewById(R.id.verifyCodeButton)
        resendCodeText = view.findViewById(R.id.resendCodeText)
        backButton = view.findViewById(R.id.backButton)

        // Set up digit input focus changes
        setupDigitInputs()

        // Set click listeners
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        verifyCodeButton.setOnClickListener {
            val code = digit1.text.toString() + digit2.text.toString() +
                    digit3.text.toString() + digit4.text.toString() +
                    digit5.text.toString() + digit6.text.toString()

            if (code.length == 6) {
                verifyCode(code)
            } else {
                showErrorDialog("Please enter the complete verification code.")
            }
        }

        resendCodeText.setOnClickListener {
            resendVerificationCode()
        }

        return view
    }

    private fun setupDigitInputs() {
        // Set up auto-focus for verification code input
        val digits = listOf(digit1, digit2, digit3, digit4, digit5, digit6)

        for (i in 0 until digits.size - 1) {
            digits[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        digits[i + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun verifyCode(code: String) {
        (activity as? MainActivity)?.showLoader()

        // Get the stored verification code from shared preferences
        val sharedPref = requireActivity().getSharedPreferences("PasswordReset", 0)
        val storedCode = sharedPref.getString("verification_code", "")
        val codeTimestamp = sharedPref.getLong("code_timestamp", 0)
        val currentTime = System.currentTimeMillis()

        // Check if code is expired (10 minutes expiration)
        val tenMinutesInMillis = 10 * 60 * 1000

        if (currentTime - codeTimestamp > tenMinutesInMillis) {
            (activity as? MainActivity)?.hideLoader()
            showErrorDialog("Verification code has expired. Please request a new code.")
            return
        }

        // Verify the entered code against the stored code
        if (code == storedCode) {
            (activity as? MainActivity)?.hideLoader()
            // Navigate to new password screen
            findNavController().navigate(R.id.action_verificationCodeFragment_to_newPasswordFragment)
        } else {
            (activity as? MainActivity)?.hideLoader()
            showErrorDialog("Incorrect verification code. Please try again.")
        }
    }

    private fun resendVerificationCode() {
        // Get stored email from shared preferences
        val sharedPref = requireActivity().getSharedPreferences("PasswordReset", 0)
        val email = sharedPref.getString("reset_email", "")

        if (!email.isNullOrEmpty()) {
            (activity as? MainActivity)?.showLoader()

            // Generate a new verification code
            val newVerificationCode = generateRandomCode()

            // Store the new code and timestamp
            with(sharedPref.edit()) {
                putString("verification_code", newVerificationCode)
                putLong("code_timestamp", System.currentTimeMillis())
                apply()
            }

            // Resend verification email
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    (activity as? MainActivity)?.hideLoader()
                    if (task.isSuccessful) {
                        showSuccessDialog("Verification code resent. Please check your email.")
                    } else {
                        showErrorDialog("Failed to resend verification code. Please try again.")
                    }
                }
        } else {
            showErrorDialog("Email address not found. Please go back and try again.")
        }
    }

    private fun generateRandomCode(): String {
        // Generate a random 6-digit code
        return (100000..999999).random().toString()
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
