package com.example.plannerwedding

data class Budget(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val paid: Boolean = false
)
