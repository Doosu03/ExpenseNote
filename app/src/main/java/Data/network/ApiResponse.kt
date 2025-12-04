package com.example.expensenote.Data.network

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String
)