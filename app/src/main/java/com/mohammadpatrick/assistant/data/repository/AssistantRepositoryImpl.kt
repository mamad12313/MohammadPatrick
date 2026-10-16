package com.mohammadpatrick.assistant.data.repository

import com.mohammadpatrick.assistant.data.local.pref.PreferencesManager
import com.mohammadpatrick.assistant.data.remote.ApiClientProvider
import com.mohammadpatrick.assistant.data.remote.dto.AssistantRequest
import com.mohammadpatrick.assistant.data.remote.dto.DeviceInfoDto
import com.mohammadpatrick.assistant.domain.repository.AssistantRepository
import com.mohammadpatrick.assistant.domain.repository.AssistantResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class AssistantRepositoryImpl(
    private val apiClientProvider: ApiClientProvider,
    private val preferencesManager: PreferencesManager
) : AssistantRepository {

    override suspend fun sendMessage(
        conversationId: String,
        userPrompt: String,
        attachedImageBase64: String?,
        attachedFileText: String?
    ): AssistantResult = withContext(Dispatchers.IO) {
        val settings = preferencesManager.appSettingsFlow.first()
        val config = settings.apiConfig

        if (!config.isConfigured || !config.isEnabled) {
            return@withContext AssistantResult.ApiNotConfigured(
                "برای دریافت پاسخ هوشمند، ابتدا API خود را متصل کنید."
            )
        }

        val service = apiClientProvider.getApiService()
            ?: return@withContext AssistantResult.Error("تنظیمات آدرس API نامعتبر است.")

        try {
            val authHeader = if (config.apiKey.isNotBlank()) "Bearer ${config.apiKey}" else null
            val request = AssistantRequest(
                conversationId = conversationId,
                prompt = userPrompt,
                imageBase64 = attachedImageBase64,
                fileContext = attachedFileText,
                deviceInfo = DeviceInfoDto()
            )

            val response = service.getCompletion(request, authHeader)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.reply.isNotBlank()) {
                    AssistantResult.Success(body.reply)
                } else {
                    AssistantResult.Error("سرور پاسخ خالی ارسال کرد.")
                }
            } else {
                AssistantResult.Error("خطای سرور (${response.code()}): ${response.message()}")
            }
        } catch (e: java.net.SocketTimeoutException) {
            AssistantResult.Error("مهلت زمانی ارتباط با سرور به پایان رسید (Timeout).")
        } catch (e: java.net.UnknownHostException) {
            AssistantResult.Error("آدرس سرور یافت نشد. لطفاً اتصال اینترنت خود را بررسی کنید.")
        } catch (e: Exception) {
            AssistantResult.Error("خطا در برقراری ارتباط: ${e.localizedMessage ?: "خطای ناشناخته"}", e)
        }
    }

    override fun streamMessage(
        conversationId: String,
        userPrompt: String
    ): Flow<AssistantResult> = flow {
        emit(AssistantResult.Loading)

        val settings = preferencesManager.appSettingsFlow.first()
        val config = settings.apiConfig

        if (!config.isConfigured || !config.isEnabled) {
            emit(AssistantResult.ApiNotConfigured("برای دریافت پاسخ هوشمند، ابتدا API خود را متصل کنید."))
            return@flow
        }

        val service = apiClientProvider.getApiService()
        if (service == null) {
            emit(AssistantResult.Error("آدرس API نامعتبر است."))
            return@flow
        }

        try {
            val authHeader = if (config.apiKey.isNotBlank()) "Bearer ${config.apiKey}" else null
            val request = AssistantRequest(
                conversationId = conversationId,
                prompt = userPrompt,
                deviceInfo = DeviceInfoDto()
            )

            val response = service.getStreamCompletion(request, authHeader)
            if (response.isSuccessful && response.body() != null) {
                val inputStream = response.body()!!.byteStream()
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.startsWith("data: ")) {
                        val chunk = currentLine.removePrefix("data: ")
                        if (chunk == "[DONE]") break
                        emit(AssistantResult.StreamChunk(chunk))
                    }
                }
            } else {
                emit(AssistantResult.Error("خطای دریافت جریان از سرور: کد ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(AssistantResult.Error("خطای جریان اطلاعات: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun testApiConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        val service = apiClientProvider.getApiService()
            ?: return@withContext Result.failure(Exception("آدرس API تنظیم نشده است."))

        try {
            val response = service.checkHealth()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("کد پاسخ سرور: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
