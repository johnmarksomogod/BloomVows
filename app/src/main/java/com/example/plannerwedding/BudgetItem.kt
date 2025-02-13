package com.example.plannerwedding

data class BudgetItem(
    val name: String,
    val category: String,
    val amount: Double,
    val isPaid: Boolean,
    val id: String = ""
)
