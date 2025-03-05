package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

    private var totalBudget = 0.0
    private var spentAmount = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budgetpage, container, false)

        progressBar = view.findViewById(R.id.budgetProgress)
        totalBudgetText = view.findViewById(R.id.totalBudgetText)
        spentText = view.findViewById(R.id.spentText)
        remainingText = view.findViewById(R.id.remainingText)
        addBudgetButton = view.findViewById(R.id.addBudgetButton)

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadBudgetDataFromFirestore()

        addBudgetButton.setOnClickListener {
            val dialog = BudgetDialogFragment()
            dialog.show(parentFragmentManager, "BudgetDialog")
        }

        return view
    }

    private fun loadBudgetDataFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).collection("Budget")
            .get()
            .addOnSuccessListener { snapshot ->
                totalBudget = 0.0
                spentAmount = 0.0
                clearAllBudgetViews()

                for (categorySnapshot in snapshot.documents) {
                    val category = categorySnapshot.id
                    categorySnapshot.reference.collection("Items").get()
                        .addOnSuccessListener { itemSnapshot ->
                            for (item in itemSnapshot.documents) {
                                val budgetItem = item.toObject(BudgetItem::class.java)
                                budgetItem?.let {
                                    totalBudget += it.amount
                                    if (it.paid) spentAmount += it.amount
                                    addBudgetItemToCategory(it, category)
                                }
                            }
                            updateProgress()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to load items", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load budget items", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProgress() {
        val remainingBudget = totalBudget - spentAmount
        val progress = if (totalBudget > 0) (spentAmount * 100) / totalBudget else 0.0

        progressBar.progress = progress.toInt()
        spentText.text = "Spent: ₱$spentAmount"
        remainingText.text = "Remaining: ₱$remainingBudget"
        totalBudgetText.text = "Total Budget: ₱$totalBudget"
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

    private fun addBudgetItemToCategory(budgetItem: BudgetItem, category: String) {
        val containerId = when (category) {
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
            dialog.show(parentFragmentManager, "BudgetDialog")
        }

        // Handle paid/unpaid status toggle
        paidIcon.setOnClickListener {
            budgetItem.paid = !budgetItem.paid
            updateBudgetItemInFirestore(budgetItem)
            updateProgress()
        }
    }

    private fun updateBudgetItemInFirestore(budgetItem: BudgetItem) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("Users").document(userId).collection("Budget")
            .document(budgetItem.category)
            .collection("Items").document(budgetItem.id ?: "")
            .set(budgetItem)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Budget item updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update budget item", Toast.LENGTH_SHORT).show()
            }
    }
}
