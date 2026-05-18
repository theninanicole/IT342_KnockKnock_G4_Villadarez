package edu.villadarez.knockknock.features.visit

import okhttp3.ResponseBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface VisitApiService {

    @GET("visits/my-visits")
    suspend fun getMyVisits(
        @Header("Authorization") token: String
    ): Response<MyVisitsResponse>

    @GET("visits/{visitId}/files")
    suspend fun getVisitFiles(
        @Path("visitId") visitId: String,
        @Header("Authorization") token: String
    ): Response<List<VisitFileDTO>>

    @FormUrlEncoded
    @POST("visits")
    suspend fun createVisitWithoutFile(
        @Header("Authorization") token: String,
        @Field("condoId") condoId: String,
        @Field("unitNumber") unitNumber: String,
        @Field("dateOfVisit") dateOfVisit: String,
        @Field("purposeOfVisit") purposeOfVisit: String
    ): Response<CreateVisitResponse>

    @Multipart
    @POST("visits")
    suspend fun createVisitWithFile(
        @Header("Authorization") token: String,
        @Part("condoId") condoId: RequestBody,
        @Part("unitNumber") unitNumber: RequestBody,
        @Part("dateOfVisit") dateOfVisit: RequestBody,
        @Part("purposeOfVisit") purposeOfVisit: RequestBody,
        @Part idFile: MultipartBody.Part
    ): Response<CreateVisitResponse>

    @Multipart
    @POST("visits/{visitId}/files")
    suspend fun uploadVisitFile(
        @Header("Authorization") token: String,
        @Path("visitId") visitId: String,
        @Part idFile: MultipartBody.Part
    ): Response<Map<String, Any>>

    @POST("visits/{visitId}/files")
    suspend fun saveVisitFileMetadata(
        @Header("Authorization") token: String,
        @Path("visitId") visitId: String,
        @Body request: FileUploadRequest
    ): Response<VisitFileDTO>

    @POST("visits/{visitId}/qr")
    suspend fun generateVisitQr(
        @Header("Authorization") token: String,
        @Path("visitId") visitId: String
    ): Response<GenerateQrResponse>

    @POST("visits/{visitId}/qr/email")
    suspend fun sendVisitQrEmail(
        @Header("Authorization") token: String,
        @Path("visitId") visitId: String
    ): Response<ResponseBody>

    @PUT("visits/{visitId}")
    suspend fun updateVisit(
        @Header("Authorization") token: String,
        @Path("visitId") visitId: String,
        @Body request: UpdateVisitRequest
    ): Response<ResponseBody>

    @POST("visits/{visitId}/cancel")
    suspend fun cancelVisit(
        @Header("Authorization") token: String,
        @Path("visitId") visitId: String
    ): Response<ResponseBody>

    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20
    ): Response<NotificationsResponse>

    @PUT("notifications/{notificationId}/read")
    suspend fun markNotificationRead(
        @Header("Authorization") token: String,
        @Path("notificationId") notificationId: String
    ): Response<ResponseBody>

    @GET("condos")
    suspend fun getCondominiums(
        @Header("Authorization") token: String
    ): Response<List<Condominium>>

    @GET("admin/visits")
    suspend fun getAdminVisits(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null
    ): Response<AdminVisitsResponse>

    @GET("admin/visits-history")
    suspend fun getAdminHistory(
        @Header("Authorization") token: String
    ): Response<AdminHistoryResponse>

    @GET("visits/{visitId}")
    suspend fun getVisitById(
        @Header("Authorization") token: String,
        @Path("visitId") visitId: String
    ): Response<AdminVisit>

    @GET("visits/reference/{referenceNumber}")
    suspend fun getVisitByReference(
        @Header("Authorization") token: String,
        @Path("referenceNumber") referenceNumber: String
    ): Response<AdminVisit>

    @POST("visits/{visitId}/check-in")
    suspend fun checkInVisit(
        @Header("Authorization") token: String,
        @Path("visitId") visitId: String
    ): Response<AdminVisit>

    @POST("visits/{visitId}/check-out")
    suspend fun checkOutVisit(
        @Header("Authorization") token: String,
        @Path("visitId") visitId: String
    ): Response<AdminVisit>
}
