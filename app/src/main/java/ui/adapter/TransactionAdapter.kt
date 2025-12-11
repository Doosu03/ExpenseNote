package com.example.expensenote.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensenote.databinding.ItemTransactionBinding
import com.example.expensenote.entity.Transaction
import com.example.expensenote.entity.TransactionType
import com.example.expensenote.R

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private val transactions = mutableListOf<Transaction>()

    private var categoryIconMap: Map<String, String?> = emptyMap()

    fun submitList(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }

    fun setCategoryIcons(map: Map<String, String?>) {
        categoryIconMap = map
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    inner class ViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                tvCategory.text = transaction.note.ifEmpty { transaction.category }
                tvDate.text = transaction.date
                tvAmount.text = formatAmount(transaction.amount)

                val icon = getCategoryIcon(transaction.category)

                when (transaction.type) {
                    TransactionType.EXPENSE -> {
                        cvIcon.setCardBackgroundColor(root.context.getColor(R.color.expense_light))
                        tvIcon.text = icon
                        tvAmount.setTextColor(root.context.getColor(R.color.expense))
                    }

                    TransactionType.INCOME -> {
                        cvIcon.setCardBackgroundColor(root.context.getColor(R.color.income_light))
                        tvIcon.text = icon
                        tvAmount.setTextColor(root.context.getColor(R.color.income))
                    }
                }

                root.setOnClickListener {
                    onItemClick(transaction)
                }
            }
        }

        private fun formatAmount(amount: Double): String {
            val prefix = if (amount < 0) "-" else "+"
            val value = kotlin.math.abs(amount)
            val formatted = String.format("%,.0f", value)
            return "$prefix₡ $formatted"
        }

        private fun getCategoryIcon(category: String): String {
            val dynamicIcon = categoryIconMap[category]
            if (!dynamicIcon.isNullOrBlank()) return dynamicIcon
            return when (category) {
                "Food" -> "🍔"
                "Transport" -> "🚌"
                "Health" -> "🏥"
                "Entertainment" -> "🎮"
                "Home" -> "🏠"
                "Salary" -> "💰"
                else -> "📝"
            }
        }
    }
}
