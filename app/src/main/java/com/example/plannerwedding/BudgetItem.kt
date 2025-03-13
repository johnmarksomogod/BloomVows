package com.example.plannerwedding

import java.io.Serializable

data class BudgetItem(
    var id: String? = null,
    val name: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    var paid: Boolean = false
) : Serializable