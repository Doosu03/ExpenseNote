package com.example.expensenote.Data

import com.example.expensenote.Entity.*
import com.example.expensenote.entity.DateRange
import com.example.expensenote.entity.Transaction
import com.example.expensenote.entity.TransactionQuery

interface IDataManager {

    // ----------------- Transacciones -----------------
    fun getTransactions(query: TransactionQuery? = null): List<Transaction>
    fun getTransaction(id: Long): Transaction?
    fun insertTransaction(tx: Transaction): Transaction
    fun updateTransaction(tx: Transaction): Boolean
    fun deleteTransaction(id: Long): Boolean
    fun getTotals(range: DateRange? = null): Pair<Double, Double>

    // ----------------- Categorías -----------------
    fun getCategories(): List<Category>
    fun getCategory(id: Long): Category?
    fun insertCategory(cat: Category): Category
    fun updateCategory(cat: Category): Boolean
    fun deleteCategory(id: Long): Boolean
}
