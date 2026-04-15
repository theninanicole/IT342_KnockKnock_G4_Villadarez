package edu.cit.villadarez.knockknock.api

import edu.cit.villadarez.knockknock.models.MyVisitsResponse
import edu.cit.villadarez.knockknock.models.NotificationItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface VisitApiService {

    @GET("visits/my-visits")
    suspend fun getMyVisits(
        @Header("Authorization") token: String
    ): Response<MyVisitsResponse>

    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20
    ): Response<List<NotificationItem>>
}
