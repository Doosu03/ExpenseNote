package com.example.expensenote

import android.os.Bundle
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.expensenote.Controller.CategoryController
import com.example.expensenote.Controller.TransactionController
import com.example.expensenote.Data.RemoteDataManager
import com.example.expensenote.databinding.ActivityFiltersBinding
import com.example.expensenote.entity.Category
import com.example.expensenote.entity.TransactionType
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.util.*

class FiltersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFiltersBinding
    private val categoryController = CategoryController(RemoteDataManager)
    private val transactionController = TransactionController(RemoteDataManager)

    private var selectedType: TransactionType? = null
    private val selectedCategoryIds = mutableSetOf<Long>()

    private var allCategories: List<Category> = emptyList()
    private val categoryCheckboxes = mutableMapOf<Long, CheckBox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiltersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTypeFilters()
        setupButtons()

        loadCategories()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Filtros"
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupTypeFilters() {
        binding.btnFilterAll.setOnClickListener {
            selectedType = null
            updateTypeButtons()
        }

        binding.btnFilterExpense.setOnClickListener {
            selectedType = TransactionType.EXPENSE
            updateTypeButtons()
        }

        binding.btnFilterIncome.setOnClickListener {
            selectedType = TransactionType.INCOME
            updateTypeButtons()
        }
    }

    private fun updateTypeButtons() {
        listOf(binding.btnFilterAll, binding.btnFilterExpense, binding.btnFilterIncome).forEach {
            it.strokeColor = getColorStateList(R.color.stroke)
            it.setBackgroundColor(getColor(android.R.color.transparent))
        }

        when (selectedType) {
            null -> {
                binding.btnFilterAll.strokeColor = getColorStateList(R.color.primary)
                binding.btnFilterAll.setBackgroundColor(getColor(R.color.primary_light))
            }
            TransactionType.EXPENSE -> {
                binding.btnFilterExpense.strokeColor = getColorStateList(R.color.expense)
                binding.btnFilterExpense.setBackgroundColor(getColor(R.color.expense_light))
            }
            TransactionType.INCOME -> {
                binding.btnFilterIncome.strokeColor = getColorStateList(R.color.income)
                binding.btnFilterIncome.setBackgroundColor(getColor(R.color.income_light))
            }
        }

        updateResultsCount()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                allCategories = categoryController.list()
                displayCategories(allCategories)
                updateResultsCount()
            } catch (e: Exception) {
                Toast.makeText(
                    this@FiltersActivity,
                    "Error loading categories: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayCategories(categories: List<Category>) {
        val categoriesContainer = findViewById<LinearLayout>(R.id.llCategoriesContainer)
        categoriesContainer?.removeAllViews()

        categories.forEach { category ->
            val categoryCard = createCategoryCard(category)
            categoriesContainer?.addView(categoryCard)
        }
    }

    private fun createCategoryCard(category: Category): MaterialCardView {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24
            }
            radius = 30f
            strokeWidth = 6
            strokeColor = getColor(R.color.stroke)
            cardElevation = 0f
            setCardBackgroundColor(getColor(R.color.background_light))
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(36, 36, 36, 36)
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val checkbox = CheckBox(this).apply {
            buttonTintList = getColorStateList(R.color.primary)
            setOnCheckedChangeListener { _, isChecked ->
                updateCategorySelection(category.id, isChecked)
                card.strokeColor = if (isChecked) {
                    getColor(R.color.primary)
                } else {
                    getColor(R.color.stroke)
                }
            }
        }
        categoryCheckboxes[category.id] = checkbox

        val categoryName = TextView(this).apply {
            text = "${getCategoryIcon(category.name)} ${getCategoryDisplayName(category.name)}"
            textSize = 15f
            setTextColor(getColor(R.color.text_primary))
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = 24
            }
        }

        val countText = TextView(this).apply {
            text = "0 movimientos"
            textSize = 13f
            setTextColor(getColor(R.color.text_secondary))
            tag = "count_${category.id}"
        }

        contentLayout.addView(checkbox)
        contentLayout.addView(categoryName)
        contentLayout.addView(countText)
        card.addView(contentLayout)

        return card
    }

    private fun getCategoryIcon(categoryName: String): String {
        val category = allCategories.find { it.name == categoryName }
        return category?.icon ?: when (categoryName) {
            "Food" -> "🍔"
            "Transport" -> "🚌"
            "Health" -> "🏥"
            "Entertainment" -> "🎮"
            "Home" -> "🏠"
            "Salary" -> "💰"
            else -> "📌"
        }
    }

    private fun getCategoryDisplayName(categoryName: String): String {
        return when (categoryName) {
            "Food" -> "Alimentación"
            "Transport" -> "Transporte"
            "Health" -> "Salud"
            "Entertainment" -> "Entretenimiento"
            "Home" -> "Hogar"
            "Salary" -> "Salario"
            else -> categoryName
        }
    }

    private fun updateCategorySelection(categoryId: Long, isSelected: Boolean) {
        if (isSelected) {
            selectedCategoryIds.add(categoryId)
        } else {
            selectedCategoryIds.remove(categoryId)
        }
        updateResultsCount()
    }

    private fun setupButtons() {
        binding.btnClearFilters.setOnClickListener {
            clearAllFilters()
        }

        binding.btnApplyFilters.setOnClickListener {
            applyFilters()
        }
    }

    private fun clearAllFilters() {
        selectedType = null
        selectedCategoryIds.clear()

        // Desmarcar todos los checkboxes
        categoryCheckboxes.values.forEach { it.isChecked = false }

        updateTypeButtons()
        updateResultsCount()
    }

    private fun applyFilters() {
        val selectedCategoryNames = allCategories
            .filter { selectedCategoryIds.contains(it.id) }
            .map { it.name }

        val intent = intent.apply {
            putExtra("FILTER_TYPE", selectedType?.name)
            putStringArrayListExtra("FILTER_CATEGORIES", ArrayList(selectedCategoryNames))
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun updateResultsCount() {
        lifecycleScope.launch {
            try {
                // Obtener TODAS las transacciones primero
                val allTransactions = transactionController.list()

                // Filtrar manualmente según los criterios seleccionados
                var filteredTransactions = allTransactions

                // Filtrar por tipo
                if (selectedType != null) {
                    filteredTransactions = filteredTransactions.filter { it.type == selectedType }
                }

                // Filtrar por categorías seleccionadas
                if (selectedCategoryIds.isNotEmpty()) {
                    val categoryNames = allCategories
                        .filter { selectedCategoryIds.contains(it.id) }
                        .map { it.name }
                    filteredTransactions = filteredTransactions.filter { categoryNames.contains(it.category) }
                }

                val count = filteredTransactions.size
                binding.tvResultsCount.text = "$count movimientos"

                // Actualizar contadores por categoría con TODAS las transacciones
                updateCategoryCounts(allTransactions)
            } catch (e: Exception) {
                binding.tvResultsCount.text = "0 movimientos"
                e.printStackTrace()
            }
        }
    }

    private fun updateCategoryCounts(transactions: List<com.example.expensenote.entity.Transaction>) {
        // Contar transacciones por categoría
        val countsByCategory = transactions.groupBy { it.category }.mapValues { it.value.size }

        // Actualizar los TextViews de conteo
        allCategories.forEach { category ->
            val countView = findViewById<LinearLayout>(R.id.llCategoriesContainer)
                ?.findViewWithTag<TextView>("count_${category.id}")
            val count = countsByCategory[category.name] ?: 0
            countView?.text = "$count movimientos"
        }
    }
}