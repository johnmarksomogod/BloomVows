package com.example.plannerwedding

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.cardview.widget.CardView
import androidx.appcompat.widget.AppCompatImageView

class WeddingThemeFragment : Fragment(R.layout.fragment_wedding_theme) {

    private var imageUri: String? = null
    private val colorPalette = mutableListOf<Int>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Floating button to trigger customization dialog
        val customizeButton: LinearLayout = view.findViewById(R.id.Customizebttn)
        customizeButton.setOnClickListener {
            // Show the customization dialog when the button is clicked
            showCustomizationDialog()
        }

        // Back Button to navigate back to the previous screen
        val backButton: ImageView = view.findViewById(R.id.WeddingThemeBackbttn)
        backButton.setOnClickListener {
            // Navigate back to the previous fragment or activity
            findNavController().navigateUp()
        }
    }

    private fun showCustomizationDialog() {
        // Create and show the dialog (Dialog logic is moved to a separate class)
        CustomizationDialogFragment { themeName, themeDescription, imageUri, colorPalette ->
            // After customization is done, update the wedding theme UI
            updateWeddingThemePage(imageUri, themeName, themeDescription, colorPalette)
        }.show(childFragmentManager, "CustomizationDialog")
    }

    private fun updateWeddingThemePage(imageUri: String?, themeName: String, themeDescription: String, colorPalette: List<Int>) {
        val customizedWeddingThemeContainer: LinearLayout = requireView().findViewById(R.id.customizedWeddingThemeContainer)
        val customizedThemeImage: ImageView = requireView().findViewById(R.id.customizedThemeImage)
        val customizedThemeName: TextView = requireView().findViewById(R.id.customizedThemeName)
        val customizedThemeDescription: TextView = requireView().findViewById(R.id.customizedThemeDescription)
        val customizedThemeColorPalette: LinearLayout = requireView().findViewById(R.id.customizedThemeColorPalette)

        // Check if imageUri is not null before trying to set the image
        if (imageUri != null) {
            customizedThemeImage.setImageURI(Uri.parse(imageUri))  // Set the image URI
        } else {
            customizedThemeImage.setImageResource(R.drawable.budget)  // Use a default image if no image is selected
        }

        customizedThemeName.text = themeName
        customizedThemeDescription.text = themeDescription

        // Add selected colors to the color palette
        customizedThemeColorPalette.removeAllViews()
        colorPalette.forEach { color ->
            val colorCircle = ImageView(requireContext()).apply {
                setImageResource(R.drawable.circle_shape)  // Circle shape for colors
                setColorFilter(color)
            }
            customizedThemeColorPalette.addView(colorCircle)
        }

        // Show the customized wedding theme container above the ScrollView
        customizedWeddingThemeContainer.visibility = View.VISIBLE
    }
}
