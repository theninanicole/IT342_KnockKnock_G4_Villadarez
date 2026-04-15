package edu.cit.villadarez.knockknock.api

import edu.cit.villadarez.knockknock.models.AuthResponse
import edu.cit.villadarez.knockknock.models.LoginRequest
import edu.cit.villadarez.knockknock.models.RegisterCondoAdminRequest
import edu.cit.villadarez.knockknock.models.RegisterVisitorRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register/visitor")
    suspend fun registerVisitor(@Body request: RegisterVisitorRequest): Response<AuthResponse>

    @POST("auth/register/condo-admin")
    suspend fun registerCondoAdmin(@Body request: RegisterCondoAdminRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<AuthResponse>
}
