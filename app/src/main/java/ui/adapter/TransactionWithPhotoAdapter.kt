package com.example.expensenote.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensenote.databinding.ItemTransactionWithPhotoBinding
import com.example.expensenote.entity.Transaction
import com.example.expensenote.entity.TransactionType
import kotlin.math.abs

class TransactionWithPhotoAdapter(
    private val onClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionWithPhotoAdapter.VH>() {

    private val items = mutableListOf<Transaction>()

    fun submit(list: List<Transaction>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTransactionWithPhotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class VH(private val b: ItemTransactionWithPhotoBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(tx: Transaction) {
            // Title + subtitle
            b.tvTitle.text = if (tx.note.isNotBlank()) tx.note else tx.category
            b.tvSubtitle.text = "${tx.date} · ${tx.category}"

            // Amount color by type
            val prefix = if (tx.type == TransactionType.EXPENSE) "-" else "+"
            b.tvAmount.text = "$prefix₡ ${String.format("%,.0f", abs(tx.amount))}"

            // Thumbnail: prefer bitmap, else URI, else placeholder
            when {
                tx.photoBitmap != null -> b.ivReceiptThumb.setImageBitmap(tx.photoBitmap)
                tx.photoUri != null    -> b.ivReceiptThumb.setImageURI(Uri.parse(tx.photoUri))
                else                   -> { /* keep placeholder */ }
            }

            b.root.setOnClickListener { onClick(tx) }
        }
    }
}
