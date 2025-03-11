package com.example.plannerwedding


import java.io.Serializable

data class Budget(
    val id: String = "budget",
    var totalAmount: Double = 0.0

) : Serializable

