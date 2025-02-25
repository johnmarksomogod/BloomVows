<<<<<<< HEAD
package com.example.plannerwedding

import java.io.Serializable

data class Task(
    val id: String? = null,
    val name: String = "",
    val category: String = "",
    val deadline: String = "",
    var completed: Boolean = false // Task completion state
): Serializable

=======
data class Task(
    val taskId: String = "",
    val taskName: String = "",
    val category: String = "",
    val deadline: String = "",
    val completed: Boolean = false
)
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
