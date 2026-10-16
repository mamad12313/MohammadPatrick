package com.mohammadpatrick.assistant.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AssistantRequest(
    @Json(name = "conversation_id") val conversationId: String,
    @Json(name = "prompt") val prompt: String,
    @Json(name = "image_base64") val imageBase64: String? = null,
    @Json(name = "file_context") val fileContext: String? = null,
    @Json(name = "device_info") val deviceInfo: DeviceInfoDto? = null
)

@JsonClass(generateAdapter = true)
data class DeviceInfoDto(
    @Json(name = "device") val device: String = "Samsung Galaxy A23",
    @Json(name = "os_version") val osVersion: String = "Android 14 (API 34)",
    @Json(name = "locale") val locale: String = "fa-IR"
)

@JsonClass(generateAdapter = true)
data class AssistantResponse(
    @Json(name = "reply") val reply: String,
    @Json(name = "finish_reason") val finishReason: String? = null,
    @Json(name = "usage_tokens") val usageTokens: Int? = null,
    @Json(name = "action_intent") val actionIntent: String? = null
)
