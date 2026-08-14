package com.user_425.ewallet_relay.data.network

import com.user_425.ewallet_relay.data.model.ApiResponse
import com.user_425.ewallet_relay.data.model.NotificationPayload
import com.user_425.ewallet_relay.data.model.TestPayload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface NotificationApiService {
    
    @POST
    suspend fun sendNotification(
        @Url url: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("X-API-Key") apiKey: String? = null,
        @Body payload: NotificationPayload
    ): Response<ApiResponse>
    
    @POST
    suspend fun sendTestNotification(
        @Url url: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("X-API-Key") apiKey: String? = null,
        @Body payload: TestPayload
    ): Response<ApiResponse>
    
    @POST
    suspend fun sendRawJson(
        @Url url: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("X-API-Key") apiKey: String? = null,
        @Body payload: String
    ): Response<Any>
}