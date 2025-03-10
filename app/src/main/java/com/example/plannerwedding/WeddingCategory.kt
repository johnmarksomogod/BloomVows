package com.example.plannerwedding

data class WeddingCategory(
    val name: String,
    val description: String,
    val imageUri: String?,
    val colorPalette: List<Int>
)
