package com.example.expensenote

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.expensenote.Controller.TransactionController
import com.example.expensenote.Data.RemoteDataManager
import com.example.expensenote.databinding.ActivityDetailBinding
import com.example.expensenote.entity.Transaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var transactionStringId: String = ""
    private var current: Transaction? = null
    private val controller = TransactionController(RemoteDataManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionStringId = intent.getStringExtra("TRANSACTION_STRING_ID") ?: ""

        setupToolbar()
        setupButtons()
        loadTransaction()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Transaction details"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupButtons() {
        binding.btnEdit.setOnClickListener {
            val intent = android.content.Intent(this, FormActivity::class.java)
            // IMPORTANTE: Pasar el stringId, no el transactionStringId local
            current?.let {
                intent.putExtra("TRANSACTION_STRING_ID", it.stringId)
            }
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener { showDeleteDialog() }
    }

    private fun showDeleteDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnDialogCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnDialogConfirm).setOnClickListener {
            deleteTransaction()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadTransaction() {
        lifecycleScope.launch {
            try {
                val tx = controller.get(transactionStringId)
                if (tx == null) {
                    Toast.makeText(
                        this@DetailActivity,
                        "Transaction not found",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }
                current = tx
                displayTransaction(tx)
            } catch (e: Exception) {
                Toast.makeText(
                    this@DetailActivity,
                    "Error loading transaction: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun displayTransaction(transaction: Transaction) {
        binding.apply {
            tvDetailAmount.text = formatCurrency(kotlin.math.abs(transaction.amount))
            val typeLabel = transaction.type.name.lowercase().replaceFirstChar { it.titlecase() }
            tvDetailType.text = "$typeLabel • ${transaction.category}"
            tvDetailCategory.text = "📌 ${transaction.category}"
            tvDetailDate.text = transaction.date
            tvDetailTransactionType.text = typeLabel
            tvDetailNote.text = transaction.note

            // Cargar imagen desde URL
            when {
                transaction.photoUri != null -> {
                    // TODO: Usar Glide o Coil para cargar desde URL
                    // Por ahora solo mostramos placeholder
                    ivReceiptPhoto.setImageResource(R.drawable.ic_receipt_placeholder)
                }
                else -> ivReceiptPhoto.setImageResource(R.drawable.ic_receipt_placeholder)
            }
        }
    }

    private fun deleteTransaction() {
        lifecycleScope.launch {
            try {
                val success = controller.delete(transactionStringId)
                if (success) {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.transaction_deleted),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@DetailActivity,
                        "Error deleting transaction",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@DetailActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return "₡ ${String.format("%,.0f", amount)}"
    }

    override fun onResume() {
        super.onResume()
        // Reload in case it was edited
        loadTransaction()
    }
}