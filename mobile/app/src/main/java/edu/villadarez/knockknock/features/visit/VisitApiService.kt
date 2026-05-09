package edu.villadarez.knockknock.features.visit

import edu.villadarez.knockknock.features.visit.MyVisitsResponse
import edu.villadarez.knockknock.features.visit.NotificationItem
import edu.villadarez.knockknock.features.visit.Condominium
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

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
    ): Response<okhttp3.ResponseBody>

    @PUT("visits/{visitId}")
    suspend fun updateVisit(
        @Header("Authorization") token: String,
        @Path("visitId") visitId: String,
        @Body request: UpdateVisitRequest
    ): Response<okhttp3.ResponseBody>

    @POST("visits/{visitId}/cancel")
    suspend fun cancelVisit(
        @Header("Authorization") token: String,
        @Path("visitId") visitId: String
    ): Response<okhttp3.ResponseBody>

    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20
    ): Response<NotificationsResponse>

    @PUT("notifications/{notificationId}/read")
    suspend fun markNotificationRead(
        @Header("Authorization") token: String,
        @Path("notificationId") notificationId: String
    ): Response<okhttp3.ResponseBody>

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
