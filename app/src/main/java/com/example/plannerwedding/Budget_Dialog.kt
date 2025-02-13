package com.example.plannerwedding

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.plannerwedding.R
import com.example.plannerwedding.model.BudgetItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class BudgetDialog : DialogFragment() {

    private lateinit var expenseName: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var expenseAmount: EditText
    private lateinit var paidCheckbox: CheckBox
    private lateinit var doneButton: TextView
    private lateinit var cancelButton: TextView
    private lateinit var database: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_budget__dialog, container, false)

        // Initialize views
        expenseName = view.findViewById(R.id.expenseName)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        expenseAmount = view.findViewById(R.id.expenseAmount)
        paidCheckbox = view.findViewById(R.id.paidCheckbox)
        doneButton = view.findViewById(R.id.doneButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        database = FirebaseDatabase.getInstance().getReference("Budget")

        // Done Button Click Listener
        doneButton.setOnClickListener {
            saveBudgetItem()
        }

        // Cancel Button Click Listener
        cancelButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun saveBudgetItem() {
        val name = expenseName.text.toString().trim()
        val category = categorySpinner.selectedItem.toString()
        val amount = expenseAmount.text.toString().trim()
        val isPaid = paidCheckbox.isChecked

        if (name.isEmpty() || amount.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val id = UUID.randomUUID().toString()
        val budgetItem = BudgetItem(id, name, category, amount.toDouble(), isPaid)

        // Save to Firebase
        database.child(category).child(id).setValue(budgetItem)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Budget added", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error saving budget", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
