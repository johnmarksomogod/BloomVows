package com.example.plannerwedding

<<<<<<< HEAD
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class BudgetDialogFragment : DialogFragment() {

    private lateinit var expenseNameInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var amountInput: EditText
=======
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
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
    private lateinit var paidCheckbox: CheckBox
    private lateinit var doneButton: TextView
    private lateinit var cancelButton: TextView
    private lateinit var database: DatabaseReference

<<<<<<< HEAD
    private var budget: Budget? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_budget__dialog, container, false)

        // Initialize views
        expenseNameInput = view.findViewById(R.id.expenseName)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        amountInput = view.findViewById(R.id.expenseAmount)
        paidCheckbox = view.findViewById(R.id.paidCheckbox)
        doneButton = view.findViewById(R.id.budgetdoneButton)
        cancelButton = view.findViewById(R.id.budgetcancelButton)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance("https://wedding-planner-f8418-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("budgets")

        // Retrieve the budget data passed from the calling fragment
        budget = arguments?.getSerializable("budget") as Budget?

        // Populate the views with the budget data if it's not null
        budget?.let { populateBudgetDetails(it) }

        // Set up category spinner
        val categories = listOf(
            "Priority", "Wedding Venue", "Entertainment", "Food & Beverages", "Ceremony Essentials",
            "Favors and Gifts", "Decorations", "Photography and Videography", "Hair and Makeup",
            "Bride's Outfit", "Groom's Outfit", "Transportation Tasks", "Other Tasks"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        // Set up click listeners for Done and Cancel buttons
        doneButton.setOnClickListener { saveBudget() }
        cancelButton.setOnClickListener { dismiss() }
=======
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
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00

        return view
    }

<<<<<<< HEAD
    private fun populateBudgetDetails(budget: Budget) {
        // Set the budget's existing values
        expenseNameInput.setText(budget.name)
        categorySpinner.setSelection((categorySpinner.adapter as ArrayAdapter<String>).getPosition(budget.category))
        amountInput.setText(budget.amount.toString())
        paidCheckbox.isChecked = budget.paid
    }

    private fun saveBudget() {
        val expenseName = expenseNameInput.text.toString().trim()
        val category = categorySpinner.selectedItem?.toString() ?: ""
        val amount = amountInput.text.toString().toDoubleOrNull()
        val paid = paidCheckbox.isChecked

        if (expenseName.isEmpty() || amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Expense name and amount are required", Toast.LENGTH_SHORT).show()
            return
        }

        val budgetId = budget?.id ?: database.child(category).push().key ?: return

        val updatedBudget = Budget(budgetId, expenseName, category, amount, paid)

        budget?.let {
            updateBudget(updatedBudget)
        } ?: run {
            addNewBudget(updatedBudget)
        }
    }

    private fun updateBudget(budget: Budget) {
        budget.id?.let {
            database.child(budget.category).child(it).setValue(budget)
                .addOnSuccessListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Budget updated successfully!", Toast.LENGTH_SHORT).show()
                        dismiss()
                        (parentFragment as? BudgetCategoryFragment)?.loadBudgetsFromFirebase()
                    }
                }
                .addOnFailureListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to update budget", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun addNewBudget(budget: Budget) {
        budget.id?.let {
            database.child(budget.category).child(it).setValue(budget)
                .addOnSuccessListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Budget added successfully!", Toast.LENGTH_SHORT).show()
                        dismiss()
                        (parentFragment as? BudgetCategoryFragment)?.loadBudgetsFromFirebase()
                    }
                }
                .addOnFailureListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to add budget", Toast.LENGTH_SHORT).show()
                    }
                }
        }
=======
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
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
    }
}
