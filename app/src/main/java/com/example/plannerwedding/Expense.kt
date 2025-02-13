package com.example.plannerwedding

data class Expense(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val isPaid: Boolean = false
)
