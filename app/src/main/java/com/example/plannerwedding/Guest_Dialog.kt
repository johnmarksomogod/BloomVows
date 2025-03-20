package com.example.plannerwedding

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment

class GuestDialogFragment(private val onGuestAdded: (Guest) -> Unit) : DialogFragment() {

    private lateinit var guestName: EditText
    private lateinit var guestEmail: EditText
    private lateinit var guestSpinner: Spinner
    private lateinit var accommodationCheckbox: CheckBox
    private lateinit var guestDoneButton: TextView
    private lateinit var guestCancelButton: TextView
    private lateinit var emailError: TextView // Fixed missing reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_guest__dialog, container, false)

        // Initialize views
        guestName = view.findViewById(R.id.GuestName)
        guestEmail = view.findViewById(R.id.GuestEmail)
        guestSpinner = view.findViewById(R.id.GuestSpinner)
        accommodationCheckbox = view.findViewById(R.id.AccommodationCheckbox)
        guestDoneButton = view.findViewById(R.id.guestdoneButton)
        guestCancelButton = view.findViewById(R.id.guestcancelButton)
        emailError = view.findViewById(R.id.emailError) // Fixed missing view reference

        // Set up Spinner (Category selection)
        val categories = arrayOf(
            "Family - Bride Side", "Family - Groom Side", "Friends", "Work Colleagues",
            "Bridesmaids", "Groomsmen", "Flower Girl", "Ring Bearer", "Officiant",
            "Maid of Honor", "Best Man", "Parents", "Others"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        guestSpinner.adapter = adapter

        // Email validation on focus change
        guestEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateEmail()
            }
        }

        // Handle Done Button Click
        guestDoneButton.setOnClickListener {
            val name = guestName.text.toString().trim()
            val email = guestEmail.text.toString().trim()
            val category = guestSpinner.selectedItem.toString()
            val needsAccommodation = accommodationCheckbox.isChecked

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!validateEmail()) {
                return@setOnClickListener
            }

            val guest = Guest(name, email, category, needsAccommodation)
            onGuestAdded(guest)
            dismiss()
        }

        // Handle Cancel Button Click
        guestCancelButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    // Validate email format
    private fun validateEmail(): Boolean {
        val email = guestEmail.text.toString().trim()

        if (email.isEmpty()) {
            emailError.text = "Email is required"
            emailError.visibility = View.VISIBLE
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.text = "Please enter a valid email address"
            emailError.visibility = View.VISIBLE
            return false
        }

        emailError.visibility = View.GONE
        return true
    }

    // Override onStart() to adjust the dialog size
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
