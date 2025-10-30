package com.example.expensenote

// ============================================
// DetailActivity.kt - Detalle
// ============================================

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.expensenote.databinding.ActivityDetailBinding
import com.google.android.filament.View

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var transactionId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionId = intent.getLongExtra("TRANSACTION_ID", -1)

        setupToolbar()
        setupButtons()
        loadTransaction()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Detalle del movimiento"
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java)
            intent.putExtra("TRANSACTION_ID", transactionId)
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnDialogCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnDialogConfirm).setOnClickListener {
            deleteTransaction()
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }

    private fun loadTransaction() {
        // Cargar desde Room
        // viewModel.getTransaction(transactionId).observe(this) { transaction ->
        //     displayTransaction(transaction)
        // }

        // Ejemplo con datos dummy:
        displayTransaction(
            Transaction(
                id = transactionId,
                amount = -6500.0,
                category = "Alimentación",
                type = TransactionType.EXPENSE,
                date = "15 Octubre 2025",
                note = "Almuerzo en restaurante"
            )
        )
    }

    private fun displayTransaction(transaction: Transaction) {
        binding.apply {
            tvDetailAmount.text = formatCurrency(transaction.amount)
            tvDetailType.text = "${transaction.type.displayName} • ${transaction.category}"
            tvDetailCategory.text = "🍔 ${transaction.category}"
            tvDetailDate.text = transaction.date
            tvDetailTransactionType.text = transaction.type.displayName
            tvDetailNote.text = transaction.note

            // Cargar foto si existe
            transaction.photoUri?.let { uri ->
                // Cargar imagen con Glide o Coil
                // Glide.with(this@DetailActivity).load(uri).into(ivReceiptPhoto)
            }
        }
    }

    private fun deleteTransaction() {
        // Eliminar de Room
        // viewModel.deleteTransaction(transactionId)
    }

    private fun formatCurrency(amount: Double): String {
        return "₡ ${String.format("%,.0f", Math.abs(amount))}"
    }
}