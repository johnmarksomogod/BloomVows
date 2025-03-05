package com.example.plannerwedding

import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_guest__dialog, container, false)

        // Initialize views
        guestName = view.findViewById(R.id.GuestName)
        guestEmail = view.findViewById(R.id.GuestEmail)
        guestSpinner = view.findViewById(R.id.GuestSpinner)
        accommodationCheckbox = view.findViewById(R.id.AccomodationCheckbox)
        guestDoneButton = view.findViewById(R.id.guestdoneButton)
        guestCancelButton = view.findViewById(R.id.guestcancelButton)

        // Set up Spinner (Category selection)
        val categories = arrayOf("Family", "Friends", "Work", "Others")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        guestSpinner.adapter = adapter

        // Handle Done Button Click
        guestDoneButton.setOnClickListener {
            val name = guestName.text.toString().trim()
            val email = guestEmail.text.toString().trim()
            val category = guestSpinner.selectedItem.toString()
            val needsAccommodation = accommodationCheckbox.isChecked

            // Check if the required fields are filled
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create the Guest object and pass it back to GuestPage
            val guest = Guest(name, email, category, needsAccommodation)
            onGuestAdded(guest) // Pass data back
            dismiss()
        }

        // Handle Cancel Button Click
        guestCancelButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    // Override onStart() to adjust the dialog size
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Set width to match parent (full width)
            ViewGroup.LayoutParams.WRAP_CONTENT    // Set height to wrap content (adjustable)
        )
    }
}
