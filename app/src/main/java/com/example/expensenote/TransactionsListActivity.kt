package com.example.expensenote

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensenote.Data.DataLocator
import com.example.expensenote.databinding.ActivityTransactionsListBinding
import com.example.expensenote.ui.adapter.TransactionWithPhotoAdapter

class TransactionsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionsListBinding
    private lateinit var adapter: TransactionWithPhotoAdapter

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
                putExtra("TRANSACTION_ID", tx.id)
            }
            startActivity(intent)
        }

        binding.rvTransactionsWithPhoto.layoutManager = LinearLayoutManager(this)
        binding.rvTransactionsWithPhoto.adapter = adapter

        adapter.submit(DataLocator.data.getTransactions())
    }

    override fun onResume() {
        super.onResume()
        adapter.submit(DataLocator.data.getTransactions()) // refresh after edits
    }
}
