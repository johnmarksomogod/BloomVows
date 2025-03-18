package com.example.plannerwedding

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.sliders.AlphaSlideBar
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar

class CustomizationDialogFragment(private val onCustomizationSaved: (String, String, String?, List<Int>) -> Unit) : DialogFragment() {

    private var imageUri: String? = null
    private val colorPalette = mutableListOf<Int>()
    private lateinit var selectedImagePreview: ImageView
    private lateinit var uploadPlaceholder: LinearLayout
    private lateinit var selectedColorsContainer: LinearLayout
    private lateinit var savedCustomizationText: TextView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var colorPickerView: ColorPickerView
    private lateinit var brightnessSlideBar: BrightnessSlideBar
    private lateinit var alphaSlideBar: AlphaSlideBar
    private lateinit var colorPreview: View
    private lateinit var hexCodeText: TextView
    private lateinit var colorCodeEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                try {
                    // Update the image URI and preview
                    imageUri = it.toString()
                    selectedImagePreview.setImageURI(it)
                    selectedImagePreview.visibility = View.VISIBLE
                    uploadPlaceholder.visibility = View.GONE
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val dialogView = inflater.inflate(R.layout.fragment_customization_dialog, container, false)

        // Initialize views
        val backButton: ImageView = dialogView.findViewById(R.id.backButton)
        val imageUploadButton: ImageView = dialogView.findViewById(R.id.imageUploadButton)
        val colorPickerGrid: GridLayout = dialogView.findViewById(R.id.colorPickerGrid)
        val addColorButton: MaterialButton = dialogView.findViewById(R.id.addColorButton)
        val saveCustomizationButton: MaterialButton = dialogView.findViewById(R.id.saveCustomizationButton)
        val categoryNameEditText: TextInputEditText = dialogView.findViewById(R.id.categoryName)
        val categoryDescriptionEditText: TextInputEditText = dialogView.findViewById(R.id.categoryDescription)
        colorCodeEditText = dialogView.findViewById(R.id.colorCodeEditText)
        val addCurrentColorButton: MaterialButton = dialogView.findViewById(R.id.addCurrentColorButton)
        selectedImagePreview = dialogView.findViewById(R.id.selectedImagePreview)
        uploadPlaceholder = dialogView.findViewById(R.id.uploadPlaceholder)
        selectedColorsContainer = dialogView.findViewById(R.id.selectedColorsContainer)
        savedCustomizationText = dialogView.findViewById(R.id.savedCustomizationText)

        // Initialize color picker views
        colorPickerView = dialogView.findViewById(R.id.colorPickerView)
        brightnessSlideBar = dialogView.findViewById(R.id.brightnessSlideBar)
        alphaSlideBar = dialogView.findViewById(R.id.alphaSlideBar)
        colorPreview = dialogView.findViewById(R.id.colorPreview)
        hexCodeText = dialogView.findViewById(R.id.hexCodeText)

        // Setup color picker
        setupColorPicker()

        // Restore saved state if available
        savedInstanceState?.let {
            imageUri = it.getString(KEY_IMAGE_URI)
            val savedColors = it.getIntArray(KEY_COLOR_PALETTE)
            if (savedColors != null) {
                colorPalette.clear()
                colorPalette.addAll(savedColors.toList())
            }

            // Restore image preview if URI exists
            imageUri?.let { uri ->
                try {
                    selectedImagePreview.setImageURI(Uri.parse(uri))
                    selectedImagePreview.visibility = View.VISIBLE
                    uploadPlaceholder.visibility = View.GONE
                } catch (e: Exception) {
                    // Reset if there's an error loading the image
                    imageUri = null
                }
            }
        }

        // Back Button to dismiss the dialog
        backButton.setOnClickListener {
            dismiss()
        }

        // Handle Image Upload
        imageUploadButton.setOnClickListener {
            uploadImage()
        }

        // Also make the placeholder clickable for better UX
        uploadPlaceholder.setOnClickListener {
            uploadImage()
        }

        // Add predefined colors if we don't have any
        if (colorPalette.isEmpty()) {
            addPredefinedColors()
        }

        // Initialize color picker grid and selected colors
        updateColorGrid(colorPickerGrid)
        updateSelectedColorsContainer()

        // Handle Add Current Color Button
        addCurrentColorButton.setOnClickListener {
            val color = colorPickerView.color

            // Check if color already exists in palette
            if (colorPalette.contains(color)) {
                Toast.makeText(requireContext(), "This color is already in your palette", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Add color to palette
            colorPalette.add(color)
            updateColorGrid(colorPickerGrid)
            updateSelectedColorsContainer()
            Toast.makeText(requireContext(), "Color added", Toast.LENGTH_SHORT).show()
        }

        // Handle Add Color Button (from manual hex input)
        addColorButton.setOnClickListener {
            val colorCode = colorCodeEditText.text.toString().trim()
            if (colorCode.isEmpty()) {
                colorCodeEditText.error = "Please enter a color code"
                return@setOnClickListener
            }

            try {
                // Ensure the color code starts with #
                val formattedColorCode = if (colorCode.startsWith("#")) colorCode else "#$colorCode"
                val color = Color.parseColor(formattedColorCode)

                // Check if color already exists in palette
                if (colorPalette.contains(color)) {
                    Toast.makeText(requireContext(), "This color is already in your palette", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Add color to palette
                colorPalette.add(color)
                updateColorGrid(colorPickerGrid)
                updateSelectedColorsContainer()
                colorCodeEditText.text?.clear()
                Toast.makeText(requireContext(), "Color added", Toast.LENGTH_SHORT).show()
            } catch (e: IllegalArgumentException) {
                colorCodeEditText.error = "Invalid color code. Format: #RRGGBB"
            }
        }

        // Save Customization
        saveCustomizationButton.setOnClickListener {
            val categoryName = categoryNameEditText.text.toString().trim()
            val categoryDescription = categoryDescriptionEditText.text.toString().trim()

            // Validate inputs
            when {
                categoryName.isEmpty() -> {
                    categoryNameEditText.error = "Theme name is required"
                    return@setOnClickListener
                }
                colorPalette.isEmpty() -> {
                    Toast.makeText(requireContext(), "Please add at least one color to your palette", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Show success message
            savedCustomizationText.visibility = View.VISIBLE

            // Call the callback function to pass data to the parent fragment
            onCustomizationSaved(categoryName, categoryDescription, imageUri, colorPalette)

            // Dismiss dialog after delay
            dialogView.postDelayed({
                dismiss()
            }, 1500)
        }

        return dialogView
    }

    private fun setupColorPicker() {
        // Attach the sliders to the ColorPickerView
        colorPickerView.attachBrightnessSlider(brightnessSlideBar)
        colorPickerView.attachAlphaSlider(alphaSlideBar)

        // Set initial color
        colorPickerView.setInitialColor(Color.parseColor("#EDABAD"))

        // Update color preview and hex text when color changes
        colorPickerView.setColorListener(ColorEnvelopeListener { envelope, fromUser ->
            val color = envelope.color
            val hexCode = "#${envelope.hexCode}"

            // Update preview
            colorPreview.setBackgroundColor(color)
            hexCodeText.text = hexCode

            // Update the input field
            colorCodeEditText.setText(hexCode)
        })
    }

    private fun addPredefinedColors() {
        // Add wedding-themed colors
        val weddingColors = arrayOf(
            "#EDABAD",  // Rose pink
            "#F5E1DA",  // Blush
            "#D4B2A7",  // Mauve
            "#998B82",  // Taupe
            "#F5F5F5",  // White
            "#D8BFD8",  // Thistle
            "#87CEFA",  // Light sky blue
            "#E6E6FA",  // Lavender
            "#FFD700",  // Gold
            "#F0E68C"   // Khaki
        )

        for (colorCode in weddingColors) {
            try {
                colorPalette.add(Color.parseColor(colorCode))
            } catch (e: Exception) {
                // Skip invalid color
            }
        }
    }

    private fun uploadImage() {
        // Use the ActivityResultLauncher to select an image
        try {
            imagePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            // Fallback method if the primary method fails
            try {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
            } catch (e2: Exception) {
                Toast.makeText(requireContext(), "No app available to select images", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val uri = data.data ?: return

            try {
                // Update the image URI and preview
                imageUri = uri.toString()
                selectedImagePreview.setImageURI(uri)
                selectedImagePreview.visibility = View.VISIBLE
                uploadPlaceholder.visibility = View.GONE
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateColorGrid(colorPickerGrid: GridLayout) {
        // Clear existing views
        colorPickerGrid.removeAllViews()

        // Add colors to grid
        for (color in colorPalette) {
            val colorView = createColorView(color) {
                colorPalette.remove(color)
                updateColorGrid(colorPickerGrid)
                updateSelectedColorsContainer()
            }
            colorPickerGrid.addView(colorView)
        }
    }

    private fun createColorView(color: Int, onClickAction: () -> Unit): View {
        return AppCompatImageView(requireContext()).apply {
            val size = resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
            layoutParams = GridLayout.LayoutParams().apply {
                width = size
                height = size
                setMargins(8, 8, 8, 8)
            }
            setBackgroundColor(color)

            // Make it look like a clickable item
            isClickable = true
            isFocusable = true

            // Set click listener to remove the color
            setOnClickListener {
                onClickAction()
                Toast.makeText(requireContext(), "Color removed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSelectedColorsContainer() {
        // Clear existing views
        selectedColorsContainer.removeAllViews()

        // Create circular color views with rounded corners
        for (color in colorPalette) {
            val colorCircle = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(60, 60).apply {
                    setMargins(8, 0, 8, 0)
                }

                // Create rounded background
                val shape = GradientDrawable()
                shape.shape = GradientDrawable.OVAL
                shape.setColor(color)
                background = shape
            }
            selectedColorsContainer.addView(colorCircle)
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save state for configuration changes
        outState.putString(KEY_IMAGE_URI, imageUri)
        outState.putIntArray(KEY_COLOR_PALETTE, colorPalette.toIntArray())
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        private const val REQUEST_CODE_IMAGE_PICK = 100
        private const val KEY_IMAGE_URI = "image_uri"
        private const val KEY_COLOR_PALETTE = "color_palette"
    }
}