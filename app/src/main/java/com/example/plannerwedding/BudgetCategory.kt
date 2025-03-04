package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class BudgetCategoryFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var categoryContainers: Map<String, LinearLayout>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget_category, container, false)

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Map category names to their UI containers
        categoryContainers = mapOf(
            "Priority" to view.findViewById(R.id.budget_priorityContainer),
            "Wedding Venue" to view.findViewById(R.id.budget_weddingVenueContainer),
            "Entertainment" to view.findViewById(R.id.budget_entertainmentContainer),
            "Food & Beverages" to view.findViewById(R.id.budget_foodContainer),
            "Ceremony Essentials" to view.findViewById(R.id.budget_ceremonyEssentialsContainer),
            "Favors and Gifts" to view.findViewById(R.id.budget_favorsGiftsContainer),
            "Decorations" to view.findViewById(R.id.budget_decorationsContainer),
            "Photography and Videography" to view.findViewById(R.id.budget_photographyContainer),
            "Hair and Makeup" to view.findViewById(R.id.budget_hairMakeupContainer),
            "Bride's Outfit" to view.findViewById(R.id.budget_brideOutfitContainer),
            "Groom's Outfit" to view.findViewById(R.id.budget_groomOutfitContainer),
            "Transportation" to view.findViewById(R.id.budget_transportationContainer),
            "Other Expenses" to view.findViewById(R.id.budget_otherContainer)
        )

        // Load budget items from Firestore
        loadBudgetItemsFromFirestore()

        return view
    }

    fun loadBudgetItemsFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).collection("Budget")
            .get()
            .addOnSuccessListener { snapshot ->
                clearAllBudgetViews()

                for (document in snapshot.documents) {
                    val budgetItem = document.toObject(BudgetItem::class.java)
                    budgetItem?.let {
                        addBudgetItemToCategory(it)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load budget items", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearAllBudgetViews() {
        categoryContainers.values.forEach { it.removeAllViews() }
    }

    private fun addBudgetItemToCategory(budgetItem: BudgetItem) {
        val container = categoryContainers[budgetItem.category] ?: return
        val budgetView = layoutInflater.inflate(R.layout.fragment_budget__card, container, false)

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

        container.addView(budgetView)

        // Set click listener for editing the item
        budgetView.setOnClickListener {
            val dialog = BudgetDialogFragment()
            val bundle = Bundle()
            bundle.putSerializable("budgetItem", budgetItem)
            dialog.arguments = bundle
            dialog.show(parentFragmentManager, "BudgetDialog")
        }

        paidIcon.setOnClickListener {
            budgetItem.paid = !budgetItem.paid
            updateBudgetItemInFirestore(budgetItem, paidIcon, status)
        }
    }

    private fun updateBudgetItemInFirestore(budgetItem: BudgetItem, paidIcon: ImageView, status: TextView) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("Users").document(userId).collection("Budget")
            .document(budgetItem.id ?: return)
            .set(budgetItem)
            .addOnSuccessListener {
                if (budgetItem.paid) {
                    paidIcon.setImageResource(R.drawable.heart_filled)
                    status.text = "Paid"
                    status.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                } else {
                    paidIcon.setImageResource(R.drawable.heart1)
                    status.text = "Unpaid"
                    status.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
                Toast.makeText(requireContext(), "Budget item updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update budget item", Toast.LENGTH_SHORT).show()
            }
    }
}
