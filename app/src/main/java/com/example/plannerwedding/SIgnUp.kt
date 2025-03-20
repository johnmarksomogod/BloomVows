package com.example.plannerwedding

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var usernameEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var signInText: TextView

    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val view = inflater.inflate(R.layout.fragment_s_ign_up, container, false)

        usernameEditText = view.findViewById(R.id.username)
        firstNameEditText = view.findViewById(R.id.firstName)
        lastNameEditText = view.findViewById(R.id.lastName)
        emailEditText = view.findViewById(R.id.email)
        passwordEditText = view.findViewById(R.id.password)
        signUpButton = view.findViewById(R.id.signUpButton)
        signInText = view.findViewById(R.id.textSignIn)

        passwordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                if (event.rawX >= (passwordEditText.right - passwordEditText.compoundDrawables[drawableEnd].bounds.width())) {
                    isPasswordVisible = !isPasswordVisible
                    passwordEditText.inputType = if (isPasswordVisible) {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    passwordEditText.setSelection(passwordEditText.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }

        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (isValidEmail(email)) {
                    checkEmailExistenceAndSignUp(email, password, username, firstName, lastName)
                } else {
                    showAlertDialog("Sign Up Failed", "Please enter a valid email address.")
                }
            } else {
                showAlertDialog("Sign Up Failed", "Please fill in all fields.")
            }
        }

        signInText.setOnClickListener {
            findNavController().navigate(R.id.action_signUp_to_Login)
        }

        return view
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun checkEmailExistenceAndSignUp(email: String, password: String, username: String, firstName: String, lastName: String) {
        (activity as? MainActivity)?.showLoader()

        // Check if email already exists in Firestore
        db.collection("Users").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Email doesn't exist in Firestore, proceed with sign up
                    signUpUser(email, password, username, firstName, lastName)
                } else {
                    // Email already exists in Firestore
                    (activity as? MainActivity)?.hideLoader()
                    showAlertDialog("Sign Up Failed", "This email is already registered. Please sign in instead or use a different email.")
                }
            }
            .addOnFailureListener { e ->
                (activity as? MainActivity)?.hideLoader()
                showAlertDialog("Error", "Failed to check email existence: ${e.message}")
            }
    }

    private fun signUpUser(email: String, password: String, username: String, firstName: String, lastName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                (activity as? MainActivity)?.hideLoader()
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        val userProfile = UserProfile(username, firstName, lastName, email, userId)

                        db.collection("Users").document(userId).set(userProfile)
                            .addOnSuccessListener {
                                showAlertDialog("Sign Up Successful", "Your account has been created.") {
                                    findNavController().navigate(R.id.action_signUp_to_Login)
                                }
                            }
                            .addOnFailureListener { e ->
                                showAlertDialog("Error", "Failed to save user info: ${e.message}")
                            }
                    }
                } else {
                    val errorMessage = when {
                        task.exception?.message?.contains("email address is already in use") == true ->
                            "This email is already registered with Firebase Authentication. Please sign in instead."
                        task.exception?.message?.contains("password is invalid") == true ->
                            "Password should be at least 6 characters."
                        else -> task.exception?.message ?: "Unknown error occurred."
                    }
                    showAlertDialog("Sign Up Failed", errorMessage)
                }
            }
    }

    private fun showAlertDialog(title: String, message: String, onDismiss: (() -> Unit)? = null) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onDismiss?.invoke()
            }
            .create()

        dialog.setOnShowListener {
            val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setTextColor(Color.parseColor("#EDABAD"))
        }

        dialog.show()
    }

    data class UserProfile(
        val username: String,
        val firstName: String,
        val lastName: String,
        val email: String,
        val userId: String
    )
}