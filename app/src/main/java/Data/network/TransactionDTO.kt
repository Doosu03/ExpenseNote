package com.example.expensenote.Data.network

import com.google.gson.annotations.SerializedName

data class TransactionDTO(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("category")
    val category: String,

    @SerializedName("type")
    val type: String,  // "INCOME" o "EXPENSE"

    @SerializedName("date")
    val date: String,

    @SerializedName("note")
    val note: String = "",

    @SerializedName("photoUrl")
    val photoUrl: String? = null
)