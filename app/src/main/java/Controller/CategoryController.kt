package com.example.expensenote.Controller

import com.example.expensenote.Data.IDataManager
import com.example.expensenote.entity.Category

class CategoryController(private val data: IDataManager) {

    fun list(): List<Category> = data.getCategories()

    fun get(id: Long): Category? = data.getCategory(id)

    fun create(input: Category): Category {
        require(input.name.isNotBlank()) { "El nombre de la categoría es requerido." }
        return data.insertCategory(input)
    }

    fun update(input: Category): Boolean {
        require(input.id != 0L) { "El id es requerido para actualizar." }
        require(input.name.isNotBlank()) { "El nombre de la categoría es requerido." }
        return data.updateCategory(input)
    }

    fun delete(id: Long): Boolean = data.deleteCategory(id)
}
