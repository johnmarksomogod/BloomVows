package com.example.plannerwedding

<<<<<<< HEAD
import java.io.Serializable

data class Budget(
    val id: String? = null,
    val name: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    var paid: Boolean = false
) : Serializable
=======
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plannerwedding.model.BudgetItem
import com.google.firebase.database.*

class Budget : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var budgetAdapter: BudgetAdapter
    private var budgetList = mutableListOf<BudgetItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_budget, container, false)

        recyclerView = view.findViewById(R.id.budgetRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        budgetAdapter = BudgetAdapter(budgetList)
        recyclerView.adapter = budgetAdapter

        loadBudgetData()

        return view
    }

    private fun loadBudgetData() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Budget")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                budgetList.clear()
                for (categorySnapshot in snapshot.children) {
                    for (budgetSnapshot in categorySnapshot.children) {
                        val budgetItem = budgetSnapshot.getValue(BudgetItem::class.java)
                        if (budgetItem != null) {
                            budgetList.add(budgetItem)
                        }
                    }
                }
                budgetAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
