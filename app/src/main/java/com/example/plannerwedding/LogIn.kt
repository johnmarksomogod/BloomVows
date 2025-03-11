package com.example.plannerwedding

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LogIn : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpText: TextView
    private lateinit var forgotPasswordText: TextView

    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_log_in, container, false)

        // Set up UI elements
        emailEditText = view.findViewById(R.id.username)
        passwordEditText = view.findViewById(R.id.password)
        loginButton = view.findViewById(R.id.loginButton)
        signUpText = view.findViewById(R.id.textSignUp)
        forgotPasswordText = view.findViewById(R.id.forgotPassword)

        // Login Button Click Listener
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(context, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to SignUp Fragment
        signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signUpFragment)
        }

        // Navigate to Forgot Password Fragment
        forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgotPasswordFragment)
        }

        // Set OnTouchListener on the password field to detect click on the eye icon
        passwordEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = passwordEditText.compoundDrawablesRelative[2]
                if (drawableRight != null) {
                    if (event.rawX >= (passwordEditText.right - drawableRight.bounds.width())) {
                        togglePasswordVisibility()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        return view
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid

                        // Fetch user data from Firestore
                        db.collection("Users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val weddingDate = document.getString("weddingDate")
                                    // Change to getDouble to retrieve the budget as a number
                                    val budget = document.getDouble("budget")  // Corrected this line
                                    val venue = document.getString("venue")

                                    navigateToNextScreen(weddingDate, budget, venue)
                                } else {
                                    Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToNextScreen(weddingDate: String?, budget: Double?, venue: String?) {
        when {
            weddingDate.isNullOrEmpty() -> findNavController().navigate(R.id.action_login_to_enterNamesFragment)
            budget == null -> findNavController().navigate(R.id.action_loginFragment_to_enterBudgetFragment)
            venue.isNullOrEmpty() -> findNavController().navigate(R.id.action_loginFragment_to_enterVenueFragment)
            else -> findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.closeye, 0)
        } else {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.eye, 0)
        }
        passwordEditText.setSelection(passwordEditText.text.length)
        isPasswordVisible = !isPasswordVisible
    }
}
