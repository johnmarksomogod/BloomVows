package com.example.plannerwedding

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView

class CustomizationDialogFragment(private val onCustomizationSaved: (String, String, String?, List<Int>) -> Unit) : DialogFragment() {

    private var imageUri: String? = null
    private val colorPalette = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val dialogView = inflater.inflate(R.layout.fragment_customization_dialog, container, false)

        // Bind UI elements from the dialog layout
        val backButton: ImageView = dialogView.findViewById(R.id.backButton)
        val imageUploadButton: ImageView = dialogView.findViewById(R.id.imageUploadButton)
        val colorPickerGrid: GridLayout = dialogView.findViewById(R.id.colorPickerGrid)
        val addColorButton: Button = dialogView.findViewById(R.id.addColorButton)
        val saveCustomizationButton: Button = dialogView.findViewById(R.id.saveCustomizationButton)
        val themeNameEditText: EditText = dialogView.findViewById(R.id.themeName)
        val themeDescriptionEditText: EditText = dialogView.findViewById(R.id.themeDescription)
        val colorCodeEditText: EditText = dialogView.findViewById(R.id.colorCodeEditText)

        // Back Button to dismiss the dialog
        backButton.setOnClickListener {
            dismiss()
        }

        // Handle Image Upload
        imageUploadButton.setOnClickListener { uploadImage() }

        // Initialize color picker grid with predefined colors
        setupColorPickerGrid(colorPickerGrid)

        // Handle Add Color Button
        addColorButton.setOnClickListener {
            val colorCode = colorCodeEditText.text.toString()
            if (colorCode.isNotEmpty()) {
                val color = try {
                    Color.parseColor(colorCode)  // Parse the color code
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(requireContext(), "Invalid color code", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                colorPalette.add(color)
                updateColorGrid(colorPickerGrid) // Update the grid with the added color
                colorCodeEditText.text.clear()   // Clear the input field
            }
        }

        // Save Customization
        saveCustomizationButton.setOnClickListener {
            val themeName = themeNameEditText.text.toString()
            val themeDescription = themeDescriptionEditText.text.toString()

            // Call the callback function to pass data to the parent fragment
            onCustomizationSaved(themeName, themeDescription, imageUri, colorPalette)

            // Dismiss the dialog
            dismiss()
        }

        return dialogView
    }

    private fun uploadImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val uri = data.data
            imageUri = uri.toString()  // Convert URI to string and assign it
        }
    }

    private fun setupColorPickerGrid(colorPickerGrid: GridLayout) {
        // Initialize the color grid with existing colors
        colorPalette.forEach { color ->
            val colorView = AppCompatImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100)
                setBackgroundColor(color)
                setOnClickListener {
                    // When the color is clicked, it can be set as the selected color
                    Toast.makeText(requireContext(), "Color selected", Toast.LENGTH_SHORT).show()
                }
            }
            colorPickerGrid.addView(colorView)
        }
    }

    private fun updateColorGrid(colorPickerGrid: GridLayout) {
        // Update the color grid with newly added colors
        colorPickerGrid.removeAllViews()
        setupColorPickerGrid(colorPickerGrid)


    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
