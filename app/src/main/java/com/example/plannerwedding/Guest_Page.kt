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
    private lateinit var inviteGuestButton: LinearLayout
    private lateinit var guestRecyclerView: RecyclerView
    private lateinit var guestBackButton: ImageView
    private lateinit var selectionControls: LinearLayout
    private lateinit var selectAllButton: Button
    private lateinit var sendInvitesButton: Button
    private lateinit var selectedCountText: TextView

    private val guestList = mutableListOf<Guest>()
    private lateinit var guestAdapter: GuestAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var selectionModeActive = false

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
        inviteGuestButton = view.findViewById(R.id.inviteGuestButton)
        guestRecyclerView = view.findViewById(R.id.guestRecyclerView)
        selectionControls = view.findViewById(R.id.selectionControls)
        selectAllButton = view.findViewById(R.id.selectAllButton)
        sendInvitesButton = view.findViewById(R.id.sendInvitesButton)
        selectedCountText = view.findViewById(R.id.selectedCountText)

        // Set up RecyclerView
        guestAdapter = GuestAdapter(guestList) { guest, position ->
            if (!selectionModeActive) {
                // Only allow deletion when not in selection mode
                showDeleteConfirmationDialog(guest, position)
            }
        }

        // Setup selection change listener to update count
        guestAdapter.setOnSelectionChangedListener { count ->
            updateSelectedCount(count)
        }

        guestRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        guestRecyclerView.adapter = guestAdapter

        // Handle the back button click (navigate back to the previous screen)
        guestBackButton.setOnClickListener {
            // If in selection mode, exit selection mode
            if (selectionModeActive) {
                toggleSelectionMode(false)
            } else {
                findNavController().navigateUp()
            }
        }

        // Show GuestDialogFragment when the "add guest" button is clicked
        addGuestButton.setOnClickListener {
            val guestDialog = GuestDialogFragment { guest ->
                // Show loader before adding guest
                (activity as? MainActivity)?.showLoader()
                addGuestToList(guest)
                saveGuestToFirestore(guest) // Save guest to Firestore
            }
            guestDialog.show(childFragmentManager, "GuestDialogFragment")
        }

        // Enter selection mode when invite button is clicked
        inviteGuestButton.setOnClickListener {
            toggleSelectionMode(true)
        }

        // Select all guests
        selectAllButton.setOnClickListener {
            guestAdapter.selectAll()
        }

        // Send invites to selected guests
        sendInvitesButton.setOnClickListener {
            val selectedGuests = guestAdapter.getSelectedGuests()
            if (selectedGuests.isNotEmpty()) {
                showInviteConfirmationDialog(selectedGuests)
            } else {
                Toast.makeText(context, "No guests selected", Toast.LENGTH_SHORT).show()
            }
        }

        // Show loader before loading guests
        (activity as? MainActivity)?.showLoader()
        loadGuestsFromFirestore()

        return view
    }

    // Toggle selection mode UI
    private fun toggleSelectionMode(enabled: Boolean) {
        selectionModeActive = enabled

        // Show/hide appropriate controls
        addGuestButton.visibility = if (enabled) View.GONE else View.VISIBLE
        inviteGuestButton.visibility = if (enabled) View.GONE else View.VISIBLE
        selectionControls.visibility = if (enabled) View.VISIBLE else View.GONE
        selectedCountText.visibility = if (enabled) View.VISIBLE else View.GONE

        // Update adapter selection mode
        guestAdapter.toggleSelectionMode(enabled)

        // If disabling, reset count
        if (!enabled) {
            updateSelectedCount(0)
        }
    }

    // Update the selected count text
    private fun updateSelectedCount(count: Int) {
        selectedCountText.text = "$count guest${if (count != 1) "s" else ""} selected"
    }

    // Show invitation confirmation dialog
    private fun showInviteConfirmationDialog(selectedGuests: List<Guest>) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Send Invitations")
            .setMessage("Are you sure you want to send invitations to ${selectedGuests.size} guests?")
            .setCancelable(false)
            .setPositiveButton("Send") { _, _ ->
                // Show loader before sending invitations
                (activity as? MainActivity)?.showLoader()
                // Here you would implement sending invitations to the selected guests
                sendInvitations(selectedGuests)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        val alert = alertDialogBuilder.create()
        alert.show()
        alert.window?.setBackgroundDrawableResource(android.R.color.white)

        val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveButton.setTextColor(Color.parseColor("#EDABAD"))
        negativeButton.setTextColor(Color.parseColor("#EDABAD"))
    }

    // Send invitations logic (to be implemented based on your requirements)
    private fun sendInvitations(guests: List<Guest>) {
        // Here you would implement your logic to send invitations to guests
        // This could include sending emails, texts, or updating Firebase status

        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(userId)

        // Example: Update invited status in Firestore
        userRef.get().addOnSuccessListener { document ->
            val allGuests = document.get("guests") as? List<Map<String, Any>> ?: mutableListOf()
            val updatedGuests = allGuests.toMutableList()

            // Update invited status for selected guests
            for (guest in guests) {
                val index = guestList.indexOf(guest)
                if (index != -1 && index < updatedGuests.size) {
                    val updatedGuest = updatedGuests[index].toMutableMap()
                    updatedGuest["invited"] = true
                    updatedGuests[index] = updatedGuest
                }
            }

            // Update Firestore
            userRef.update("guests", updatedGuests)
                .addOnSuccessListener {
                    // Hide loader after success
                    (activity as? MainActivity)?.hideLoader()
                    Toast.makeText(context, "Invitations sent successfully!", Toast.LENGTH_SHORT).show()
                    toggleSelectionMode(false) // Exit selection mode
                }
                .addOnFailureListener {
                    // Hide loader after failure
                    (activity as? MainActivity)?.hideLoader()
                    Toast.makeText(context, "Failed to send invitations", Toast.LENGTH_SHORT).show()
                }
        }
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
                val invited = guest["invited"] as? Boolean ?: false

                val newGuest = Guest(name, email, category, accommodationNeeded, invited)
                guestList.add(newGuest)
            }

            guestAdapter.notifyDataSetChanged()

            // Hide loader after data is loaded
            (activity as? MainActivity)?.hideLoader()
        }.addOnFailureListener {
            // Hide loader if there's an error
            (activity as? MainActivity)?.hideLoader()
            Toast.makeText(context, "Failed to load guests", Toast.LENGTH_SHORT).show()
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
                "accommodationNeeded" to guest.accommodationNeeded,
                "invited" to guest.invited
            ))

            // Update Firestore with the new guest list
            userRef.update("guests", updatedGuests)
                .addOnSuccessListener {
                    // Hide loader after success
                    (activity as? MainActivity)?.hideLoader()
                    Toast.makeText(context, "Guest added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Hide loader after failure
                    (activity as? MainActivity)?.hideLoader()
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
                // Show loader before deleting
                (activity as? MainActivity)?.showLoader()
                deleteGuest(guest, position) // Proceed with deleting the guest
            }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }

        val alert = alertDialogBuilder.create()
        alert.show()
        alert.window?.setBackgroundDrawableResource(android.R.color.white)

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
            if (position < guests.size) {
                guests.removeAt(position)

                // Update Firestore with the new list
                userRef.update("guests", guests)
                    .addOnSuccessListener {
                        // Hide loader after success
                        (activity as? MainActivity)?.hideLoader()
                        Toast.makeText(context, "Guest deleted successfully", Toast.LENGTH_SHORT).show()
                        guestList.removeAt(position) // Remove from UI
                        guestAdapter.notifyItemRemoved(position) // Update RecyclerView
                    }
                    .addOnFailureListener {
                        // Hide loader after failure
                        (activity as? MainActivity)?.hideLoader()
                        Toast.makeText(context, "Failed to delete guest", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}