package com.livon.app.data.remote.api

import android.util.Log
import com.livon.app.core.network.RetrofitProvider
import com.livon.app.data.remote.dto.ChatMessageResponseDto
import com.livon.app.data.remote.dto.ChatRoomInfoResponseDto
import retrofit2.HttpException
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {

    suspend fun getChatMessages(
        reservationId: Int,
        lastSentAt: String? = null,
        accessToken: String? = null
    ): ChatMessageResponseDto
    
    suspend fun getChatRoomInfo(
        consultationId: Long,
        accessToken: String? = null
    ): ChatRoomInfoResponseDto
}


class ChatApiImpl(
    private val service: ChatRetrofitService = RetrofitProvider.createService(ChatRetrofitService::class.java)
) : ChatApi {

    override suspend fun getChatMessages(
        reservationId: Int,
        lastSentAt: String?,
        accessToken: String?
    ): ChatMessageResponseDto {
        return try {
            service.getChatMessages(
                roomId = reservationId,
                lastSentAt = lastSentAt,
                authorization = null  // authInterceptor에서 이미 처리됨
            )
        } catch (e: HttpException) {
            Log.e("ChatApi", "채팅 메시지 조회 실패: HTTP ${e.code()} ${e.message()}", e)
            throw e
        } catch (e: Exception) {
            Log.e("ChatApi", "채팅 메시지 조회 실패: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun getChatRoomInfo(
        consultationId: Long,
        accessToken: String?
    ): ChatRoomInfoResponseDto {
        return try {
            service.getChatRoomInfo(
                consultationId = consultationId,
                authorization = null  // authInterceptor에서 이미 처리됨
            )
        } catch (e: HttpException) {
            Log.e("ChatApi", "채팅방 정보 조회 실패: HTTP ${e.code()} ${e.message()}", e)
            throw e
        } catch (e: Exception) {
            Log.e("ChatApi", "채팅방 정보 조회 실패: ${e.message}", e)
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
    
    @POST("goods/chat")
    suspend fun getChatRoomInfo(
        @Query("consultationId") consultationId: Long,
        @Header("Authorization") authorization: String? = null
    ): ChatRoomInfoResponseDto
}
