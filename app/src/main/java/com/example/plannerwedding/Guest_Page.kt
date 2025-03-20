package com.example.plannerwedding

import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import java.text.SimpleDateFormat
import java.util.*

class GuestPage : Fragment() {

    private lateinit var addGuestButton: LinearLayout
    private lateinit var inviteAllButton: Button
    private lateinit var guestRecyclerView: RecyclerView
    private lateinit var guestBackButton: ImageView
    private lateinit var selectionControls: LinearLayout
    private lateinit var selectAllButton: Button
    private lateinit var sendInvitesButton: Button
    private lateinit var selectedCountText: TextView
    private lateinit var guestCountText: TextView
    private lateinit var inviteStatusText: TextView

    private val guestList = mutableListOf<Guest>()
    private lateinit var guestAdapter: GuestAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var selectionModeActive = false
    private var weddingDate: Date? = null
    private var weddingLocation: String = ""
    private var coupleNames: String = ""

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
        inviteAllButton = view.findViewById(R.id.inviteAllButton)
        guestRecyclerView = view.findViewById(R.id.guestRecyclerView)
        selectionControls = view.findViewById(R.id.selectionControls)
        selectAllButton = view.findViewById(R.id.selectAllButton)
        sendInvitesButton = view.findViewById(R.id.sendInvitesButton)
        selectedCountText = view.findViewById(R.id.selectedCountText)
        guestCountText = view.findViewById(R.id.guestCountText)
        inviteStatusText = view.findViewById(R.id.inviteStatusText)

        // Set up RecyclerView with the updated adapter that includes invite functionality
        guestAdapter = GuestAdapter(
            guestList,
            // Delete callback
            { guest, position ->
                if (!selectionModeActive) {
                    showDeleteConfirmationDialog(guest, position)
                }
            },
            // Invite callback
            { guest, position ->
                showSingleInviteConfirmationDialog(guest, position)
            }
        )

        // Setup selection change listener to update count
        guestAdapter.setOnSelectionChangedListener { count ->
            updateSelectedCount(count)
        }

        guestRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        guestRecyclerView.adapter = guestAdapter

        // Load wedding details for invitation
        loadWeddingDetails()

        // Handle the back button click
        guestBackButton.setOnClickListener {
            if (selectionModeActive) {
                toggleSelectionMode(false)
            } else {
                findNavController().navigateUp()
            }
        }

        // Add guest button
        addGuestButton.setOnClickListener {
            val guestDialog = GuestDialogFragment { guest ->
                (activity as? MainActivity)?.showLoader()
                addGuestToList(guest)
                saveGuestToFirestore(guest)
            }
            guestDialog.show(childFragmentManager, "GuestDialogFragment")
        }

        // Invite All button
        inviteAllButton.setOnClickListener {
            val uninvitedGuests = guestList.filter { !it.invited }
            if (uninvitedGuests.isNotEmpty()) {
                showInviteAllConfirmationDialog(uninvitedGuests)
            } else {
                Toast.makeText(context, "All guests have already been invited", Toast.LENGTH_SHORT).show()
            }
        }

        // Selection mode controls
        selectAllButton.setOnClickListener {
            guestAdapter.selectAll()
        }

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

    // Method for inviting a single guest
    private fun showSingleInviteConfirmationDialog(guest: Guest, position: Int) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Send Invitation")
            .setMessage("Send wedding invitation to ${guest.name}?")
            .setCancelable(false)
            .setPositiveButton("Send") { _, _ ->
                (activity as? MainActivity)?.showLoader()
                sendSingleInvitation(guest, position)
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

    // Method for inviting all uninvited guests at once
    private fun showInviteAllConfirmationDialog(uninvitedGuests: List<Guest>) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Send Invitations to All")
            .setMessage("Send wedding invitations to all ${uninvitedGuests.size} uninvited guests?")
            .setCancelable(false)
            .setPositiveButton("Send All") { _, _ ->
                (activity as? MainActivity)?.showLoader()
                sendInvitations(uninvitedGuests)
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

    // Method to send invitation to a single guest and update their status
    private fun sendSingleInvitation(guest: Guest, position: Int) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val allGuests = document.get("guests") as? List<Map<String, Any>> ?: mutableListOf()
            val updatedGuests = allGuests.toMutableList()

            // Update invited status for this guest
            if (position < updatedGuests.size) {
                val updatedGuest = updatedGuests[position].toMutableMap()
                updatedGuest["invited"] = true
                updatedGuests[position] = updatedGuest

                // Update local guest list
                guestList[position] = guest.copy(invited = true)

                // Send email invitation
                sendEmailInvitation(guest)

                // Update Firestore
                userRef.update("guests", updatedGuests)
                    .addOnSuccessListener {
                        (activity as? MainActivity)?.hideLoader()
                        Toast.makeText(context, "Invitation sent to ${guest.name}", Toast.LENGTH_SHORT).show()
                        guestAdapter.notifyItemChanged(position)
                        updateGuestStats()
                    }
                    .addOnFailureListener {
                        (activity as? MainActivity)?.hideLoader()
                        Toast.makeText(context, "Failed to update invitation status", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Load wedding details for invitation
    private fun loadWeddingDetails() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            // Get couple names (bride and groom)
            val bride = document.getString("brideName") ?: ""
            val groom = document.getString("groomName") ?: ""
            coupleNames = "$bride & $groom"

            // Get wedding date
            val weddingDateStr = document.getString("weddingDate")
            if (!weddingDateStr.isNullOrEmpty()) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    weddingDate = dateFormat.parse(weddingDateStr)
                } catch (e: Exception) {
                    // Handle date parsing error
                }
            }

            // Get wedding location
            weddingLocation = document.getString("weddingLocation") ?: ""
        }
    }

    // Toggle selection mode UI
    private fun toggleSelectionMode(enabled: Boolean) {
        selectionModeActive = enabled
        addGuestButton.visibility = if (enabled) View.GONE else View.VISIBLE
        inviteAllButton.visibility = if (enabled) View.GONE else View.VISIBLE
        selectionControls.visibility = if (enabled) View.VISIBLE else View.GONE
        selectedCountText.visibility = if (enabled) View.VISIBLE else View.GONE
        guestAdapter.toggleSelectionMode(enabled)

        if (!enabled) {
            updateSelectedCount(0)
        } else {
            updateSelectedCount(guestAdapter.getSelectedCount())
        }

        if (enabled) {
            Toast.makeText(context, "Selection mode activated", Toast.LENGTH_SHORT).show()
        }
    }

    // Update the selected count text
    private fun updateSelectedCount(count: Int) {
        selectedCountText.text = "$count guest${if (count != 1) "s" else ""} selected"
    }

    // Update the guest count and invitation status
    private fun updateGuestStats() {
        val totalGuests = guestList.size
        val invitedGuests = guestList.count { it.invited }

        guestCountText.text = "Total Guests: $totalGuests"
        inviteStatusText.text = "Invited: $invitedGuests"
    }

    // Show invitation confirmation dialog
    private fun showInviteConfirmationDialog(selectedGuests: List<Guest>) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Send Invitations")
            .setMessage("Are you sure you want to send invitations to ${selectedGuests.size} guests?")
            .setCancelable(false)
            .setPositiveButton("Send") { _, _ ->
                (activity as? MainActivity)?.showLoader()
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

    private fun sendEmailInvitation(guest: Guest) {
        if (guest.email.isNullOrBlank()) {
            Toast.makeText(context, "No email found for ${guest.name}", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dateStr = weddingDate?.let {
                SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(it)
            } ?: "Our special day"

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(guest.email))
                putExtra(Intent.EXTRA_SUBJECT, "Wedding Invitation - $coupleNames")
                putExtra(Intent.EXTRA_TEXT, """
                ✨ ♥ ✨ ♥ ✨ ♥ ✨ ♥ ✨ ♥ ✨ ♥ ✨
                
                WEDDING INVITATION
                
                Dear ${guest.name},
                
                With joy in our hearts, we invite you to share in our happiness
                as we unite in marriage and begin our journey together.
                
                ${coupleNames.uppercase()}
                
                REQUEST THE PLEASURE OF YOUR COMPANY
                ON
                ${dateStr.uppercase()}
                AT
                ${weddingLocation.uppercase()}
                
                ${if (guest.accommodationNeeded) "We have noted your accommodation needs and will be in touch with details soon." else ""}
                
                RSVP by replying to this email
                
                We look forward to celebrating this special day with you!
                
                With love and excitement,
                ${coupleNames}
                
                ✨ ♥ ✨ ♥ ✨ ♥ ✨ ♥ ✨ ♥ ✨ ♥ ✨
            """.trimIndent())
            }

            if (emailIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(Intent.createChooser(emailIntent, "Send invitation using..."))
            } else {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send email: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendInvitations(guests: List<Guest>) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val allGuests = document.get("guests") as? List<Map<String, Any>> ?: mutableListOf()
            val updatedGuests = allGuests.toMutableList()

            for (guest in guests) {
                if (!guest.email.isNullOrBlank()) { // Ensure the guest has a valid email
                    val index = guestList.indexOf(guest)
                    if (index != -1 && index < updatedGuests.size) {
                        val updatedGuest = updatedGuests[index].toMutableMap()
                        updatedGuest["invited"] = true
                        updatedGuests[index] = updatedGuest

                        guestList[index] = guest.copy(invited = true)
                        sendEmailInvitation(guest) // Send invitation
                    }
                }
            }

            userRef.update("guests", updatedGuests)
                .addOnSuccessListener {
                    (activity as? MainActivity)?.hideLoader()
                    Toast.makeText(context, "Invitations sent successfully!", Toast.LENGTH_SHORT).show()
                    if (selectionModeActive) {
                        toggleSelectionMode(false) // Exit selection mode if active
                    }
                    guestAdapter.notifyDataSetChanged()
                    updateGuestStats()
                }
                .addOnFailureListener {
                    (activity as? MainActivity)?.hideLoader()
                    Toast.makeText(context, "Failed to update invitation status", Toast.LENGTH_SHORT).show()
                }
        }

    }

    // Add guest to list and update RecyclerView
    private fun addGuestToList(guest: Guest) {
        guestList.add(guest)
        guestAdapter.notifyDataSetChanged() // Notify adapter to update RecyclerView
        updateGuestStats() // Update guest statistics
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
                val rsvpStatus = guest["rsvpStatus"] as? String ?: "Not Responded"

                val newGuest = Guest(name, email, category, accommodationNeeded, invited, rsvpStatus)
                guestList.add(newGuest)
            }

            guestAdapter.notifyDataSetChanged()
            updateGuestStats() // Update guest statistics

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
                "invited" to guest.invited,
                "rsvpStatus" to guest.rsvpStatus
            ))

            // Update Firestore with the new guest list
            userRef.update("guests", updatedGuests)
                .addOnSuccessListener {
                    // Hide loader after success
                    (activity as? MainActivity)?.hideLoader()
                    Toast.makeText(context, "Guest added successfully", Toast.LENGTH_SHORT).show()
                    updateGuestStats() // Update guest statistics
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
                        updateGuestStats() // Update guest statistics
                    }
                    .addOnFailureListener {
                        // Hide loader after failure
                        (activity as? MainActivity)?.hideLoader()
                        Toast.makeText(context, "Failed to delete guest", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Override onResume to ensure the UI is in the correct state
    override fun onResume() {
        super.onResume()
        // If coming back to this fragment, make sure we're not in selection mode
        if (selectionModeActive) {
            toggleSelectionMode(false)
        }
    }
}