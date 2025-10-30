package com.example.expensenote.Controller

import com.example.expensenote.Data.IDataManager
import com.example.expensenote.entity.*
import com.example.expensenote.entity.DateRange
import com.example.expensenote.entity.Totals
import com.example.expensenote.entity.Transaction
import com.example.expensenote.entity.TransactionQuery

class TransactionController(private val data: IDataManager) {

    fun list(query: TransactionQuery? = null): List<Transaction> =
        data.getTransactions(query)

    fun get(id: Long): Transaction? = data.getTransaction(id)

    fun create(input: Transaction): Transaction {
        require(input.amount > 0.0) { "El monto debe ser mayor a 0." }
        requireNotNull(input.type) { "El tipo de movimiento es requerido." }
        return data.insertTransaction(input)
    }

    fun update(input: Transaction): Boolean {
        require(input.id != 0L) { "El id es requerido para actualizar." }
        require(input.amount > 0.0) { "El monto debe ser mayor a 0." }
        return data.updateTransaction(input)
    }

    fun delete(id: Long): Boolean = data.deleteTransaction(id)

    /** Totales para un rango (ingresos, gastos) y balance */
    fun totals(range: DateRange? = null): Totals {
        val (income, expense) = data.getTotals(range)
        return Totals(income = income, expense = expense, balance = income - expense)
    }
}
