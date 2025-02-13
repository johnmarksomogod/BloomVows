package com.example.plannerwedding.model

data class BudgetItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    var isPaid: Boolean = false
)


