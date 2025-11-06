package com.example.expensenote

// ============================================
// FormActivity.kt - Form (Create / Update)
// ============================================

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.expensenote.Data.DataLocator
import com.example.expensenote.databinding.ActivityFormBinding
import com.example.expensenote.entity.Transaction
import com.example.expensenote.entity.TransactionType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private var selectedType: TransactionType = TransactionType.EXPENSE
    private var selectedDate: Date = Date()
    private var photoUri: Uri? = null
    private var transactionId: Long? = null

    // Pick from gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUri = it
            // TODO: show preview if you have an ImageView
            // binding.ivPhotoPreview.setImageURI(it)
        }
    }

    // Take photo with camera
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Photo saved in photoUri
            // TODO: show preview
            // binding.ivPhotoPreview.setImageURI(photoUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionId = intent.getLongExtra("TRANSACTION_ID", -1).takeIf { it != -1L }

        setupToolbar()
        setupTypeSelector()
        setupCategorySpinner()
        setupDatePicker()
        setupPhotoUpload()
        setupButtons()

        if (transactionId != null) {
            loadTransaction(transactionId!!)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (transactionId == null) "New transaction" else "Edit transaction"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupTypeSelector() {
        binding.btnExpense.setOnClickListener { selectType(TransactionType.EXPENSE) }
        binding.btnIncome.setOnClickListener { selectType(TransactionType.INCOME) }
        selectType(TransactionType.EXPENSE)
    }

    private fun selectType(type: TransactionType) {
        selectedType = type

        // Reset visuals
        binding.btnExpense.strokeColor = getColorStateList(R.color.stroke)
        binding.btnIncome.strokeColor = getColorStateList(R.color.stroke)
        binding.btnExpense.setBackgroundColor(getColor(android.R.color.transparent))
        binding.btnIncome.setBackgroundColor(getColor(android.R.color.transparent))

        // Highlight selected
        when (type) {
            TransactionType.EXPENSE -> {
                binding.btnExpense.strokeColor = getColorStateList(R.color.expense)
                binding.btnExpense.setBackgroundColor(getColor(R.color.expense_light))
            }
            TransactionType.INCOME -> {
                binding.btnIncome.strokeColor = getColorStateList(R.color.income)
                binding.btnIncome.setBackgroundColor(getColor(R.color.income_light))
            }
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf(
            "Food",
            "Transport",
            "Health",
            "Entertainment",
            "Home",
            "Salary",
            "Other"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(selectedDate))

        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply { time = selectedDate }

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.time
                    binding.etDate.setText(dateFormat.format(selectedDate))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupPhotoUpload() {
        binding.cvPhotoUpload.setOnClickListener { showPhotoOptions() }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Take photo", "Choose from gallery", "Cancel")

        MaterialAlertDialogBuilder(this)
            .setTitle("Add photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePicture()
                    1 -> pickImageLauncher.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun takePicture() {
        val photoFile = File(cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            photoFile
        )
        photoUri = uri
        takePictureLauncher.launch(uri)
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            if (transactionId == null) {
                // Create directly
                saveTransaction(createMode = true)
            } else {
                // Confirm update
                MaterialAlertDialogBuilder(this)
                    .setTitle("Update transaction?")
                    .setMessage("Do you want to save the changes?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Update") { _, _ ->
                        saveTransaction(createMode = false)
                    }
                    .show()
            }
        }

        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun saveTransaction(createMode: Boolean) {
        val amountText = binding.etAmount.text.toString()
        val amount = amountText.toDoubleOrNull()
        val category = binding.actvCategory.text.toString()
        val note = binding.etNote.text.toString()

        if (amount == null || amount <= 0) {
            binding.etAmount.error = "Enter a valid amount"
            return
        }
        if (category.isEmpty()) {
            binding.actvCategory.error = "Select a category"
            return
        }

        val tx = Transaction(
            id = transactionId ?: 0,
            amount = if (selectedType == TransactionType.EXPENSE) -amount else amount,
            category = category,
            type = selectedType,
            date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate),
            note = note,
            photoUri = photoUri?.toString()
        )

        if (createMode) {
            DataLocator.data.insertTransaction(tx)
            Snackbar.make(binding.root, getString(R.string.transaction_saved), Snackbar.LENGTH_SHORT).show()
        } else {
            DataLocator.data.updateTransaction(tx)
            Snackbar.make(binding.root, getString(R.string.transaction_saved), Snackbar.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun loadTransaction(id: Long) {
        val tx = DataLocator.data.getTransaction(id) ?: return
        // Populate fields
        binding.etAmount.setText(kotlin.math.abs(tx.amount).toString())
        binding.actvCategory.setText(tx.category, false)
        binding.etNote.setText(tx.note)
        selectType(tx.type)
        binding.etDate.setText(tx.date)
        tx.photoUri?.let {
            photoUri = Uri.parse(it)
            // binding.ivPhotoPreview.setImageURI(photoUri)
        }
    }
}
