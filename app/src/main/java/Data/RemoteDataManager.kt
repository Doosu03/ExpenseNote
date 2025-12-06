package com.example.expensenote.Data

import com.example.expensenote.Data.network.ApiService
import com.example.expensenote.Data.network.CategoryDTO
import com.example.expensenote.Data.network.RetrofitClient
import com.example.expensenote.Data.network.TransactionDTO
import com.example.expensenote.Data.network.UploadRequest
import com.example.expensenote.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RemoteDataManager : IDataManager {

    private val api = RetrofitClient.apiService

    // ============================================
    // TRANSACCIONES
    // ============================================

    override fun getTransactions(query: TransactionQuery?): List<Transaction> {
        throw UnsupportedOperationException("Use suspend version: getTransactionsSuspend()")
    }

    suspend fun getTransactionsSuspend(query: TransactionQuery? = null): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTransactions(
                text = query?.text?.takeIf { it.isNotBlank() },
                type = query?.type?.name,
                categoryIds = query?.categoryIds?.takeIf { it.isNotEmpty() }?.joinToString(",")
            )

            val data = response.data
            if (response.success && data != null) {
                data.map { it.toTransaction() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override fun getTransaction(id: Long): Transaction? {
        throw UnsupportedOperationException("Use suspend version: getTransactionSuspend()")
    }

    suspend fun getTransactionSuspend(id: String): Transaction? = withContext(Dispatchers.IO) {
        try {
            val response = api.getTransaction(id)
            val data = response.data
            if (response.success && data != null) {
                data.toTransaction()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun insertTransaction(tx: Transaction): Transaction {
        throw UnsupportedOperationException("Use suspend version: insertTransactionSuspend()")
    }

    suspend fun insertTransactionSuspend(tx: Transaction): Transaction? = withContext(Dispatchers.IO) {
        try {
            val dto = tx.toDTO()
            val response = api.createTransaction(dto)
            val data = response.data
            if (response.success && data != null) {
                data.toTransaction()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun updateTransaction(tx: Transaction): Boolean {
        throw UnsupportedOperationException("Use suspend version: updateTransactionSuspend()")
    }

    suspend fun updateTransactionSuspend(tx: Transaction): Boolean = withContext(Dispatchers.IO) {
        try {
            val id = tx.stringId.ifEmpty { return@withContext false }
            val dto = tx.toDTO()
            val response = api.updateTransaction(id, dto)
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun deleteTransaction(id: Long): Boolean {
        throw UnsupportedOperationException("Use suspend version: deleteTransactionSuspend()")
    }

    suspend fun deleteTransactionSuspend(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteTransaction(id)
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getTotals(range: DateRange?): Pair<Double, Double> {
        throw UnsupportedOperationException("Use suspend version: getTotalsSuspend()")
    }

    suspend fun getTotalsSuspend(): Triple<Double, Double, Double> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTotals()
            val data = response.data
            if (response.success && data != null) {
                Triple(data.income, data.expense, data.balance)
            } else {
                Triple(0.0, 0.0, 0.0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Triple(0.0, 0.0, 0.0)
        }
    }

    // ============================================
    // CATEGORÍAS
    // ============================================

    override fun getCategories(): List<Category> {
        throw UnsupportedOperationException("Use suspend version: getCategoriesSuspend()")
    }

    suspend fun getCategoriesSuspend(): List<Category> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCategories()
            val data = response.data
            if (response.success && data != null) {
                data.map { it.toCategory() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override fun getCategory(id: Long): Category? {
        throw UnsupportedOperationException("Use suspend version: getCategorySuspend()")
    }

    suspend fun getCategorySuspend(id: String): Category? = withContext(Dispatchers.IO) {
        try {
            val response = api.getCategory(id)
            val data = response.data
            if (response.success && data != null) {
                data.toCategory()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun insertCategory(cat: Category): Category {
        throw UnsupportedOperationException("Use suspend version: insertCategorySuspend()")
    }

    suspend fun insertCategorySuspend(cat: Category): Category? = withContext(Dispatchers.IO) {
        try {
            val dto = cat.toDTO()
            val response = api.createCategory(dto)
            val data = response.data
            if (response.success && data != null) {
                data.toCategory()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun updateCategory(cat: Category): Boolean {
        throw UnsupportedOperationException("Use suspend version: updateCategorySuspend()")
    }

    suspend fun updateCategorySuspend(cat: Category): Boolean = withContext(Dispatchers.IO) {
        try {
            val id = cat.id.toString()
            val dto = cat.toDTO()
            val response = api.updateCategory(id, dto)
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun deleteCategory(id: Long): Boolean {
        throw UnsupportedOperationException("Use suspend version: deleteCategorySuspend()")
    }

    suspend fun deleteCategorySuspend(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteCategory(id)
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ============================================
    // UPLOAD DE IMÁGENES
    // ============================================

    suspend fun uploadImageSuspend(base64: String, fileName: String? = null): String? = withContext(Dispatchers.IO) {
        try {
            val request = UploadRequest(base64, fileName)
            val response = api.uploadImage(request)
            val data = response.data
            if (response.success && data != null) {
                data.url
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ============================================
    // CONVERSIONES DTO ↔ ENTITY
    // ============================================

    private fun TransactionDTO.toTransaction(): Transaction {
        return Transaction(
            id = id.hashCode().toLong(),
            stringId = id,
            amount = amount,
            category = category,
            type = TransactionType.valueOf(type),
            date = date,
            note = note,
            photoUri = photoUrl,
            photoBitmap = null
        )
    }

    private fun Transaction.toDTO(): TransactionDTO {
        return TransactionDTO(
            id = stringId,
            amount = amount,
            category = category,
            type = type.name,
            date = date,
            note = note,
            photoUrl = photoUri
        )
    }

    private fun CategoryDTO.toCategory(): Category {
        return Category(
            id = id.hashCode().toLong(),
            name = name,
            color = color,
            icon = icon
        )
    }

    private fun Category.toDTO(): CategoryDTO {
        return CategoryDTO(
            id = if (id == 0L) "" else id.toString(),
            name = name,
            color = color,
            icon = icon
        )
    }
}