package com.example.expensenote

// ============================================
// MainActivity.kt - Lista de movimientos
// ============================================

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensenote.entity.Transaction
import com.example.expensenote.entity.TransactionType
import com.google.android.material.chip.Chip
import com.example.expensenote.databinding.ActivityMainBinding
import com.example.expensenote.ui.adapter.TransactionAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFAB()
        setupSearch()
        setupFilters()
        loadTransactions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "ExpenseNote"
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            // Click en un item
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("TRANSACTION_ID", transaction.id)
            startActivity(intent)
        }

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = transactionAdapter
        }
    }

    private fun setupFAB() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            filterTransactions(text.toString())
        }
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipAll -> filterByCategory(null)
                R.id.chipFood -> filterByCategory("Food")
                R.id.chipTransport -> filterByCategory("Transport")
                R.id.chipHealth -> filterByCategory("Health")
            }
        }
    }

    private fun loadTransactions() {
        // Aquí cargarías las transacciones desde Room
        // Ejemplo con datos dummy:
        val transactions = listOf(
            Transaction(
                id = 1,
                amount = -6500.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                date = "15 Oct 2025",
                note = "Lunch"
            ),
            Transaction(
                id = 2,
                amount = 850000.0,
                category = "Salary",
                type = TransactionType.INCOME,
                date = "14 Oct 2025",
                note = "Monthly salary"
            )
        )

        transactionAdapter.submitList(transactions)
        updateBalance(transactions)
    }

    private fun updateBalance(transactions: List<Transaction>) {
        val income = transactions.filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        val balance = income + expenses

        binding.tvBalance.text = formatCurrency(balance)
        binding.tvIncome.text = formatCurrency(income)
        binding.tvExpenses.text = formatCurrency(Math.abs(expenses))
    }

    private fun filterTransactions(query: String) {
        // Implementar filtro de búsqueda
    }

    private fun filterByCategory(category: String?) {
        // Implementar filtro por categoría
    }

    private fun formatCurrency(amount: Double): String {
        return "₡ ${String.format("%,.0f", amount)}"
    }
}