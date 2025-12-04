package com.example.expensenote.Data.network

import com.google.gson.annotations.SerializedName

data class CategoryDTO(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String,

    @SerializedName("color")
    val color: Int? = null,

    @SerializedName("icon")
    val icon: String? = null
)