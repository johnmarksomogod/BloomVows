package com.example.plannerwedding

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BudgetPageFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var totalBudgetText: TextView
    private lateinit var spentText: TextView
    private lateinit var remainingText: TextView
    private lateinit var addBudgetButton: LinearLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var totalBudget: Double = 0.0
    private var spentAmount: Double = 0.0
    private var remainingBudget: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budgetpage, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        progressBar = view.findViewById(R.id.budgetProgress)
        totalBudgetText = view.findViewById(R.id.totalBudgetText)
        spentText = view.findViewById(R.id.spentText)
        remainingText = view.findViewById(R.id.remainingText)
        addBudgetButton = view.findViewById(R.id.addBudgetButton)

        // Load the total budget from Firestore
        loadTotalBudgetFromFirestore()

        addBudgetButton.setOnClickListener {
            val dialog = BudgetDialogFragment()
            dialog.setDialogDismissListener(object : BudgetDialogFragment.DialogDismissListener {
                override fun onDialogDismiss() {
                    // Reload data after the dialog is dismissed
                    loadBudgetDataFromFirestore()
                }
            })
            dialog.show(parentFragmentManager, "BudgetDialog")
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadTotalBudgetFromFirestore()
    }

    private fun loadTotalBudgetFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Use getDouble() to get the budget field correctly as a number (Double)
                    val budgetField = document.getDouble("budget")
                    totalBudget = budgetField ?: 0.0  // If the budget is null, default to 0.0
                    // Now load the budget items
                    loadBudgetDataFromFirestore()
                } else {
                    totalBudget = 0.0
                    updateProgress()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load total budget", Toast.LENGTH_SHORT).show()
                loadBudgetDataFromFirestore()
            }
    }

    private fun loadBudgetDataFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).collection("Budget")
            .get()
            .addOnSuccessListener { snapshot ->
                spentAmount = 0.0

                clearAllBudgetViews()

                if (snapshot.isEmpty) {
                    updateProgress()
                }

                for (document in snapshot.documents) {
                    val budgetItem = document.toObject(BudgetItem::class.java)
                    // Make sure we set the ID field from the document ID
                    budgetItem?.id = document.id
                    budgetItem?.let {
                        if (it.paid) spentAmount += it.amount
                        addBudgetItemToCategory(it)
                    }
                }
                updateProgress()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load budget items", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProgress() {
        remainingBudget = totalBudget - spentAmount
        val progress = if (totalBudget > 0) (spentAmount * 100) / totalBudget else 0.0

        progressBar.progress = progress.toInt()  // Cast progress to Int
        totalBudgetText.text = "Total Budget: ₱${totalBudget.toInt()}"
        spentText.text = "Spent: ₱${spentAmount.toInt()}"
        remainingText.text = "Remaining: ₱${remainingBudget.toInt()}"
    }

    private fun clearAllBudgetViews() {
        val allContainers = listOf(
            R.id.budget_priorityContainer, R.id.budget_weddingVenueContainer,
            R.id.budget_entertainmentContainer, R.id.budget_foodContainer,
            R.id.budget_ceremonyEssentialsContainer, R.id.budget_favorsGiftsContainer,
            R.id.budget_decorationsContainer, R.id.budget_photographyContainer,
            R.id.budget_hairMakeupContainer, R.id.budget_brideOutfitContainer,
            R.id.budget_groomOutfitContainer, R.id.budget_transportationContainer,
            R.id.budget_otherContainer
        )

        for (containerId in allContainers) {
            val container = view?.findViewById<LinearLayout>(containerId)
            container?.removeAllViews()
        }
    }

    private fun addBudgetItemToCategory(budgetItem: BudgetItem) {
        val containerId = when (budgetItem.category) {
            "Priority" -> R.id.budget_priorityContainer
            "Wedding Venue" -> R.id.budget_weddingVenueContainer
            "Entertainment" -> R.id.budget_entertainmentContainer
            "Food & Beverages" -> R.id.budget_foodContainer
            "Ceremony Essentials" -> R.id.budget_ceremonyEssentialsContainer
            "Favors and Gifts" -> R.id.budget_favorsGiftsContainer
            "Decorations" -> R.id.budget_decorationsContainer
            "Photography and Videography" -> R.id.budget_photographyContainer
            "Hair and Makeup" -> R.id.budget_hairMakeupContainer
            "Bride's Outfit" -> R.id.budget_brideOutfitContainer
            "Groom's Outfit" -> R.id.budget_groomOutfitContainer
            "Transportation Tasks" -> R.id.budget_transportationContainer
            else -> R.id.budget_otherContainer
        }

        val categoryContainer = view?.findViewById<LinearLayout>(containerId) ?: return
        val budgetView = layoutInflater.inflate(R.layout.fragment_budget__card, null)

        val title = budgetView.findViewById<TextView>(R.id.itemTitle)
        val cost = budgetView.findViewById<TextView>(R.id.itemCost)
        val status = budgetView.findViewById<TextView>(R.id.paidStatus)
        val paidIcon = budgetView.findViewById<ImageView>(R.id.paidbudget)
        val deleteIcon = budgetView.findViewById<ImageView>(R.id.deletebudget)

        title.text = budgetItem.name
        cost.text = "Cost: ₱${budgetItem.amount}"
        status.text = if (budgetItem.paid) {
            paidIcon.setImageResource(R.drawable.heart_filled)
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            "Paid"
        } else {
            paidIcon.setImageResource(R.drawable.heart1)
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            "Unpaid"
        }

        categoryContainer.addView(budgetView)

        // Handle item click
        budgetView.setOnClickListener {
            val dialog = BudgetDialogFragment()
            val bundle = Bundle()
            bundle.putSerializable("budgetItem", budgetItem)
            dialog.arguments = bundle
            dialog.setDialogDismissListener(object : BudgetDialogFragment.DialogDismissListener {
                override fun onDialogDismiss() {
                    // Reload data after the dialog is dismissed to update UI
                    loadBudgetDataFromFirestore()
                }
            })
            dialog.show(parentFragmentManager, "BudgetDialog")
        }

        // Handle paid/unpaid status toggle with confirmation dialog
        paidIcon.setOnClickListener {
            val message = if (budgetItem.paid)
                "Are you sure you want to mark this item as unpaid?"
            else
                "Are you sure you want to mark this item as paid?"

            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle("Confirm Status Change")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    // Update the paid status in the budgetItem object
                    budgetItem.paid = !budgetItem.paid

                    // Update Firestore
                    updateBudgetItemInFirestore(budgetItem)

                    // Update UI
                    if (budgetItem.paid) {
                        spentAmount += budgetItem.amount
                    } else {
                        spentAmount -= budgetItem.amount
                    }

                    bindBudget(budgetView, budgetItem)
                    updateProgress()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.cancel()
                }

            val alert = alertDialogBuilder.create()
            alert.show()

            // Change the text color of the buttons
            val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveButton.setTextColor(Color.parseColor("#EDABAD"))
            negativeButton.setTextColor(Color.parseColor("#EDABAD"))

            // Set dialog message text color to black
            alert.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
        }

        // Add delete functionality with confirmation dialog
        deleteIcon.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle("Delete Budget Item")
                .setMessage("Are you sure you want to delete this budget item? This action cannot be undone.")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    deleteBudgetItem(budgetItem, budgetView, categoryContainer)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.cancel()
                }

            val alert = alertDialogBuilder.create()
            alert.show()

            // Change the text color of the buttons
            val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveButton.setTextColor(Color.parseColor("#EDABAD"))
            negativeButton.setTextColor(Color.parseColor("#EDABAD"))

            // Set dialog message text color to black
            alert.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
        }
    }

    // Improved delete method to ensure the item is removed properly
    private fun deleteBudgetItem(budgetItem: BudgetItem, view: View, container: LinearLayout) {
        val userId = auth.currentUser?.uid ?: return

        // Make sure we have a valid ID for the document
        if (budgetItem.id.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Error: Budget item has no ID", Toast.LENGTH_SHORT).show()
            return
        }

        // First, remove the amount from the spent amount if it was paid
        if (budgetItem.paid) {
            spentAmount -= budgetItem.amount
        }

        // Delete from Firestore
        db.collection("Users").document(userId).collection("Budget")
            .document(budgetItem.id!!)
            .delete()
            .addOnSuccessListener {
                // Remove view from container
                container.removeView(view)

                // Update progress
                updateProgress()

                Toast.makeText(requireContext(), "Budget item deleted successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete budget item: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBudgetItemInFirestore(budgetItem: BudgetItem) {
        val userId = auth.currentUser?.uid ?: return

        if (budgetItem.id.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Error: Budget item has no ID", Toast.LENGTH_SHORT).show()
            return
        }

        val taskRef = db.collection("Users").document(userId).collection("Budget")
            .document(budgetItem.id!!)

        taskRef.set(budgetItem)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Budget item updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update budget item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bindBudget(view: View, budgetItem: BudgetItem) {
        val title = view.findViewById<TextView>(R.id.itemTitle)
        val cost = view.findViewById<TextView>(R.id.itemCost)
        val status = view.findViewById<TextView>(R.id.paidStatus)
        val paidIcon = view.findViewById<ImageView>(R.id.paidbudget)

        title.text = budgetItem.name
        cost.text = "Cost: ₱${budgetItem.amount}"
        status.text = if (budgetItem.paid) {
            paidIcon.setImageResource(R.drawable.heart_filled)
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            "Paid"
        } else {
            paidIcon.setImageResource(R.drawable.heart1)
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            "Unpaid"
        }
    }
}