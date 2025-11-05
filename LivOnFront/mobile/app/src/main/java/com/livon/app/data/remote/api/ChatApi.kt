package com.livon.app.data.remote.api

import android.util.Log
import com.livon.app.data.remote.dto.ChatMessageResponseDto
import com.livon.app.feature.shared.streaming.Urls
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface ChatApi {

    suspend fun getChatMessages(
        reservationId: Int,
        lastSentAt: String? = null,
        accessToken: String? = null
    ): ChatMessageResponseDto
}


class ChatApiImpl(
    private val baseUrl: String = Urls.applicationServerUrl.trimEnd('/')
) : ChatApi {
    
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun getChatMessages(
        reservationId: Int,
        lastSentAt: String?,
        accessToken: String?
    ): ChatMessageResponseDto {
        val endpoint = "$baseUrl/goods/chat/$reservationId/message"
        
        return try {
            Log.d("ChatApi", "요청 시작: $endpoint")
            Log.d("ChatApi", "lastSentAt: $lastSentAt")
            Log.d("ChatApi", "accessToken 존재: ${accessToken != null}")
            
            val response = client.get(endpoint) {
                url {
                    lastSentAt?.let {
                        parameters.append("lastSentAt", it)
                    }
                }
                accessToken?.let { token ->
                    val authHeader = if (token.startsWith("Bearer ", ignoreCase = true)) {
                        token
                    } else {
                        "Bearer $token"
                    }
                    header(HttpHeaders.Authorization, authHeader)
                }
            }.body<ChatMessageResponseDto>()
            
            Log.d("ChatApi", "응답 성공: isSuccess=${response.isSuccess}, message=${response.message}")
            response
        } catch (e: Exception) {
            Log.e("ChatApi", "요청 실패: ${e.message}", e)
            throw e
        }
    }
}

