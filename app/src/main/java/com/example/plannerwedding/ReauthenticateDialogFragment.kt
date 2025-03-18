package com.example.plannerwedding

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText

class ReauthenticateDialogFragment : DialogFragment() {

    private var onReauthenticateListener: ((String) -> Unit)? = null
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Reusing the password dialog layout with customizations
        val view = inflater.inflate(R.layout.fragment_change_password_dialog, container, false)

        // Update the title
        val titleTextView = view.findViewById<TextView>(R.id.dialogTitle)
        titleTextView.text = "Confirm Password"

        // Hide the new password and confirm password fields
        val newPasswordLayout = view.findViewById<View>(R.id.newPasswordLayout)
        val confirmPasswordLayout = view.findViewById<View>(R.id.confirmPasswordLayout)
        newPasswordLayout.visibility = View.GONE
        confirmPasswordLayout.visibility = View.GONE

        // Set up the current password field with visibility toggle
        val currentPasswordInput = view.findViewById<TextInputEditText>(R.id.currentPasswordInput)
        currentPasswordInput.hint = "Enter your password"

        // Set up password visibility toggle
        currentPasswordInput.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = currentPasswordInput.compoundDrawablesRelative[2]
                if (drawableRight != null) {
                    if (event.rawX >= (currentPasswordInput.right - drawableRight.bounds.width())) {
                        togglePasswordVisibility(currentPasswordInput)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        // Update the button text
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            dismiss()
        }

        val confirmButton = view.findViewById<Button>(R.id.changePasswordButton)
        confirmButton.text = "Confirm"
        confirmButton.setOnClickListener {
            val password = currentPasswordInput.text.toString().trim()
            if (password.isNotEmpty()) {
                onReauthenticateListener?.invoke(password)
                dismiss()
            }
        }

        return view
    }

    private fun togglePasswordVisibility(passwordEditText: TextInputEditText) {
        if (isPasswordVisible) {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.closeye, 0)
        } else {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.eye, 0)
        }
        // Maintain cursor position
        passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
        isPasswordVisible = !isPasswordVisible
    }

    fun setOnReauthenticateListener(listener: (String) -> Unit) {
        onReauthenticateListener = listener
    }
}