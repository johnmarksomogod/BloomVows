package com.example.plannerwedding

import java.io.Serializable

data class BudgetItem(
    val id: String? = null,
    val name: String = "",
    val category: String = "",
    var amount: Double = 0.0,
    var paid: Boolean = false
) : Serializable
