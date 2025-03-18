package com.example.plannerwedding

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide

class WeddingThemeFragment : Fragment(R.layout.fragment_wedding_theme) {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // UI components that need to be accessed throughout the class
    private lateinit var customizedWeddingThemeContainer: LinearLayout
    private lateinit var customizedThemeImage: ImageView
    private lateinit var customizedThemeName: TextView
    private lateinit var customizedThemeDescription: TextView
    private lateinit var customizedThemeColorPalette: LinearLayout

    // Theme selection buttons
    private lateinit var useRusticThemeButton: Button
    private lateinit var useGardenThemeButton: Button
    private lateinit var useGlamThemeButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI components
        initializeViews(view)

        // Set click listeners
        setupClickListeners()

        // Load existing theme data if available
        loadExistingThemeData()
    }

    private fun initializeViews(view: View) {
        // Main UI elements
        customizedWeddingThemeContainer = view.findViewById(R.id.customizedWeddingThemeContainer)
        customizedThemeImage = view.findViewById(R.id.customizedThemeImage)
        customizedThemeName = view.findViewById(R.id.customizedThemeName)
        customizedThemeDescription = view.findViewById(R.id.customizedThemeDescription)
        customizedThemeColorPalette = view.findViewById(R.id.customizedThemeColorPalette)

        // Buttons
        useRusticThemeButton = view.findViewById(R.id.useRusticThemeButton)
        useGardenThemeButton = view.findViewById(R.id.useGardenThemeButton)
        useGlamThemeButton = view.findViewById(R.id.useGlamThemeButton)
    }

    private fun setupClickListeners() {
        // Customize button click listener
        val customizeButton: LinearLayout = requireView().findViewById(R.id.Customizebttn)
        customizeButton.setOnClickListener {
            showCustomizationDialog()
        }

        // Back button click listener
        val backButton: ImageView = requireView().findViewById(R.id.WeddingThemeBackbttn)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Theme selection button listeners
        useRusticThemeButton.setOnClickListener {
            showConfirmationDialog("Rustic Elegance",
                "A beautiful blend of natural elements with elegant touches. Features wooden details, burlap accents, and soft floral arrangements.",
                null,
                listOf(
                    getColor(R.color.rustic_brown_dark),
                    getColor(R.color.rustic_brown_medium),
                    getColor(R.color.rustic_beige),
                    getColor(R.color.rustic_cream),
                    getColor(R.color.rustic_brown_deep)
                )
            )
        }

        useGardenThemeButton.setOnClickListener {
            showConfirmationDialog("Romantic Garden",
                "An enchanting garden-inspired theme with lush greenery, delicate flowers, and soft pastel hues. Perfect for outdoor or spring weddings.",
                null,
                listOf(
                    getColor(R.color.garden_green_light),
                    getColor(R.color.garden_pink_light),
                    getColor(R.color.garden_green_pale),
                    getColor(R.color.garden_pink_medium),
                    getColor(R.color.garden_green_medium)
                )
            )
        }

        useGlamThemeButton.setOnClickListener {
            showConfirmationDialog("Elegant Glam",
                "A luxurious and glamorous theme featuring metallic accents, crystal details, and rich colors. Perfect for evening celebrations and formal venues.",
                null,
                listOf(
                    getColor(R.color.glam_silver),
                    getColor(R.color.glam_light_gray),
                    getColor(R.color.glam_gold),
                    getColor(R.color.glam_dark_gray),
                    getColor(R.color.glam_platinum)
                )
            )
        }
    }

    private fun getColor(colorResId: Int): Int {
        return requireContext().getColor(colorResId)
    }

    private fun showConfirmationDialog(themeName: String, themeDescription: String, imageUri: String?, colorPalette: List<Int>) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Set Wedding Theme")
            .setMessage("Would you like to use the '$themeName' theme for your wedding?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                (activity as? MainActivity)?.showLoader()
                selectPredefinedTheme(themeName, themeDescription, imageUri, colorPalette)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()

        // Set button colors
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        positiveButton.setTextColor(Color.parseColor("#EDABAD"))
        negativeButton.setTextColor(Color.parseColor("#EDABAD"))
    }

    private fun selectPredefinedTheme(themeName: String, themeDescription: String, imageUri: String?, colorPalette: List<Int>) {
        // Convert the drawable resource to a URI
        val imageUriString = imageUri ?: when (themeName) {
            "Rustic Elegance" -> "android.resource://${requireContext().packageName}/drawable/rustic"
            "Romantic Garden" -> "android.resource://${requireContext().packageName}/drawable/garden"
            "Elegant Glam" -> "android.resource://${requireContext().packageName}/drawable/elegant"
            else -> null // Default case, if needed
        }

        // Save selection to Firebase first
        saveCustomizationToFirestore(themeName, themeDescription, imageUriString, colorPalette)
    }

    private fun showCustomizationDialog() {
        // Create and show the dialog
        CustomizationDialogFragment { themeName, themeDescription, imageUri, colorPalette ->
            // After customization is done, show loader and save to Firestore
            (activity as? MainActivity)?.showLoader()
            saveCustomizationToFirestore(themeName, themeDescription, imageUri, colorPalette)
        }.show(childFragmentManager, "CustomizationDialog")
    }

    private fun updateWeddingThemePage(imageUri: String?, themeName: String, themeDescription: String, colorPalette: List<Int>) {
        // Update theme name and description
        customizedThemeName.text = themeName
        customizedThemeDescription.text = themeDescription

        // Handle image setting with null safety
        if (!imageUri.isNullOrEmpty()) {
            try {
                // Use Glide for efficient image loading and caching
                Glide.with(requireContext())
                    .load(Uri.parse(imageUri))
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(customizedThemeImage)
            } catch (e: Exception) {
                // Fallback to default image if there's an error
                customizedThemeImage.setImageResource(R.drawable.placeholder_image)
                e.printStackTrace()
            }
        } else {
            // Use a default image if no image is selected
            customizedThemeImage.setImageResource(R.drawable.placeholder_image)
        }

        // Add selected colors to the color palette
        customizedThemeColorPalette.removeAllViews()

        // Create and add color circles to the palette
        val layoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.color_circle_size),
            resources.getDimensionPixelSize(R.dimen.color_circle_size)
        ).apply {
            marginEnd = resources.getDimensionPixelSize(R.dimen.color_circle_margin)
        }

        colorPalette.forEach { color ->
            val colorView = View(requireContext()).apply {
                background = resources.getDrawable(R.drawable.circle_shape, null)
                backgroundTintList = android.content.res.ColorStateList.valueOf(color)
                this.layoutParams = layoutParams
            }
            customizedThemeColorPalette.addView(colorView)
        }

        // Show the customized wedding theme container
        customizedWeddingThemeContainer.visibility = View.VISIBLE

        // Hide loader after UI is updated
        (activity as? MainActivity)?.hideLoader()
    }

    private fun saveCustomizationToFirestore(themeName: String, themeDescription: String, imageUri: String?, colorPalette: List<Int>) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            (activity as? MainActivity)?.hideLoader()
            Toast.makeText(requireContext(), "Please sign in to save your theme", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = db.collection("Users").document(userId)

        // Convert color integers to longs for Firestore compatibility
        val colorLongList = colorPalette.map { it.toLong() }

        val themeData = hashMapOf(
            "themeName" to themeName,
            "themeDescription" to themeDescription,
            "imageUri" to (imageUri ?: ""),
            "colorPalette" to colorLongList,
            "lastUpdated" to com.google.firebase.Timestamp.now()
        )

        // Update Firestore with the new theme data
        userRef.update("weddingTheme", themeData)
            .addOnSuccessListener {
                // After save is successful, load the theme data to update UI
                loadExistingThemeData()
                Toast.makeText(requireContext(), "Wedding Theme saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // If update fails, the document might not exist, so try to set it instead
                userRef.set(hashMapOf("weddingTheme" to themeData))
                    .addOnSuccessListener {
                        // After save is successful, load the theme data to update UI
                        loadExistingThemeData()
                        Toast.makeText(requireContext(), "Wedding Theme saved successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { innerError ->
                        (activity as? MainActivity)?.hideLoader()
                        Toast.makeText(requireContext(), "Failed to save wedding theme: ${innerError.message}", Toast.LENGTH_SHORT).show()
                        innerError.printStackTrace()
                    }
            }
    }

    private fun loadExistingThemeData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val weddingTheme = document.get("weddingTheme") as? Map<String, Any>

                    if (weddingTheme != null) {
                        val themeName = weddingTheme["themeName"] as? String ?: "Your Custom Theme"
                        val themeDescription = weddingTheme["themeDescription"] as? String ?:
                        "Your personalized wedding theme will appear here after customization."
                        val imageUri = weddingTheme["imageUri"] as? String

                        // Convert the color palette back to Int list
                        val colorPaletteLongs = weddingTheme["colorPalette"] as? List<Long> ?: listOf()
                        val colorPaletteInts = colorPaletteLongs.map { it.toInt() }

                        // Update UI with saved theme data
                        updateWeddingThemePage(imageUri, themeName, themeDescription, colorPaletteInts)
                    } else {
                        (activity as? MainActivity)?.hideLoader()
                    }
                } else {
                    (activity as? MainActivity)?.hideLoader()
                }
            }
            .addOnFailureListener { e ->
                (activity as? MainActivity)?.hideLoader()
                e.printStackTrace()
                // Handle silently - we'll just show the default state
            }
    }
}