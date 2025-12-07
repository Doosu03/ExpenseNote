package com.example.expensenote.Controller

import com.example.expensenote.Data.RemoteDataManager
import com.example.expensenote.entity.Category

class CategoryController(private val data: RemoteDataManager) {

    suspend fun list(): List<Category> = data.getCategoriesSuspend()

    suspend fun get(stringId: String): Category? = data.getCategorySuspend(stringId)
    suspend fun create(input: Category): Category? {
        require(input.name.isNotBlank()) { "Category name is required." }
        return data.insertCategorySuspend(input)
    }
    suspend fun update(input: Category): Boolean {
        require(input.name.isNotBlank()) { "Category name is required." }
        return data.updateCategorySuspend(input)
    }
    suspend fun delete(stringId: String): Boolean = data.deleteCategorySuspend(stringId)
}