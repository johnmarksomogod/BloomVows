package com.example.plannerwedding

// Updated Guest data class with invited status
data class Guest(
    val name: String,
    val email: String,
    val category: String,
    val accommodationNeeded: Boolean,
    val invited: Boolean = false
)