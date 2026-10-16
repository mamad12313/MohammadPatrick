package com.mohammadpatrick.assistant.domain.model

data class ApiConfig(
    val baseUrl: String = "",
    val apiKey: String = "",
    val timeoutSeconds: Long = 30L,
    val isEnabled: Boolean = false,
    val isStreamingEnabled: Boolean = true
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && baseUrl.startsWith("http")
}
