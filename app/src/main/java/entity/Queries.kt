package com.example.expensenote.entity

import java.time.LocalDate

data class DateRange(
    val from: LocalDate? = null,
    val to: LocalDate? = null
)

data class TransactionQuery(
    val text: String? = null,
    val categoryIds: Set<Long> = emptySet(),
    val dateRange: DateRange? = null,
    val type: TransactionType? = null
)

data class Totals(
    val income: Double,
    val expense: Double,
    val balance: Double
)
