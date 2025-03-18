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

class ChangePasswordDialogFragment : DialogFragment() {

    private var onPasswordChangeListener: ((String, String) -> Unit)? = null

    // Track password visibility states
    private var isCurrentPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password_dialog, container, false)

        val currentPasswordInput = view.findViewById<TextInputEditText>(R.id.currentPasswordInput)
        val newPasswordInput = view.findViewById<TextInputEditText>(R.id.newPasswordInput)
        val confirmPasswordInput = view.findViewById<TextInputEditText>(R.id.confirmPasswordInput)

        // Set up password visibility toggle for current password
        currentPasswordInput.setOnTouchListener { v, event ->
            handlePasswordVisibilityToggle(event, currentPasswordInput, isCurrentPasswordVisible) { isVisible ->
                isCurrentPasswordVisible = isVisible
            }
        }

        // Set up password visibility toggle for new password
        newPasswordInput.setOnTouchListener { v, event ->
            handlePasswordVisibilityToggle(event, newPasswordInput, isNewPasswordVisible) { isVisible ->
                isNewPasswordVisible = isVisible
            }
        }

        // Set up password visibility toggle for confirm password
        confirmPasswordInput.setOnTouchListener { v, event ->
            handlePasswordVisibilityToggle(event, confirmPasswordInput, isConfirmPasswordVisible) { isVisible ->
                isConfirmPasswordVisible = isVisible
            }
        }

        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            dismiss()
        }

        val confirmButton = view.findViewById<Button>(R.id.changePasswordButton)
        confirmButton.setOnClickListener {
            val currentPassword = currentPasswordInput.text.toString().trim()
            val newPassword = newPasswordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (validateInputs(currentPassword, newPassword, confirmPassword)) {
                onPasswordChangeListener?.invoke(currentPassword, newPassword)
                dismiss()
            }
        }

        return view
    }

    private fun handlePasswordVisibilityToggle(
        event: MotionEvent,
        editText: TextInputEditText,
        isVisible: Boolean,
        updateVisibility: (Boolean) -> Unit
    ): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val drawableRight = editText.compoundDrawablesRelative[2]
            if (drawableRight != null) {
                if (event.rawX >= (editText.right - drawableRight.bounds.width())) {
                    togglePasswordVisibility(editText, isVisible, updateVisibility)
                    return true
                }
            }
        }
        return false
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun togglePasswordVisibility(
        editText: TextInputEditText,
        isVisible: Boolean,
        updateVisibility: (Boolean) -> Unit
    ) {
        if (isVisible) {
            // Hide password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.closeye, 0)
        } else {
            // Show password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.eye, 0)
        }
        // Maintain cursor position
        editText.setSelection(editText.text?.length ?: 0)
        updateVisibility(!isVisible)
    }

    private fun validateInputs(currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            // Show error message - all fields required
            return false
        }

        if (newPassword != confirmPassword) {
            // Show error message - passwords don't match
            return false
        }

        // Add more validation as needed (password strength, etc.)

        return true
    }

    fun setOnPasswordChangeListener(listener: (currentPassword: String, newPassword: String) -> Unit) {
        onPasswordChangeListener = listener
    }
}