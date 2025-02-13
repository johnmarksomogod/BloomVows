package com.example.plannerwedding

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var sendResetButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        // Set up UI elements
        emailEditText = view.findViewById(R.id.emailEditText)
        sendResetButton = view.findViewById(R.id.sendResetButton)

        // Send password reset email
        sendResetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (!TextUtils.isEmpty(email)) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(context, "Please enter your email address", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Password reset link sent to your email", Toast.LENGTH_LONG).show()
                    // Use the back button to navigate back to the login screen
                    activity?.onBackPressed()  // This will take the user back to the previous screen (LogIn)
                } else {
                    Toast.makeText(context, "Failed to send reset link: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
