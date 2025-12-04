package com.example.expensenote.entity

import android.graphics.Bitmap

enum class TransactionType(val displayName: String) {
    INCOME("Ingreso"),
    EXPENSE("Gasto")
}

data class Transaction(
    val id: Long = 0,
    val stringId: String = "",
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val date: String,
    val note: String = "",
    val photoUri: String? = null,
    val photoBitmap: Bitmap? = null
)