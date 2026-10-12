package com.example.lock_in.food

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data classes that parse JSON
data class OffNutriments(
    @SerializedName("energy-kcal_100g") val calories: Double?,
    @SerializedName("proteins_100g") val protein: Double?,
    @SerializedName("carbohydrates_100g") val carbs: Double?,
    @SerializedName("fat_100g") val fat: Double?
)

data class OffProduct(
    @SerializedName("product_name") val productName: String?,
    val nutriments: OffNutriments?
)

data class OffSearchResponse(
    val products: List<OffProduct>
)

// Retrofit Interface
interface OpenFoodFactsApiService {
    @GET("search")
    suspend fun searchFoods(
        @Query("search_terms") query: String,
        @Query("search_simple") simple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("fields") fields: String = "product_name,nutriments"
    ): OffSearchResponse
}

// Singleton of OpenFoodFactsAPI
object RetrofitInstance {
    private const val BASE_URL = "https://world.openfoodfacts.org/api/v2/"

    val api: OpenFoodFactsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApiService::class.java)
    }
}