package com.example.expensenote.Data

import com.example.expensenote.entity.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicLong

object MemoryDataManager : IDataManager {

    // Autoincrement for IDs
    private val txIdGen = AtomicLong(1)
    private val catIdGen = AtomicLong(1)

    // Seed categories (English code names; UI text puede venir de strings.xml)
    private val categories = mutableListOf(
        Category(id = catIdGen.getAndIncrement(), name = "Food"),
        Category(id = catIdGen.getAndIncrement(), name = "Transport"),
        Category(id = catIdGen.getAndIncrement(), name = "Health"),
        Category(id = catIdGen.getAndIncrement(), name = "Entertainment"),
        Category(id = catIdGen.getAndIncrement(), name = "Home"),
        Category(id = catIdGen.getAndIncrement(), name = "Salary"),
        Category(id = catIdGen.getAndIncrement(), name = "Other"),
    )

    // Transactions in memory
    private val transactions = mutableListOf<Transaction>()

    // ----------------- Transactions -----------------
    override fun getTransactions(query: TransactionQuery?): List<Transaction> {
        var list = transactions.toList()

        query?.let { q ->
            q.text?.takeIf { it.isNotBlank() }?.let { text ->
                val t = text.trim().lowercase(Locale.getDefault())
                list = list.filter {
                    it.note.lowercase(Locale.getDefault()).contains(t) ||
                            it.category.lowercase(Locale.getDefault()).contains(t)
                }
            }

            if (q.type != null) {
                list = list.filter { it.type == q.type }
            }

            if (q.categoryIds.isNotEmpty()) {
                // Como Transaction guarda category por nombre, mapeamos ids→nombres aquí
                val names = categories.filter { q.categoryIds.contains(it.id) }.map { it.name }.toSet()
                list = list.filter { names.contains(it.category) }
            }

            q.dateRange?.let { dr ->
                // date es String; hacemos un intento de parse con formatos comunes
                val fmts = listOf(
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
                    SimpleDateFormat("d-MMM-yyyy", Locale.getDefault()),
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                )

                fun parseOrNull(s: String): Date? {
                    for (f in fmts) try { return f.parse(s) } catch (_: Exception) {}
                    return null
                }

                list = list.filter { tx ->
                    val d = parseOrNull(tx.date) ?: return@filter true // si no se puede parsear, no filtramos
                    val isAfterFrom = dr.from?.let { d >= Date.from(it.atStartOfDay(TimeZone.getDefault().toZoneId()).toInstant()) } ?: true
                    val isBeforeTo = dr.to?.let { d <= Date.from(it.atStartOfDay(TimeZone.getDefault().toZoneId()).toInstant()) } ?: true
                    isAfterFrom && isBeforeTo
                }
            }
        }

        return list
    }

    override fun getTransaction(id: Long): Transaction? =
        transactions.firstOrNull { it.id == id }

    override fun insertTransaction(tx: Transaction): Transaction {
        val newTx = tx.copy(id = txIdGen.getAndIncrement())
        transactions.add(0, newTx) // al inicio para que se vea arriba
        return newTx
    }

    override fun updateTransaction(tx: Transaction): Boolean {
        val idx = transactions.indexOfFirst { it.id == tx.id }
        if (idx == -1) return false
        transactions[idx] = tx
        return true
    }

    override fun deleteTransaction(id: Long): Boolean =
        transactions.removeIf { it.id == id }

    override fun getTotals(range: DateRange?): Pair<Double, Double> {
        val list = getTransactions(
            if (range == null) null else TransactionQuery(dateRange = range)
        )
        val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { kotlin.math.abs(it.amount) }
        return Pair(income, expense)
    }

    // ----------------- Categories -----------------
    override fun getCategories(): List<Category> = categories.toList()

    override fun getCategory(id: Long): Category? =
        categories.firstOrNull { it.id == id }

    override fun insertCategory(cat: Category): Category {
        val c = cat.copy(id = catIdGen.getAndIncrement())
        categories.add(c)
        return c
    }

    override fun updateCategory(cat: Category): Boolean {
        val idx = categories.indexOfFirst { it.id == cat.id }
        if (idx == -1) return false
        categories[idx] = cat
        return true
    }

    override fun deleteCategory(id: Long): Boolean =
        categories.removeIf { it.id == id }
}
