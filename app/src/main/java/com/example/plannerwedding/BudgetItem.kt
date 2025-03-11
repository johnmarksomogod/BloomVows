package com.example.plannerwedding

import java.io.Serializable

data class BudgetItem(
    var id: String? = null,
    var name: String = "",
    var category: String = "",
    var amount: Double = 0.0,
    var paid: Boolean = false
) : Serializable