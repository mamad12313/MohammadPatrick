package com.mohammadpatrick.assistant.data.remote

import com.mohammadpatrick.assistant.data.remote.dto.AssistantRequest
import com.mohammadpatrick.assistant.data.remote.dto.AssistantResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * Retrofit Service Interface for the future backend API.
 * Supports standard JSON responses, health checks, and Server-Sent Events (SSE) streaming.
 */
interface AssistantApiService {

    @POST("chat/completions")
    suspend fun getCompletion(
        @Body request: AssistantRequest,
        @Header("Authorization") authorization: String? = null
    ): Response<AssistantResponse>

    @Streaming
    @POST("chat/stream")
    suspend fun getStreamCompletion(
        @Body request: AssistantRequest,
        @Header("Authorization") authorization: String? = null
    ): Response<ResponseBody>

    @GET("health")
    suspend fun checkHealth(): Response<Map<String, Any>>
}
