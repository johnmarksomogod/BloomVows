
package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class SIgnUp : Fragment() {
    private lateinit var auth: FirebaseAuth

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
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_s_ign_up, container, false)

        // Set up UI elements
        usernameEditText = view.findViewById(R.id.username)
        firstNameEditText = view.findViewById(R.id.firstName)
        lastNameEditText = view.findViewById(R.id.lastName)
        emailEditText = view.findViewById(R.id.email)
        passwordEditText = view.findViewById(R.id.password)
        signUpButton = view.findViewById(R.id.signUpButton)
        signInText = view.findViewById(R.id.textSignIn)

        // Sign Up Button Click Listener
        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signUpUser(email, password, username, firstName, lastName)
            } else {
                Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }
        // Navigate to SignUp Fragment
        signInText.setOnClickListener {
            // Using NavController for navigation (if you use Navigation Component)
            findNavController().navigate(R.id.action_signUp_to_Login)
        }


        return view
    }

    private fun signUpUser(email: String, password: String, username: String, firstName: String, lastName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Account successfully created
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        val userProfile = UserProfile(username, firstName, lastName, email)

                        val database = FirebaseDatabase.getInstance().getReference("Users")
                        database.child(userId).setValue(userProfile).addOnCompleteListener { databaseTask ->
                            if (databaseTask.isSuccessful) {
                                Toast.makeText(context, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                                // Navigate to login page after sign up
                                val fragment = LogIn()
                                val transaction = parentFragmentManager.beginTransaction()
                                transaction.replace(R.id.logIn_container, fragment)
                                transaction.commit()
                            } else {
                                Toast.makeText(context, "Failed to save user info", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    data class UserProfile(
        val username: String,
        val firstName: String,
        val lastName: String,
        val email: String
    )
}


