package com.example.expensenote

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.expensenote.Controller.TransactionController
import com.example.expensenote.Data.RemoteDataManager
import com.example.expensenote.databinding.ActivityFormBinding
import com.example.expensenote.entity.Transaction
import com.example.expensenote.entity.TransactionType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private val controller = TransactionController(RemoteDataManager)

    private var selectedType: TransactionType = TransactionType.EXPENSE
    private var selectedDate: Date = Date()
    private var photoUri: Uri? = null
    private var transactionStringId: String? = null  // Firebase usa String IDs

    // Bitmap kept in memory to store on save (as required by the task)
    private var selectedReceiptBitmap: Bitmap? = null
    private var uploadedPhotoUrl: String? = null  // URL devuelta por el API

    // Pick from gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUri = it
            selectedReceiptBitmap = decodeBitmapFromUri(it)
            showPreview(selectedReceiptBitmap)
        }
    }

    // Take photo with camera
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            selectedReceiptBitmap = decodeBitmapFromUri(photoUri!!)
            showPreview(selectedReceiptBitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionStringId = intent.getStringExtra("TRANSACTION_STRING_ID")

        // DEBUG: Verificar si se recibió el ID
        android.util.Log.d("FormActivity", "Received ID: $transactionStringId")

        setupToolbar()
        setupTypeSelector()
        setupCategorySpinner()
        setupDatePicker()
        setupPhotoUpload()
        setupButtons()

        if (transactionStringId != null && transactionStringId!!.isNotEmpty()) {
            loadTransaction(transactionStringId!!)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (transactionStringId == null) "New transaction" else "Edit transaction"
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
            if (transactionStringId == null) {
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

        lifecycleScope.launch {
            try {
                // 1. Subir imagen si hay una nueva
                if (selectedReceiptBitmap != null && uploadedPhotoUrl == null) {
                    val base64 = bitmapToBase64(selectedReceiptBitmap!!)
                    uploadedPhotoUrl = RemoteDataManager.uploadImageSuspend(base64)
                }

                // 2. Crear objeto Transaction
                val tx = Transaction(
                    id = 0, // No usado
                    stringId = transactionStringId ?: "", // IMPORTANTE: Usar el stringId guardado
                    amount = if (selectedType == TransactionType.EXPENSE) -amount else amount,
                    category = category,
                    type = selectedType,
                    date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate),
                    note = note,
                    photoUri = uploadedPhotoUrl,  // Guardar URL de Firebase Storage
                    photoBitmap = null  // No guardamos Bitmap en API
                )

                // DEBUG: Verificar el stringId antes de actualizar
                android.util.Log.d("FormActivity", "Updating with stringId: ${tx.stringId}")

                // 3. Crear o actualizar
                if (createMode) {
                    val created = controller.create(tx)
                    if (created != null) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.transaction_saved),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(this@FormActivity, "Error creating transaction", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val updated = controller.update(tx)
                    if (updated) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.transaction_saved),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(this@FormActivity, "Error updating transaction", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@FormActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadTransaction(id: String) {
        lifecycleScope.launch {
            try {
                android.util.Log.d("FormActivity", "Loading transaction with ID: $id")

                val tx = controller.get(id)
                if (tx == null) {
                    Toast.makeText(this@FormActivity, "Transaction not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                // IMPORTANTE: Guardar el stringId para poder editar
                transactionStringId = tx.stringId
                android.util.Log.d("FormActivity", "Loaded transaction, stringId: $transactionStringId")

                // Populate fields
                binding.etAmount.setText(kotlin.math.abs(tx.amount).toString())
                binding.actvCategory.setText(tx.category, false)
                binding.etNote.setText(tx.note)
                selectType(tx.type)

                // Parse date to set in calendar
                try {
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    selectedDate = dateFormat.parse(tx.date) ?: Date()
                    binding.etDate.setText(tx.date)
                } catch (e: Exception) {
                    binding.etDate.setText(tx.date)
                }

                // Cargar imagen si existe URL
                tx.photoUri?.let { url ->
                    uploadedPhotoUrl = url
                    // TODO: Cargar imagen desde URL con Glide/Coil si quieres mostrar preview
                    // Por ahora solo guardamos la URL
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@FormActivity,
                    "Error loading transaction: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    // ---- Image helpers ----

    private fun decodeBitmapFromUri(uri: Uri, maxSize: Int = 1280): Bitmap? {
        return try {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

            options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun showPreview(bitmap: Bitmap?) {
        if (bitmap != null) {
            binding.ivPhotoPreview.visibility = View.VISIBLE
            binding.ivPhotoPreview.setImageBitmap(bitmap)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}