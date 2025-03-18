package com.example.plannerwedding

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
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

        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                try {
                    (activity as? MainActivity)?.showLoader()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                signUpUser(email, password, username, firstName, lastName)
            } else {
                showAlertDialog("Sign Up Failed", "Please fill in all fields.")
            }
        }

        signInText.setOnClickListener {
            findNavController().navigate(R.id.action_signUp_to_Login)
        }

        return view
    }

    private fun signUpUser(email: String, password: String, username: String, firstName: String, lastName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                try {
                    (activity as? MainActivity)?.hideLoader()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

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
                    showAlertDialog("Sign Up Failed", task.exception?.message ?: "Unknown error occurred.")
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
