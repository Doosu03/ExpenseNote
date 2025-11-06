package com.example.expensenote

// ============================================
// DetailActivity.kt - Detail
// ============================================

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.expensenote.Data.DataLocator
import com.example.expensenote.databinding.ActivityDetailBinding
import com.example.expensenote.entity.Transaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var transactionId: Long = -1
    private var current: Transaction? = null

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
            title = "Transaction details"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupButtons() {
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java)
            intent.putExtra("TRANSACTION_ID", transactionId)
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
        val tx = DataLocator.data.getTransaction(transactionId)
        if (tx == null) {
            finish()
            return
        }
        current = tx
        displayTransaction(tx)
    }

    private fun displayTransaction(transaction: Transaction) {
        binding.apply {
            tvDetailAmount.text = formatCurrency(kotlin.math.abs(transaction.amount))
            val typeLabel = transaction.type.name.lowercase().replaceFirstChar { it.titlecase() }
            tvDetailType.text = "$typeLabel • ${transaction.category}"
            tvDetailCategory.text = "🍔 ${transaction.category}" // you can map emoji by category if needed
            tvDetailDate.text = transaction.date
            tvDetailTransactionType.text = typeLabel
            tvDetailNote.text = transaction.note

            transaction.photoUri?.let { uriStr ->
                // Load with your preferred image loader later (Coil/Glide)
                // Coil example:
                // ivReceiptPhoto.load(uriStr)
            }
        }
    }

    private fun deleteTransaction() {
        if (transactionId != -1L) {
            DataLocator.data.deleteTransaction(transactionId)
            Snackbar.make(binding.root, getString(R.string.transaction_deleted), Snackbar.LENGTH_SHORT).show()
            finish()
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
