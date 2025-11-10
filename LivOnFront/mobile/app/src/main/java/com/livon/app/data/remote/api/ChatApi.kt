package com.livon.app.data.remote.api

import android.util.Log
import com.livon.app.core.network.RetrofitProvider
import com.livon.app.data.remote.dto.ChatMessageResponseDto
import retrofit2.HttpException
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {

    suspend fun getChatMessages(
        reservationId: Int,
        lastSentAt: String? = null,
        accessToken: String? = null
    ): ChatMessageResponseDto
}


class ChatApiImpl(
    private val service: ChatRetrofitService = RetrofitProvider.createService(ChatRetrofitService::class.java)
) : ChatApi {

    override suspend fun getChatMessages(
        reservationId: Int,
        lastSentAt: String?,
        accessToken: String?
    ): ChatMessageResponseDto {
        val authHeader = accessToken?.let { token ->
            if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
        }
        return try {
            Log.d("ChatApi", "요청 시작: reservationId=$reservationId")
            Log.d("ChatApi", "lastSentAt: $lastSentAt")
            Log.d("ChatApi", "accessToken 존재: ${accessToken != null}")
            service.getChatMessages(
                roomId = reservationId,
                lastSentAt = lastSentAt,
                authorization = authHeader
            )
        } catch (e: HttpException) {
            Log.e("ChatApi", "요청 실패: HTTP ${e.code()} ${e.message()}", e)
            throw e
        } catch (e: Exception) {
            Log.e("ChatApi", "요청 실패: ${e.message}", e)
            throw e
        }
    }

}


interface ChatRetrofitService {
    @GET("goods/chat/{roomId}/message")
    suspend fun getChatMessages(
        @Path("roomId") roomId: Int,
        @Query("lastSentAt") lastSentAt: String? = null,
        @Header("Authorization") authorization: String? = null
    ): ChatMessageResponseDto
}
