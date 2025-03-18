package com.example.plannerwedding

import java.io.Serializable

data class Task(
    var id: String? = null,
    var name: String = "",
    var category: String = "",
    var deadline: String = "",
    var completed: Boolean = false
) : Serializable