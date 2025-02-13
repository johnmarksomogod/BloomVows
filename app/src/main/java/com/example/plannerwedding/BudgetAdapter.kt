package com.example.plannerwedding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plannerwedding.R
import com.example.plannerwedding.model.BudgetItem
import com.google.firebase.database.FirebaseDatabase

class BudgetAdapter(private val budgetList: MutableList<BudgetItem>) :
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.budget_item, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budgetItem = budgetList[position]
        holder.itemTitle.text = budgetItem.name
        holder.itemCost.text = "Cost: $${budgetItem.amount}"
        holder.paidStatus.text = if (budgetItem.isPaid) "Paid" else "Unpaid"
        holder.paidStatus.setTextColor(if (budgetItem.isPaid) 0xFF228B22.toInt() else 0xFFB22222.toInt())

        holder.paidIcon.setOnClickListener {
            val database = FirebaseDatabase.getInstance().getReference("Budget")
                .child(budgetItem.category).child(budgetItem.id)

            database.child("isPaid").setValue(true)
            budgetItem.isPaid = true
            notifyDataSetChanged()
        }

        holder.deleteIcon.setOnClickListener {
            val database = FirebaseDatabase.getInstance().getReference("Budget")
                .child(budgetItem.category).child(budgetItem.id)

            database.removeValue()
            budgetList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount() = budgetList.size

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.findViewById(R.id.itemTitle)
        val itemCost: TextView = itemView.findViewById(R.id.itemCost)
        val paidStatus: TextView = itemView.findViewById(R.id.paidStatus)
        val paidIcon: ImageView = itemView.findViewById(R.id.paidbudget)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deletebudget)
    }
}
