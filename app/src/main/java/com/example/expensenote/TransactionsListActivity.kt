package com.example.expensenote

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensenote.Controller.TransactionController
import com.example.expensenote.Data.RemoteDataManager
import com.example.expensenote.databinding.ActivityTransactionsListBinding
import com.example.expensenote.ui.adapter.TransactionWithPhotoAdapter
import kotlinx.coroutines.launch

class TransactionsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionsListBinding
    private lateinit var adapter: TransactionWithPhotoAdapter
    private val controller = TransactionController(RemoteDataManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Transactions list"

        adapter = TransactionWithPhotoAdapter { tx ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("TRANSACTION_STRING_ID", tx.stringId)
            }
            startActivity(intent)
        }

        binding.rvTransactionsWithPhoto.layoutManager = LinearLayoutManager(this)
        binding.rvTransactionsWithPhoto.adapter = adapter

        loadTransactions()
    }

    private fun loadTransactions() {
        lifecycleScope.launch {
            try {
                val transactions = controller.list()
                adapter.submit(transactions)
            } catch (e: Exception) {
                Toast.makeText(
                    this@TransactionsListActivity,
                    "Error loading transactions: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTransactions() // refresh after edits
    }
}