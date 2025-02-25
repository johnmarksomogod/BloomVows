<<<<<<< HEAD
package com.example.plannerwedding

data class BudgetItem(
    val name: String,
    val category: String,
    val amount: Double,
    val isPaid: Boolean,
    val id: String = ""
)
=======
package com.example.plannerwedding.model

data class BudgetItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    var isPaid: Boolean = false
)


>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
