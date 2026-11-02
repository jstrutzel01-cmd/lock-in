package com.example.lock_in.food

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class UsdaNutrient(
    val nutrientName: String?,
    val value: Double?
)

// Parses list and extracts nutritional info from array
data class UsdaFood(
    val description: String,
    val brandOwner: String?,
    val foodCategory: String?,
    val foodNutrients: List<UsdaNutrient>
) {
    // Helper properties to instantly extract macros from the nested array
    val calories: Double
        get() = foodNutrients.find { it.nutrientName == "Energy" }?.value ?: 0.0

    val protein: Double
        get() = foodNutrients.find { it.nutrientName == "Protein" }?.value ?: 0.0

    val carbs: Double
        get() = foodNutrients.find { it.nutrientName?.contains("Carbohydrate", ignoreCase = true) == true }?.value ?: 0.0

    val fat: Double
        get() = foodNutrients.find { it.nutrientName?.contains("lipid", ignoreCase = true) == true }?.value ?: 0.0
}

// Returns USDA nested array
data class UsdaSearchResponse(
    val foods: List<UsdaFood>
)

// Retrofit Interface
interface UsdaApiService {
    @GET("foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("pageSize") pageSize: Int = 20,
        @Query("dataType") dataType: String = "Foundation,SR Legacy,Branded",
        @Query("api_key") apiKey: String = "kKdcCwfFPRpWN8fLzn9wPBB6m78bhBhN7F6mKTC7"
    ): UsdaSearchResponse
}

// API Builder
object RetrofitInstance {
    private const val BASE_URL = "https://api.nal.usda.gov/fdc/v1/"

    val api: UsdaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsdaApiService::class.java)
    }
}
