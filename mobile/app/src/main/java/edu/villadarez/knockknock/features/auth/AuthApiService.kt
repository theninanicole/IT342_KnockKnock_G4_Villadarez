package edu.villadarez.knockknock.features.auth

import edu.villadarez.knockknock.features.auth.AuthResponse
import edu.villadarez.knockknock.features.auth.LoginRequest
import edu.villadarez.knockknock.features.auth.RegisterCondoAdminRequest
import edu.villadarez.knockknock.features.auth.RegisterVisitorRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register/visitor")
    suspend fun registerVisitor(@Body request: RegisterVisitorRequest): Response<AuthResponse>

    @POST("auth/register/condo-admin")
    suspend fun registerCondoAdmin(@Body request: RegisterCondoAdminRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<AuthResponse>

    @PUT("users/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<okhttp3.ResponseBody>

    @PUT("users/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<okhttp3.ResponseBody>
}
