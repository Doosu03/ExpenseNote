package com.example.expensenote.Controller

import com.example.expensenote.Data.RemoteDataManager
import com.example.expensenote.entity.Transaction
import com.example.expensenote.entity.TransactionQuery
import com.example.expensenote.entity.TransactionType
import kotlin.math.abs

class TransactionController(private val data: RemoteDataManager) {

    suspend fun list(query: TransactionQuery? = null): List<Transaction> =
        data.getTransactionsSuspend(query)

    suspend fun get(id: String): Transaction? =
        data.getTransactionSuspend(id)

    suspend fun create(input: Transaction): Transaction? {
        require(input.amount != 0.0) { "Amount must be different from 0." }
        val normalized = when (input.type) {
            TransactionType.EXPENSE -> input.copy(amount = -abs(input.amount))
            TransactionType.INCOME  -> input.copy(amount = abs(input.amount))
        }
        return data.insertTransactionSuspend(normalized)
    }

    suspend fun update(input: Transaction): Boolean {
        require(input.id != 0L) { "Id is required to update." }
        require(input.amount != 0.0) { "Amount must be different from 0." }
        val normalized = when (input.type) {
            TransactionType.EXPENSE -> input.copy(amount = -abs(input.amount))
            TransactionType.INCOME  -> input.copy(amount = abs(input.amount))
        }
        return data.updateTransactionSuspend(normalized)
    }

    suspend fun delete(id: String): Boolean =
        data.deleteTransactionSuspend(id)

    suspend fun getTotals(): Triple<Double, Double, Double> =
        data.getTotalsSuspend()
}