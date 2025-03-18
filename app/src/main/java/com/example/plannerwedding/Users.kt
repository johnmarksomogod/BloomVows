package com.example.plannerwedding

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class Users : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var profileImageView: ImageView
    private lateinit var editPhotoButton: ImageView
    private lateinit var editNameButton: ImageView
    private lateinit var profileNameTextView: TextView
    private lateinit var weddingDateTextView: TextView
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_users, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize image-related views
        profileImageView = view.findViewById(R.id.profile_image)
        editPhotoButton = view.findViewById(R.id.edit_photo_button)
        editNameButton = view.findViewById(R.id.edit_name_button)
        profileNameTextView = view.findViewById(R.id.profile_name)
        weddingDateTextView = view.findViewById(R.id.WeddingDateTextView)

        // Set up image selection
        profileImageView.setOnClickListener {
            openImagePicker()
        }

        editPhotoButton.setOnClickListener {
            openImagePicker()
        }

        // Set up name editing
        editNameButton.setOnClickListener {
            showEditNamesDialog()
        }

        // Wedding Budget Section
        val weddingBudget = view.findViewById<LinearLayout>(R.id.WeddingBudget)
        weddingBudget.setOnClickListener {
            val userId = auth.currentUser?.uid
            userId?.let {
                showLoader()
                db.collection("Users").document(it).get()
                    .addOnSuccessListener { document ->
                        hideLoader()
                        val budget = document.getDouble("budget")?.toString() ?: "No Budget"
                        showEditDialog("Wedding Budget", budget, "budget")
                    }
                    .addOnFailureListener {
                        hideLoader()
                        Toast.makeText(requireContext(), "Failed to load budget data", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Wedding Date Section
        val weddingDate = view.findViewById<LinearLayout>(R.id.WeddingDate)
        weddingDate.setOnClickListener {
            val userId = auth.currentUser?.uid
            userId?.let {
                showLoader()
                db.collection("Users").document(it).get()
                    .addOnSuccessListener { document ->
                        hideLoader()
                        val date = document.getString("weddingDate") ?: "No Date"
                        showEditDialog("Wedding Date", date, "weddingDate")
                    }
                    .addOnFailureListener {
                        hideLoader()
                        Toast.makeText(requireContext(), "Failed to load date data", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Wedding Venue Section
        val weddingVenue = view.findViewById<LinearLayout>(R.id.WeddingVenue)
        weddingVenue.setOnClickListener {
            val userId = auth.currentUser?.uid
            userId?.let {
                showLoader()
                db.collection("Users").document(it).get()
                    .addOnSuccessListener { document ->
                        hideLoader()
                        val venue = document.getString("venue") ?: "No Venue"
                        showEditDialog("Wedding Venue", venue, "venue")
                    }
                    .addOnFailureListener {
                        hideLoader()
                        Toast.makeText(requireContext(), "Failed to load venue data", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Change Password Section
        val changePassword = view.findViewById<LinearLayout>(R.id.ChangePassword)
        changePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Delete Wedding Section
        val deleteWedding = view.findViewById<LinearLayout>(R.id.DeleteWedding)
        deleteWedding.setOnClickListener {
            showDeleteWeddingConfirmationDialog()
        }

        // Delete Account Section
        val deleteAccount = view.findViewById<LinearLayout>(R.id.DeleteAccount)
        deleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        // Log Out Section
        val logOut = view.findViewById<LinearLayout>(R.id.LogOut)
        logOut.setOnClickListener {
            signOut()
        }

        // Load data from Firestore
        showLoader() // Show loader before loading data
        loadUserData()

        return view
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            // Display the selected image
            Glide.with(this)
                .load(selectedImageUri)
                .centerCrop()
                .into(profileImageView)

            // Upload the image to Firebase Storage
            uploadImageToFirebase()
        }
    }

    private fun uploadImageToFirebase() {
        if (selectedImageUri != null) {
            val userId = auth.currentUser?.uid ?: return
            val storageRef = storage.reference.child("profile_images/$userId.jpg")

            // Show progress using the main loader instead of a separate dialog
            showLoader()

            storageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    // Get the download URL
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Save image URL to user profile in Firestore
                        db.collection("Users").document(userId)
                            .update("profileImageUrl", uri.toString())
                            .addOnSuccessListener {
                                hideLoader()
                                Toast.makeText(
                                    requireContext(),
                                    "Profile image updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                hideLoader()
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to update profile: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    hideLoader()
                    Toast.makeText(
                        requireContext(),
                        "Failed to upload image: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showEditNamesDialog() {
        val dialogView = layoutInflater.inflate(R.layout.fragment_edit_names_dialog, null)
        val groomNameInput = dialogView.findViewById<EditText>(R.id.groomNameInput)
        val brideNameInput = dialogView.findViewById<EditText>(R.id.brideNameInput)
        val cancelButton = dialogView.findViewById<TextView>(R.id.UserCancelButton)
        val saveButton = dialogView.findViewById<TextView>(R.id.UserSaveButton)

        // Get current names
        val userId = auth.currentUser?.uid
        userId?.let {
            showLoader()
            db.collection("Users").document(it).get()
                .addOnSuccessListener { document ->
                    hideLoader()
                    groomNameInput.setText(document.getString("groomName") ?: "")
                    brideNameInput.setText(document.getString("brideName") ?: "")
                }
                .addOnFailureListener { e ->
                    hideLoader()
                    Toast.makeText(requireContext(), "Failed to load name data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set up button listeners
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val groomName = groomNameInput.text.toString().trim()
            val brideName = brideNameInput.text.toString().trim()

            if (groomName.isEmpty() || brideName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both names", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Update names in Firestore
            userId?.let {
                showLoader()
                db.collection("Users").document(it)
                    .update(
                        mapOf(
                            "groomName" to groomName,
                            "brideName" to brideName
                        )
                    )
                    .addOnSuccessListener {
                        hideLoader()
                        dialog.dismiss()
                        profileNameTextView.text = "$brideName & $groomName"
                        Toast.makeText(
                            requireContext(),
                            "Names updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        hideLoader()
                        Toast.makeText(
                            requireContext(),
                            "Failed to update names: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

        dialog.show()
    }

    private fun showEditDialog(sectionTitle: String, currentValue: String, firestoreField: String) {
        val dialog = EditSectionDialogFragment(sectionTitle, currentValue)
        dialog.setOnSaveListener { newValue ->
            updateFirestore(firestoreField, newValue)

            // Notify HomePage to refetch the wedding date from Firestore
            if (firestoreField == "weddingDate") {
                parentFragmentManager.setFragmentResult("WEDDING_DATE_UPDATED", Bundle())
            }
        }
        dialog.show(parentFragmentManager, "EditSectionDialog")
    }

    private fun updateFirestore(field: String, value: String) {
        val userId = auth.currentUser?.uid
        userId?.let {
            // Show loader before updating data
            showLoader()

            // Convert value to Double if it's a budget field
            val dataToUpdate = if (field == "budget") {
                try {
                    mapOf(field to value.toDouble())
                } catch (e: NumberFormatException) {
                    hideLoader()
                    Toast.makeText(
                        requireContext(),
                        "Please enter a valid number for budget",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            } else {
                mapOf(field to value)
            }

            db.collection("Users").document(it)
                .update(dataToUpdate)
                .addOnSuccessListener {
                    // Reload user data to update UI
                    loadUserData() // This already has loader handling inside
                    Toast.makeText(requireContext(), "Update successful", Toast.LENGTH_SHORT).show()

                    // If we're updating wedding date, try to notify HomePage
                    if (field == "weddingDate") {
                        parentFragmentManager.setFragmentResult("WEDDING_DATE_UPDATED", Bundle())
                    }
                }
                .addOnFailureListener { e ->
                    hideLoader()
                    Toast.makeText(
                        requireContext(),
                        "Update failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showChangePasswordDialog() {
        val passwordDialog = ChangePasswordDialogFragment()
        passwordDialog.show(parentFragmentManager, "ChangePasswordDialog")
    }

    private fun showDeleteWeddingConfirmationDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Wedding")
            .setMessage("Are you sure you want to delete all your wedding information? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteWeddingData() }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val deleteButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val cancelButton: Button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            deleteButton.setTextColor(Color.parseColor("#EDABAD"))
            cancelButton.setTextColor(Color.parseColor("#EDABAD"))
        }

        dialog.show()
    }

    private fun deleteWeddingData() {
        val userId = auth.currentUser?.uid
        userId?.let {
            showLoader()
            db.collection("Users").document(it)
                .update(
                    mapOf(
                        "weddingDate" to null,
                        "budget" to null,
                        "venue" to null
                    )
                )
                .addOnSuccessListener {
                    hideLoader()
                    Toast.makeText(
                        requireContext(),
                        "Wedding data deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_userFragmaent_to_Login)
                }
                .addOnFailureListener { e ->
                    hideLoader()
                    Toast.makeText(
                        requireContext(),
                        "Failed to delete wedding data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showDeleteAccountConfirmationDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> showReauthenticateDialog() }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val deleteButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val cancelButton: Button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            deleteButton.setTextColor(Color.parseColor("#EDABAD"))
            cancelButton.setTextColor(Color.parseColor("#EDABAD"))
        }

        dialog.show()
    }

    private fun showReauthenticateDialog() {
        val passwordDialog = ReauthenticateDialogFragment()
        passwordDialog.setOnReauthenticateListener { password ->
            reauthenticateAndDeleteAccount(password)
        }
        passwordDialog.show(parentFragmentManager, "ReauthenticateDialog")
    }

    private fun reauthenticateAndDeleteAccount(password: String) {
        val user = auth.currentUser
        user?.let {
            showLoader()
            val credential = EmailAuthProvider.getCredential(it.email ?: "", password)
            it.reauthenticate(credential)
                .addOnSuccessListener {
                    deleteUserAccount()
                }
                .addOnFailureListener { e ->
                    hideLoader()
                    Toast.makeText(
                        requireContext(),
                        "Authentication failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun deleteUserAccount() {
        val userId = auth.currentUser?.uid
        userId?.let {
            // First delete user data from Firestore
            db.collection("Users").document(it)
                .delete()
                .addOnSuccessListener {
                    // Delete profile image from storage if exists
                    storage.reference.child("profile_images/$userId.jpg").delete()

                    // Then delete the user account
                    auth.currentUser?.delete()
                        ?.addOnSuccessListener {
                            hideLoader()
                            Toast.makeText(
                                requireContext(),
                                "Account deleted successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToLogin()
                        }
                        ?.addOnFailureListener { e ->
                            hideLoader()
                            Toast.makeText(
                                requireContext(),
                                "Failed to delete account: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    hideLoader()
                    Toast.makeText(
                        requireContext(),
                        "Failed to delete user data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_userFragmaent_to_Login)
    }

    private fun signOut() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                findNavController().navigate(R.id.action_userFragmaent_to_Login)
            }
            .setNegativeButton("No", null)
            .create()

        dialog.setOnShowListener {
            val yesButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val noButton: Button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            yesButton.setTextColor(Color.parseColor("#EDABAD"))
            noButton.setTextColor(Color.parseColor("#EDABAD"))
        }

        dialog.show()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        userId?.let {
            showLoader() // Show loader before loading data
            db.collection("Users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Retrieve and update names
                        val brideName = document.getString("brideName") ?: ""
                        val groomName = document.getString("groomName") ?: ""
                        if (brideName.isNotEmpty() && groomName.isNotEmpty()) {
                            profileNameTextView.text = "$brideName & $groomName"
                        }

                        // Retrieve and update profile image if exists
                        val profileImageUrl = document.getString("profileImageUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .centerCrop()
                                .into(profileImageView)
                        }

                        // Retrieve and update wedding date
                        val weddingDate = document.getString("weddingDate") ?: "No Date"
                        weddingDateTextView.text = weddingDate

                        // Retrieve and update wedding budget
                        val budget = document.getDouble("budget")?.toString() ?: "No Budget"
                        view?.findViewById<TextView>(R.id.WeddingBudgetTextView)?.text = budget

                        // Retrieve and update wedding venue
                        val venue = document.getString("venue") ?: "No Venue"
                        view?.findViewById<TextView>(R.id.WeddingVenueTextView)?.text = venue
                    }
                    hideLoader() // Hide loader after data is loaded
                }
                .addOnFailureListener {
                    hideLoader() // Hide loader if there's an error
                    Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    // Show loader method that uses the MainActivity's loader
    private fun showLoader() {
        (activity as? MainActivity)?.showLoader()
    }

    // Hide loader method that uses the MainActivity's loader
    private fun hideLoader() {
        (activity as? MainActivity)?.hideLoader()
    }
}