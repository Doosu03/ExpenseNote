package com.example.expensenote

// ============================================
// FiltersActivity.kt - Filtros
// ============================================

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.expensenote.databinding.ActivityFiltersBinding
import com.example.expensenote.entity.TransactionType
import java.text.SimpleDateFormat
import java.util.*

class FiltersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFiltersBinding
    private var selectedType: TransactionType? = null
    private var dateFrom: Date? = null
    private var dateTo: Date? = null
    private val selectedCategories = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiltersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTypeFilters()
        setupDateFilters()
        setupCategoryFilters()
        setupSortSpinner()
        setupButtons()
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
        // Reset all buttons
        listOf(binding.btnFilterAll, binding.btnFilterExpense, binding.btnFilterIncome).forEach {
            it.strokeColor = getColorStateList(R.color.stroke)
            it.setBackgroundColor(getColor(android.R.color.transparent))
        }

        // Highlight selected
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

    private fun setupDateFilters() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es"))

        binding.etDateFrom.setOnClickListener {
            showDatePicker { date ->
                dateFrom = date
                binding.etDateFrom.setText(dateFormat.format(date))
                updateResultsCount()
            }
        }

        binding.etDateTo.setOnClickListener {
            showDatePicker { date ->
                dateTo = date
                binding.etDateTo.setText(dateFormat.format(date))
                updateResultsCount()
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupCategoryFilters() {
        binding.cbFood.setOnCheckedChangeListener { _, isChecked ->
            updateCategory("Food", isChecked)
        }
        binding.cbTransport.setOnCheckedChangeListener { _, isChecked ->
            updateCategory("Transport", isChecked)
        }
        binding.cbHealth.setOnCheckedChangeListener { _, isChecked ->
            updateCategory("Health", isChecked)
        }
        binding.cbEntertainment.setOnCheckedChangeListener { _, isChecked ->
            updateCategory("Entertainment", isChecked)
        }
        binding.cbHome.setOnCheckedChangeListener { _, isChecked ->
            updateCategory("Home", isChecked)
        }
        binding.cbSalary.setOnCheckedChangeListener { _, isChecked ->
            updateCategory("Salary", isChecked)
        }
    }

    private fun updateCategory(category: String, isSelected: Boolean) {
        if (isSelected) {
            selectedCategories.add(category)
        } else {
            selectedCategories.remove(category)
        }
        updateResultsCount()
    }

    private fun setupSortSpinner() {
        val sortOptions = arrayOf(
            "Fecha (más reciente)",
            "Fecha (más antiguo)",
            "Monto (mayor a menor)",
            "Monto (menor a mayor)",
            "Categoría (A-Z)"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sortOptions)
        binding.actvSortBy.setAdapter(adapter)
        binding.actvSortBy.setText(sortOptions[0], false)
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
        dateFrom = null
        dateTo = null
        selectedCategories.clear()

        binding.etDateFrom.text?.clear()
        binding.etDateTo.text?.clear()
        binding.cbFood.isChecked = false
        binding.cbTransport.isChecked = false
        binding.cbHealth.isChecked = false
        binding.cbEntertainment.isChecked = false
        binding.cbHome.isChecked = false
        binding.cbSalary.isChecked = false

        updateTypeButtons()
        updateResultsCount()
    }

    private fun applyFilters() {
        // Aplicar filtros y devolver resultado
        val intent = intent.apply {
            putExtra("FILTER_TYPE", selectedType?.name)
            putExtra("FILTER_DATE_FROM", dateFrom?.time)
            putExtra("FILTER_DATE_TO", dateTo?.time)
            putStringArrayListExtra("FILTER_CATEGORIES", ArrayList(selectedCategories))
            putExtra("SORT_BY", binding.actvSortBy.text.toString())
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun updateResultsCount() {
        // Aquí calcularías el número real de transacciones que coinciden con los filtros
        // Ejemplo:
        val count = 28 // Este número vendría de tu ViewModel/Repository
        binding.tvResultsCount.text = "$count movimientos"
    }
}