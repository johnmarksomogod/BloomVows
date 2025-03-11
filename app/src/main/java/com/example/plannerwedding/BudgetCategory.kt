package com.example.plannerwedding
//
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BudgetCategoryFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var budgetPriorityContainer: LinearLayout
    private lateinit var budgetWeddingVenueContainer: LinearLayout
    private lateinit var budgetEntertainmentContainer: LinearLayout
    private lateinit var budgetFoodContainer: LinearLayout
    private lateinit var budgetCeremonyEssentialsContainer: LinearLayout
    private lateinit var budgetFavorsGiftsContainer: LinearLayout
    private lateinit var budgetDecorationsContainer: LinearLayout
    private lateinit var budgetPhotographyContainer: LinearLayout
    private lateinit var budgetHairMakeupContainer: LinearLayout
    private lateinit var budgetBrideOutfitContainer: LinearLayout
    private lateinit var budgetGroomOutfitContainer: LinearLayout
    private lateinit var budgetTransportationContainer: LinearLayout
    private lateinit var budgetOtherContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget_category, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        budgetPriorityContainer = view.findViewById(R.id.budget_priorityContainer)
        budgetWeddingVenueContainer = view.findViewById(R.id.budget_weddingVenueContainer)
        budgetEntertainmentContainer = view.findViewById(R.id.budget_entertainmentContainer)
        budgetFoodContainer = view.findViewById(R.id.budget_foodContainer)
        budgetCeremonyEssentialsContainer = view.findViewById(R.id.budget_ceremonyEssentialsContainer)
        budgetFavorsGiftsContainer = view.findViewById(R.id.budget_favorsGiftsContainer)
        budgetDecorationsContainer = view.findViewById(R.id.budget_decorationsContainer)
        budgetPhotographyContainer = view.findViewById(R.id.budget_photographyContainer)
        budgetHairMakeupContainer = view.findViewById(R.id.budget_hairMakeupContainer)
        budgetBrideOutfitContainer = view.findViewById(R.id.budget_brideOutfitContainer)
        budgetGroomOutfitContainer = view.findViewById(R.id.budget_groomOutfitContainer)
        budgetTransportationContainer = view.findViewById(R.id.budget_transportationContainer)
        budgetOtherContainer = view.findViewById(R.id.budget_otherContainer)

        loadBudgetItemsFromFirestore()

        return view
    }

    private fun loadBudgetItemsFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId)
            .collection("Budget")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(requireContext(), "No budget items found", Toast.LENGTH_SHORT).show()
                }

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

    private fun addBudgetItemToCategory(budgetItem: BudgetItem) {
        val containerId = when (budgetItem.category) {
            "Priority" -> budgetPriorityContainer
            "Wedding Venue" -> budgetWeddingVenueContainer
            "Entertainment" -> budgetEntertainmentContainer
            "Food & Beverages" -> budgetFoodContainer
            "Ceremony Essentials" -> budgetCeremonyEssentialsContainer
            "Favors and Gifts" -> budgetFavorsGiftsContainer
            "Decorations" -> budgetDecorationsContainer
            "Photography and Videography" -> budgetPhotographyContainer
            "Hair and Makeup" -> budgetHairMakeupContainer
            "Bride's Outfit" -> budgetBrideOutfitContainer
            "Groom's Outfit" -> budgetGroomOutfitContainer
            "Transportation Tasks" -> budgetTransportationContainer
            else -> budgetOtherContainer
        }

        val budgetView = layoutInflater.inflate(R.layout.fragment_budget__card, containerId, false)
        val budgetCard = budgetView as BudgetCard
        budgetCard.setBudgetItem(budgetItem)

        containerId.addView(budgetView)
    }
}
