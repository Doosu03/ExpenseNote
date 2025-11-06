package com.example.expensenote

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensenote.Data.DataLocator
import com.example.expensenote.databinding.ActivityMainBinding
import com.example.expensenote.entity.Transaction
import com.example.expensenote.entity.TransactionType
import com.example.expensenote.entity.TransactionQuery
import com.example.expensenote.ui.adapter.TransactionAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var transactionAdapter: TransactionAdapter

    private var isSearching: Boolean = false
    private var lastLoaded: List<Transaction> = emptyList()

    companion object {
        const val FILTERS_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFAB()
        setupSearch()
        setupFiltersButton()
        loadTransactions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "ExpenseNote"
    }

    private fun setupFiltersButton() {
        binding.btnOpenFilters.setOnClickListener {
            openFiltersActivity()
        }
    }

    private fun openFiltersActivity() {
        val intent = Intent(this, FiltersActivity::class.java)
        startActivityForResult(intent, FILTERS_REQUEST_CODE)
    }

    @Deprecated("Using onActivityResult for simplicity in this delivery")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILTERS_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.let {
                val filterType = it.getStringExtra("FILTER_TYPE")
                val dateFrom = it.getLongExtra("FILTER_DATE_FROM", 0)
                val dateTo = it.getLongExtra("FILTER_DATE_TO", 0)
                val categories = it.getStringArrayListExtra("FILTER_CATEGORIES")
                val sortBy = it.getStringExtra("SORT_BY")

                applyFilters(filterType, dateFrom, dateTo, categories, sortBy)
            }
        }
    }

    private fun applyFilters(
        filterType: String?,
        dateFrom: Long,
        dateTo: Long,
        categories: ArrayList<String>?,
        sortBy: String?
    ) {
        // For this task delivery we keep filters simple (not required by professor yet)
        loadTransactions()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            // When searching, tap goes straight to EDIT mode (FormActivity)
            if (isSearching) {
                val intent = Intent(this, FormActivity::class.java)
                intent.putExtra("TRANSACTION_ID", transaction.id)
                startActivity(intent)
            } else {
                // Normal flow → detail
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("TRANSACTION_ID", transaction.id)
                startActivity(intent)
            }
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

    private fun loadTransactions() {
        val transactions = DataLocator.data.getTransactions()
        lastLoaded = transactions
        transactionAdapter.submitList(transactions)
        updateBalance(transactions)
    }

    private fun updateBalance(transactions: List<Transaction>) {
        val income = transactions.filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
            .sumOf { kotlin.math.abs(it.amount) }
        val balance = income - expenses

        binding.tvBalance.text = formatCurrency(balance)
        binding.tvIncome.text = formatCurrency(income)
        binding.tvExpenses.text = formatCurrency(expenses)
    }

    private fun filterTransactions(query: String) {
        isSearching = query.isNotBlank()
        val result = if (query.isBlank()) {
            DataLocator.data.getTransactions()
        } else {
            DataLocator.data.getTransactions(TransactionQuery(text = query))
        }
        lastLoaded = result
        transactionAdapter.submitList(result)
        updateBalance(result)
    }

    private fun formatCurrency(amount: Double): String {
        return "₡ ${String.format("%,.0f", amount)}"
    }

    override fun onResume() {
        super.onResume()
        // Reload transactions when coming back from form or detail
        loadTransactions()
    }
}
