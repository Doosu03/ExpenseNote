package com.example.expensenote

// ============================================
// FormActivity.kt - Formulario
// ============================================

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.expensenote.databinding.ActivityFormBinding
import com.example.expensenote.entity.Transaction
import com.example.expensenote.entity.TransactionType
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private var selectedType: TransactionType = TransactionType.EXPENSE
    private var selectedDate: Date = Date()
    private var photoUri: Uri = null
    private var transactionId: Long? = null

    // Launcher para seleccionar imagen de galería
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUri = it
            // Mostrar preview de la imagen
        }
    }

    // Launcher para tomar foto con cámara
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // La foto se guardó en photoUri
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
            title = if (transactionId == null) "Nuevo movimiento" else "Editar movimiento"
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupTypeSelector() {
        binding.btnExpense.setOnClickListener {
            selectType(TransactionType.EXPENSE)
        }

        binding.btnIncome.setOnClickListener {
            selectType(TransactionType.INCOME)
        }

        selectType(TransactionType.EXPENSE)
    }

    private fun selectType(type: TransactionType) {
        selectedType = type

        // Reset button states
        binding.btnExpense.strokeColor = getColorStateList(R.color.stroke)
        binding.btnIncome.strokeColor = getColorStateList(R.color.stroke)

        // Highlight selected button
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
            "Alimentación",
            "Transporte",
            "Salud",
            "Entretenimiento",
            "Hogar",
            "Salario",
            "Otros"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es"))
        binding.etDate.setText(dateFormat.format(selectedDate))

        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

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
        binding.cvPhotoUpload.setOnClickListener {
            showPhotoOptions()
        }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Tomar foto", "Elegir de galería", "Cancelar")

        MaterialAlertDialogBuilder(this)
            .setTitle("Agregar foto")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePicture()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun takePicture() {
        val photoFile = File(cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            photoFile
        )
        takePictureLauncher.launch(photoUri)
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            saveTransaction()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveTransaction() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        val category = binding.actvCategory.text.toString()
        val note = binding.etNote.text.toString()

        if (amount == null || category.isEmpty()) {
            // Mostrar error
            return
        }

        val transaction = Transaction(
            id = transactionId ?: 0,
            amount = if (selectedType == TransactionType.EXPENSE) -amount else amount,
            category = category,
            type = selectedType,
            date = SimpleDateFormat("dd MMM yyyy", Locale("es")).format(selectedDate),
            note = note,
            photoUri = photoUri?.toString()
        )

        // Guardar en Room database
        // viewModel.saveTransaction(transaction)

        finish()
    }

    private fun loadTransaction(id: Long) {
        // Cargar transacción desde Room
        // viewModel.getTransaction(id).observe(this) { transaction ->
        //     // Llenar los campos con los datos
        // }
    }
}