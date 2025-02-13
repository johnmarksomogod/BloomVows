package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class BudgetCategoryFragment : Fragment() {

    private lateinit var budgetPriorityContainer: LinearLayout
    private lateinit var budgetWeddingVenueContainer: LinearLayout
    private lateinit var budgetEntertainmentContainer: LinearLayout
    private lateinit var budgetFoodContainer: LinearLayout
    private lateinit var budgetCeremonyEssentialsContainer: LinearLayout
    private lateinit var budgetFavorsGiftsContainer: LinearLayout
    private lateinit var budgetDecorationsContainer: LinearLayout
    private lateinit var budgetPhotographyVideographyContainer: LinearLayout
    private lateinit var budgetHairMakeupContainer: LinearLayout
    private lateinit var budgetBrideOutfitContainer: LinearLayout
    private lateinit var budgetGroomOutfitContainer: LinearLayout
    private lateinit var budgetTransportationContainer: LinearLayout
    private lateinit var budgetOtherTasksContainer: LinearLayout

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget_category, container, false)

        // Initialize category containers
        budgetPriorityContainer = view.findViewById(R.id.budget_priorityContainer)
        budgetWeddingVenueContainer = view.findViewById(R.id.budget_weddingVenueContainer)
        budgetEntertainmentContainer = view.findViewById(R.id.budget_entertainmentContainer)
        budgetFoodContainer = view.findViewById(R.id.budget_foodContainer)
        budgetCeremonyEssentialsContainer = view.findViewById(R.id.budget_ceremonyEssentialsContainer)
        budgetFavorsGiftsContainer = view.findViewById(R.id.budget_favorsGiftsContainer)
        budgetDecorationsContainer = view.findViewById(R.id.budget_decorationsContainer)
        budgetPhotographyVideographyContainer = view.findViewById(R.id.budget_photographyContainer)
        budgetHairMakeupContainer = view.findViewById(R.id.budget_hairMakeupContainer)
        budgetBrideOutfitContainer = view.findViewById(R.id.budget_brideOutfitContainer)
        budgetGroomOutfitContainer = view.findViewById(R.id.budget_groomOutfitContainer)
        budgetTransportationContainer = view.findViewById(R.id.budget_transportationContainer)
        budgetOtherTasksContainer = view.findViewById(R.id.budget_otherContainer)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("budgets")

        // Load data from Firebase
        loadBudgetsFromFirebase()

        return view
    }

    fun loadBudgetsFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                clearAllBudgetViews() // Clear existing budget views

                snapshot.children.forEach { categorySnapshot ->
                    val category = categorySnapshot.key
                    categorySnapshot.children.forEach { budgetSnapshot ->
                        val budget = budgetSnapshot.getValue(Budget::class.java)
                        budget?.let {
                            addBudgetToCategory(it, category)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load budgets", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clearAllBudgetViews() {
        val containers = listOf(
            budgetPriorityContainer,
            budgetWeddingVenueContainer,
            budgetEntertainmentContainer,
            budgetFoodContainer,
            budgetCeremonyEssentialsContainer,
            budgetFavorsGiftsContainer,
            budgetDecorationsContainer,
            budgetPhotographyVideographyContainer,
            budgetHairMakeupContainer,
            budgetBrideOutfitContainer,
            budgetGroomOutfitContainer,
            budgetTransportationContainer,
            budgetOtherTasksContainer
        )

        containers.forEach { it.removeAllViews() }
    }

    private fun addBudgetToCategory(budget: Budget, category: String?) {
        val container = when (category) {
            "Priority" -> budgetPriorityContainer
            "Wedding Venue" -> budgetWeddingVenueContainer
            "Entertainment" -> budgetEntertainmentContainer
            "Food & Beverages" -> budgetFoodContainer
            "Ceremony Essentials" -> budgetCeremonyEssentialsContainer
            "Favors and Gifts" -> budgetFavorsGiftsContainer
            "Decorations" -> budgetDecorationsContainer
            "Photography and Videography" -> budgetPhotographyVideographyContainer
            "Hair and Makeup" -> budgetHairMakeupContainer
            "Bride's Outfit" -> budgetBrideOutfitContainer
            "Groom's Outfit" -> budgetGroomOutfitContainer
            "Transportation Tasks" -> budgetTransportationContainer
            else -> budgetOtherTasksContainer
        }

        container?.let {
            val budgetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_budget__card, it, false)
            val budgetCard = budgetView as BudgetCardFragment
            budgetCard.setBudget(budget)
            it.addView(budgetView)
        }
    }
}
