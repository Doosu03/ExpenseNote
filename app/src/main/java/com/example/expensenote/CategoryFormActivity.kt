package com.example.expensenote

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.expensenote.Controller.CategoryController
import com.example.expensenote.Data.RemoteDataManager
import com.example.expensenote.databinding.ActivityCategoryFormBinding
import com.example.expensenote.entity.Category
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class CategoryFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryFormBinding
    private val controller = CategoryController(RemoteDataManager)

    private var categoryStringId: String? = null
    private var selectedIcon: String = "📌"
    private val iconButtons = mutableListOf<MaterialButton>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryStringId = intent.getStringExtra("CATEGORY_STRING_ID")

        setupToolbar()
        setupIconButtons()
        setupButtons()

        if (categoryStringId != null) {
            loadCategory()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (categoryStringId == null) "Nueva categoría" else "Editar categoría"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupIconButtons() {
        iconButtons.addAll(
            listOf(
                binding.btnIcon1, binding.btnIcon2, binding.btnIcon3,
                binding.btnIcon4, binding.btnIcon5, binding.btnIcon6,
                binding.btnIcon7, binding.btnIcon8, binding.btnIcon9,
                binding.btnIcon10
            )
        )

        iconButtons.forEach { button ->
            button.setOnClickListener {
                selectIcon(button)
            }
        }
        selectIcon(binding.btnIcon1)
    }

    private fun selectIcon(button: MaterialButton) {
        iconButtons.forEach {
            it.strokeWidth = 2
            it.strokeColor = getColorStateList(R.color.stroke)
        }

        button.strokeWidth = 4
        button.strokeColor = getColorStateList(R.color.primary)
        selectedIcon = button.text.toString()
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            saveCategory()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun loadCategory() {
        val name = intent.getStringExtra("CATEGORY_NAME")
        val icon = intent.getStringExtra("CATEGORY_ICON")

        binding.etCategoryName.setText(name)

        icon?.let { iconToSelect ->
            val button = iconButtons.find { it.text.toString() == iconToSelect }
            button?.let { selectIcon(it) }
        }
    }

    private fun saveCategory() {
        val name = binding.etCategoryName.text.toString().trim()

        if (name.isEmpty()) {
            binding.etCategoryName.error = "Ingresa un nombre"
            return
        }

        lifecycleScope.launch {
            try {
                val category = Category(
                    id = 0,
                    stringId = categoryStringId ?: "",
                    name = name,
                    icon = selectedIcon
                )

                if (categoryStringId == null) {
                    // Crear nueva
                    val created = controller.create(category)
                    if (created != null) {
                        Toast.makeText(
                            this@CategoryFormActivity,
                            "Categoría creada",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    // Actualizar existente
                    val updated = controller.update(category)
                    if (updated) {
                        Toast.makeText(
                            this@CategoryFormActivity,
                            "Categoría actualizada",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@CategoryFormActivity,
                            "Error al actualizar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CategoryFormActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}