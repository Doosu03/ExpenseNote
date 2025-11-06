package com.example.expensenote.Controller

import com.example.expensenote.Data.IDataManager
import com.example.expensenote.entity.DateRange
import com.example.expensenote.entity.Totals
import com.example.expensenote.entity.Transaction
import com.example.expensenote.entity.TransactionQuery
import com.example.expensenote.entity.TransactionType
import kotlin.math.abs

class TransactionController(private val data: IDataManager) {

    fun list(query: TransactionQuery? = null): List<Transaction> =
        data.getTransactions(query)

    fun get(id: Long): Transaction? = data.getTransaction(id)

    fun create(input: Transaction): Transaction {
        require(input.amount != 0.0) { "Amount must be different from 0." }
        // Normalize amount sign according to type: INCOME = positive, EXPENSE = negative
        val normalized = when (input.type) {
            TransactionType.EXPENSE -> input.copy(amount = -abs(input.amount))
            TransactionType.INCOME  -> input.copy(amount = abs(input.amount))
        }
        return data.insertTransaction(normalized)
    }

    fun update(input: Transaction): Boolean {
        require(input.id != 0L) { "Id is required to update." }
        require(input.amount != 0.0) { "Amount must be different from 0." }
        // Normalize amount sign according to type
        val normalized = when (input.type) {
            TransactionType.EXPENSE -> input.copy(amount = -abs(input.amount))
            TransactionType.INCOME  -> input.copy(amount = abs(input.amount))
        }
        return data.updateTransaction(normalized)
    }

    fun delete(id: Long): Boolean = data.deleteTransaction(id)

    /** Totals for a date range (income, expense) and balance */
    fun totals(range: DateRange? = null): Totals {
        val (income, expense) = data.getTotals(range)
        return Totals(income = income, expense = expense, balance = income - expense)
    }
}
