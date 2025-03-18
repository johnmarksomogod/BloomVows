package com.example.plannerwedding

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
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
    ): View {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val view = inflater.inflate(R.layout.fragment_log_in, container, false)

        emailEditText = view.findViewById(R.id.username)
        passwordEditText = view.findViewById(R.id.password)
        loginButton = view.findViewById(R.id.loginButton)
        signUpText = view.findViewById(R.id.textSignUp)
        forgotPasswordText = view.findViewById(R.id.forgotPassword)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                (activity as? MainActivity)?.showLoader()
                loginUser(email, password)
            } else {
                showErrorDialog("Please enter both email and password.")
            }
        }

        signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signUpFragment)
        }

        forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgotPasswordFragment)
        }

        passwordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = passwordEditText.compoundDrawablesRelative[2]
                if (drawableRight != null && event.rawX >= (passwordEditText.right - drawableRight.bounds.width())) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }

        return view
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                (activity as? MainActivity)?.hideLoader()

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        (activity as? MainActivity)?.showLoader()

                        db.collection("Users").document(userId).get()
                            .addOnSuccessListener { document ->
                                (activity as? MainActivity)?.hideLoader()
                                if (document.exists()) {
                                    val weddingDate = document.getString("weddingDate")
                                    val budget = document.getDouble("budget")
                                    val venue = document.getString("venue")

                                    navigateToNextScreen(weddingDate, budget, venue)
                                } else {
                                    showErrorDialog("User data not found.")
                                }
                            }
                            .addOnFailureListener {
                                (activity as? MainActivity)?.hideLoader()
                                showErrorDialog("Something went wrong. Please try again.")
                            }
                    }
                } else {
                    handleLoginError(task.exception)
                }
            }
    }

    private fun handleLoginError(exception: Exception?) {
        (activity as? MainActivity)?.hideLoader()

        val errorMessage = when (exception) {
            is FirebaseAuthInvalidUserException -> "No account found with this email. Please sign up."
            is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
            else -> "Login failed. Please check your email and password."
        }

        showErrorDialog(errorMessage)
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

    private fun showErrorDialog(message: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> }
            .create()

        dialog.setOnShowListener {
            val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.parseColor("#EDABAD")) // ✅ Button color applied
        }

        dialog.show()
    }
}
