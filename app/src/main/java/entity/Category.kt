package com.example.expensenote.entity

data class Category(
    val id: Long = 0L,
    val name: String,
    val color: Int? = null,
    val icon: String? = null
)
