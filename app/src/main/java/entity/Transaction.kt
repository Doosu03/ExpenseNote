package com.example.expensenote.entity

enum class TransactionType { EXPENSE, INCOME }

data class Transaction(
    val id: Long,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val date: String,
    val note: String = "",
    val photoUri: String? = null
)
