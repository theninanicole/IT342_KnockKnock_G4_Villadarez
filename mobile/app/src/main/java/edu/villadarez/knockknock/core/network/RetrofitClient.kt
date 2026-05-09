package edu.villadarez.knockknock.core.network

import edu.villadarez.knockknock.features.auth.AuthApiService
import edu.villadarez.knockknock.features.visit.VisitApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Existing auth service (kept for backward compatibility)
    val instance: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    // New services for core modules
    val visitService: VisitApiService by lazy {
        retrofit.create(VisitApiService::class.java)
    }
}
