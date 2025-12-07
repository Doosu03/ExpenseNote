package com.example.expensenote

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensenote.Controller.CategoryController
import com.example.expensenote.Controller.TransactionController
import com.example.expensenote.Data.RemoteDataManager
import com.example.expensenote.databinding.ActivityMainBinding
import com.example.expensenote.entity.Transaction
import com.example.expensenote.entity.TransactionQuery
import com.example.expensenote.entity.TransactionType
import com.example.expensenote.ui.adapter.TransactionAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private val controller = TransactionController(RemoteDataManager)
    private val categoryController = CategoryController(RemoteDataManager)
    private var isSearching = false
    private var lastLoaded: List<Transaction> = emptyList()
    private var categoryIconMap: Map<String, String?> = emptyMap()
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
        setupCategoriesButton()
        loadCategoryIcons()
        loadTransactions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "ExpenseNote"
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { tx ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("TRANSACTION_STRING_ID", tx.stringId)
            startActivity(intent)
        }

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = transactionAdapter
        }
    }

    private fun setupFAB() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, FormActivity::class.java))
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            filterTransactions(text.toString())
        }
    }

    private fun setupFiltersButton() {
        binding.btnOpenFilters.setOnClickListener {
            val intent = Intent(this, FiltersActivity::class.java)
            startActivityForResult(intent, FILTERS_REQUEST_CODE)
        }
    }

    private fun setupCategoriesButton() {
        binding.btnManageCategories.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }
    }

    private fun loadCategoryIcons() {
        lifecycleScope.launch {
            try {
                val categories = categoryController.list()
                categoryIconMap = categories.associate { it.name to it.icon }
                transactionAdapter.setCategoryIcons(categoryIconMap)
            } catch (_: Exception) {
            }
        }
    }

    private fun loadTransactions() {
        lifecycleScope.launch {
            try {
                val transactions = controller.list()
                lastLoaded = transactions
                transactionAdapter.submitList(transactions)
                updateBalance(transactions)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error loading transactions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun filterTransactions(query: String) {
        isSearching = query.isNotBlank()

        lifecycleScope.launch {
            try {
                val result = if (query.isBlank()) {
                    controller.list()
                } else {
                    controller.list(TransactionQuery(text = query))
                }
                lastLoaded = result
                transactionAdapter.submitList(result)
                updateBalance(result)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error searching: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBalance(transactions: List<Transaction>) {
        var income = 0.0
        var expenses = 0.0

        for (tx in transactions) {
            if (tx.type == TransactionType.INCOME) {
                income += kotlin.math.abs(tx.amount)
            } else if (tx.type == TransactionType.EXPENSE) {
                expenses += kotlin.math.abs(tx.amount)
            }
        }

        val balance = income - expenses

        binding.tvBalance.text = formatCurrency(balance)
        binding.tvIncome.text = formatCurrency(income)
        binding.tvExpenses.text = formatCurrency(expenses)
    }

    private fun formatCurrency(value: Double): String {
        return "₡ " + String.format("%,.0f", value)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILTERS_REQUEST_CODE && resultCode == RESULT_OK) {
            val filterType = data?.getStringExtra("FILTER_TYPE")
            val dateFrom = data?.getLongExtra("FILTER_DATE_FROM", 0) ?: 0
            val dateTo = data?.getLongExtra("FILTER_DATE_TO", 0) ?: 0
            val categories = data?.getStringArrayListExtra("FILTER_CATEGORIES")
            val sortBy = data?.getStringExtra("SORT_BY")

            applyFilters(filterType, dateFrom, dateTo, categories, sortBy)
        }
    }

    private fun applyFilters(
        filterType: String?,
        dateFrom: Long,
        dateTo: Long,
        categories: ArrayList<String>?,
        sortBy: String?
    ) {
        lifecycleScope.launch {
            try {
                var transactions = controller.list()

                filterType?.let {
                    val t = TransactionType.valueOf(it)
                    transactions = transactions.filter { tx -> tx.type == t }
                }

                if (categories != null && categories.isNotEmpty()) {
                    transactions = transactions.filter { tx -> categories.contains(tx.category) }
                }

                lastLoaded = transactions
                transactionAdapter.submitList(transactions)
                updateBalance(transactions)

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error applying filters: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCategoryIcons()
        loadTransactions()
    }
}