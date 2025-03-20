package com.example.plannerwedding

data class Guest(
    val name: String,
    val email: String,
    val category: String,
    val accommodationNeeded: Boolean,
    val invited: Boolean = false,
    val rsvpStatus: String = "Not Responded" // New field to track RSVP status
)