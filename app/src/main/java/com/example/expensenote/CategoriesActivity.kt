package com.example.expensenote

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensenote.Controller.CategoryController
import com.example.expensenote.Data.RemoteDataManager
import com.example.expensenote.databinding.ActivityCategoriesBinding
import com.example.expensenote.entity.Category
import com.example.expensenote.ui.adapter.CategoryAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class CategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var adapter: CategoryAdapter
    private val controller = CategoryController(RemoteDataManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFAB()
        loadCategories()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Categorías"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onEditClick = { category ->
                editCategory(category)
            },
            onDeleteClick = { category ->
                confirmDelete(category)
            }
        )

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@CategoriesActivity)
            adapter = this@CategoriesActivity.adapter
        }
    }

    private fun setupFAB() {
        binding.fabAddCategory.setOnClickListener {
            val intent = Intent(this, CategoryFormActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val categories = controller.list()
                adapter.submitList(categories)
            } catch (e: Exception) {
                Toast.makeText(
                    this@CategoriesActivity,
                    "Error cargando categorías: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun editCategory(category: Category) {
        val intent = Intent(this, CategoryFormActivity::class.java).apply {
            putExtra("CATEGORY_STRING_ID", category.stringId)
            putExtra("CATEGORY_NAME", category.name)
            putExtra("CATEGORY_ICON", category.icon)
        }
        startActivity(intent)
    }

    private fun confirmDelete(category: Category) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar categoría")
            .setMessage("¿Estás seguro de eliminar '${category.name}'? Esta acción no se puede deshacer.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                deleteCategory(category)
            }
            .show()
    }

    private fun deleteCategory(category: Category) {
        lifecycleScope.launch {
            try {
                val success = controller.delete(category.stringId)
                if (success) {
                    Toast.makeText(
                        this@CategoriesActivity,
                        "Categoría eliminada",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadCategories()
                } else {
                    Toast.makeText(
                        this@CategoriesActivity,
                        "Error al eliminar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CategoriesActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }
}