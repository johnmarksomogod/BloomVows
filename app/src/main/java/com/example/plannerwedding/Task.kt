package com.example.plannerwedding

import java.io.Serializable

data class Task(
    val id: String? = null,
    val name: String = "",
    val category: String = "",
    val deadline: String = "",
    var completed: Boolean = false
) : Serializable
