package com.example.expensenote.Controller

import com.example.expensenote.Data.RemoteDataManager
import com.example.expensenote.entity.Category

class CategoryController(private val data: RemoteDataManager) {

    suspend fun list(): List<Category> = data.getCategoriesSuspend()

    suspend fun get(id: String): Category? = data.getCategorySuspend(id)

    suspend fun create(input: Category): Category? {
        require(input.name.isNotBlank()) { "Category name is required." }
        return data.insertCategorySuspend(input)
    }

    suspend fun update(input: Category): Boolean {
        require(input.id != 0L) { "Id is required to update." }
        require(input.name.isNotBlank()) { "Category name is required." }
        return data.updateCategorySuspend(input)
    }

    suspend fun delete(id: String): Boolean = data.deleteCategorySuspend(id)
}