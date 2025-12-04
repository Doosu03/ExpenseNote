package com.example.expensenote.Data.network

import retrofit2.http.*

interface ApiService {

    // ============================================
    // TRANSACCIONES
    // ============================================

    @GET("transactions")
    suspend fun getTransactions(
        @Query("text") text: String? = null,
        @Query("type") type: String? = null,
        @Query("categoryIds") categoryIds: String? = null
    ): ApiResponse<List<TransactionDTO>>

    @GET("transactions/{id}")
    suspend fun getTransaction(@Path("id") id: String): ApiResponse<TransactionDTO>

    @POST("transactions")
    suspend fun createTransaction(@Body transaction: TransactionDTO): ApiResponse<TransactionDTO>

    @PUT("transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body transaction: TransactionDTO
    ): ApiResponse<TransactionDTO>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): ApiResponse<Unit?>

    @GET("totals")
    suspend fun getTotals(): ApiResponse<TotalsDTO>

    // ============================================
    // CATEGORÍAS
    // ============================================

    @GET("categories")
    suspend fun getCategories(): ApiResponse<List<CategoryDTO>>

    @GET("categories/{id}")
    suspend fun getCategory(@Path("id") id: String): ApiResponse<CategoryDTO>

    @POST("categories")
    suspend fun createCategory(@Body category: CategoryDTO): ApiResponse<CategoryDTO>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body category: CategoryDTO
    ): ApiResponse<CategoryDTO>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): ApiResponse<Unit?>

    // ============================================
    // UPLOAD
    // ============================================

    @POST("upload")
    suspend fun uploadImage(@Body request: UploadRequest): ApiResponse<UploadResponse>

    @DELETE("upload/{fileName}")
    suspend fun deleteImage(@Path("fileName") fileName: String): ApiResponse<Unit?>
}

// DTOs para upload
data class UploadRequest(
    val imageBase64: String,
    val fileName: String? = null
)

data class UploadResponse(
    val url: String,
    val fileName: String
)

// DTO para totals
data class TotalsDTO(
    val income: Double,
    val expense: Double,
    val balance: Double
)