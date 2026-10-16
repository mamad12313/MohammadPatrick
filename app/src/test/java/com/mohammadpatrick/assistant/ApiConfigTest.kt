package com.mohammadpatrick.assistant

import com.mohammadpatrick.assistant.domain.model.ApiConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiConfigTest {

    @Test
    fun testDefaultApiConfigIsUnconfigured() {
        val config = ApiConfig()
        assertFalse("Default API should not be configured", config.isConfigured)
        assertEquals("", config.baseUrl)
        assertEquals("", config.apiKey)
    }

    @Test
    fun testValidHttpUrlConfigured() {
        val config = ApiConfig(baseUrl = "https://api.myassistant.ir/v1")
        assertTrue("Valid HTTP/HTTPS url should be configured", config.isConfigured)
    }

    @Test
    fun testInvalidUrlNotConfigured() {
        val config = ApiConfig(baseUrl = "ftp://invalid-url")
        assertFalse(config.isConfigured)

        val emptyConfig = ApiConfig(baseUrl = "   ")
        assertFalse(emptyConfig.isConfigured)
    }
}
