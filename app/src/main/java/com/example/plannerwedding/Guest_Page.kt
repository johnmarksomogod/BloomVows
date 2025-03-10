package com.example.plannerwedding

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.fragment.findNavController

class GuestPage : Fragment() {

    private lateinit var addGuestButton: LinearLayout
    private lateinit var guestRecyclerView: RecyclerView
    private lateinit var guestBackButton: ImageView
    private val guestList = mutableListOf<Guest>()
    private lateinit var guestAdapter: GuestAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guest__page, container, false)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        guestBackButton = view.findViewById(R.id.guestBackButton)
        addGuestButton = view.findViewById(R.id.addGuestButton)
        guestRecyclerView = view.findViewById(R.id.guestRecyclerView)

        // Set up RecyclerView
        guestAdapter = GuestAdapter(guestList) { guest, position ->
            showDeleteConfirmationDialog(guest, position) // Show confirmation before deleting
        }
        guestRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        guestRecyclerView.adapter = guestAdapter

        // Handle the back button click (navigate back to the previous screen)
        guestBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Show GuestDialogFragment when the "add guest" button is clicked
        addGuestButton.setOnClickListener {
            val guestDialog = GuestDialogFragment { guest ->
                addGuestToList(guest)
                saveGuestToFirestore(guest) // Save guest to Firestore
            }
            guestDialog.show(childFragmentManager, "GuestDialogFragment")
        }

        loadGuestsFromFirestore()

        return view
    }

    // Add guest to list and update RecyclerView
    private fun addGuestToList(guest: Guest) {
        guestList.add(guest)
        guestAdapter.notifyDataSetChanged() // Notify adapter to update RecyclerView
    }

    // Load the guests from Firestore to show in RecyclerView
    private fun loadGuestsFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val guests = document.get("guests") as? List<Map<String, Any>> ?: mutableListOf()
            guestList.clear()

            // Convert guest data from Firestore format to Guest object
            guests.forEach { guest ->
                val name = guest["name"] as? String ?: ""
                val email = guest["email"] as? String ?: ""
                val category = guest["category"] as? String ?: ""
                val accommodationNeeded = guest["accommodationNeeded"] as? Boolean ?: false

                val newGuest = Guest(name, email, category, accommodationNeeded)
                guestList.add(newGuest)
            }

            guestAdapter.notifyDataSetChanged()
        }
    }

    // Save the guest to Firestore
    private fun saveGuestToFirestore(guest: Guest) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(userId)

        // Fetch the current guest list from Firestore
        userRef.get().addOnSuccessListener { document ->
            val guests = document.get("guests") as? List<Map<String, Any>> ?: mutableListOf()
            val updatedGuests = guests.toMutableList()

            // Add the new guest to the list
            updatedGuests.add(mapOf(
                "name" to guest.name,
                "email" to guest.email,
                "category" to guest.category,
                "accommodationNeeded" to guest.accommodationNeeded
            ))

            // Update Firestore with the new guest list
            userRef.update("guests", updatedGuests)
                .addOnSuccessListener {
                    Toast.makeText(context, "Guest added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to add guest", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Show the delete confirmation dialog
    private fun showDeleteConfirmationDialog(guest: Guest, position: Int) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setMessage("Are you sure you want to delete this guest?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                deleteGuest(guest, position) // Proceed with deleting the guest
            }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }

        val alert = alertDialogBuilder.create()
        alert.show()

        val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveButton.setTextColor(Color.parseColor("#EDABAD"))
        negativeButton.setTextColor(Color.parseColor("#EDABAD"))
    }

    // Delete the guest from Firestore and remove it from the UI
    private fun deleteGuest(guest: Guest, position: Int) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(userId)

        // Fetch current guest list and remove the guest
        userRef.get().addOnSuccessListener { document ->
            val guests = document.get("guests") as? MutableList<Map<String, Any>> ?: mutableListOf()

            // Remove guest from list
            guests.removeAt(position)

            // Update Firestore with the new list
            userRef.update("guests", guests)
                .addOnSuccessListener {
                    Toast.makeText(context, "Guest deleted successfully", Toast.LENGTH_SHORT).show()
                    guestList.removeAt(position) // Remove from UI
                    guestAdapter.notifyItemRemoved(position) // Update RecyclerView
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to delete guest", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
