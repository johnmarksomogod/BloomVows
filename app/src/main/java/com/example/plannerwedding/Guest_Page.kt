package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GuestPage : Fragment() {

    private lateinit var addGuestButton: LinearLayout
    private lateinit var guestRecyclerView: RecyclerView
    private lateinit var guestBackButton: ImageView  // Declare lateinit for guestBackButton
    private val guestList = mutableListOf<Guest>()
    private lateinit var guestAdapter: GuestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guest__page, container, false)

        // Initialize views
        guestBackButton = view.findViewById(R.id.guestBackButton) // Initialize the back button view
        addGuestButton = view.findViewById(R.id.addGuestButton)
        guestRecyclerView = view.findViewById(R.id.guestRecyclerView)

        // Set up RecyclerView
        guestAdapter = GuestAdapter(guestList)
        guestRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        guestRecyclerView.adapter = guestAdapter

        // Handle the back button click (navigate back to the previous screen)
        guestBackButton.setOnClickListener {
            findNavController().navigateUp() // Navigate back to the previous fragment or activity
        }

        // Show GuestDialogFragment when the "add guest" button is clicked
        addGuestButton.setOnClickListener {
            val guestDialog = GuestDialogFragment { guest -> addGuestToList(guest) }
            guestDialog.show(childFragmentManager, "GuestDialogFragment")
        }

        return view
    }

    // Add guest to list and update RecyclerView
    private fun addGuestToList(guest: Guest) {
        guestList.add(guest)
        guestAdapter.notifyDataSetChanged() // Notify adapter to update RecyclerView
    }
}
