package com.livon.app.data.remote.socket

import io.reactivex.android.schedulers.AndroidSchedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import android.util.Log
import com.livon.app.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asSharedFlow
import ua.naiksoftware.stomp.dto.StompCommand
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage
import java.util.UUID
import kotlinx.coroutines.launch


object ChatStompManager {

    private val _incomingMessages = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val incomingMessages = _incomingMessages.asSharedFlow()
    private val _subscriptionReady = kotlinx.coroutines.flow.MutableSharedFlow<Boolean>()
    val subscriptionReady = _subscriptionReady.asSharedFlow()
    private lateinit var stompClient: StompClient

    fun connect(token: String, roomId: Long, scope: CoroutineScope) {
        stompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            BuildConfig.WEBSOCKET_URL
        )

        val headers = listOf(
            StompHeader("Authorization", "Bearer $token")
        )

        stompClient.connect(headers)

        stompClient.lifecycle()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { event ->
                when (event.type) {
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                        Log.d("STOMP", "연결 성공")
                        // 구독은 POST 요청 후에 수행하도록 변경
                        scope.launch {
                            _subscriptionReady.emit(true) // 연결 완료 신호
                        }
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR ->
                        Log.e("STOMP", "연결 오류", event.exception)
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED ->
                        Log.d("STOMP", "연결 종료")
                    else ->
                        Log.w("STOMP", "기타 이벤트: ${event.type}")
                }
            }
    }
    
    fun subscribe(roomId: Long, scope: CoroutineScope) {
        val topic = "/sub/chat/goods/$roomId"
        Log.d("STOMP", "구독 요청: $topic")

        stompClient.topic(topic)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ message ->
                Log.d("STOMP", "메시지 수신: ${message.payload}")
                scope.launch {
                    _incomingMessages.emit(message.payload)
                }
            }, { error ->
                Log.e("STOMP", "구독 중 오류", error)
                scope.launch {
                    _subscriptionReady.emit(false)
                }
            })
        
        // 구독 요청 완료 후 ready 신호 전송
        scope.launch {
            kotlinx.coroutines.delay(200) // 구독 요청이 완료될 시간을 줌
            _subscriptionReady.emit(true)
            Log.d("STOMP", "구독 완료 신호 전송")
        }
    }


    val FIXED_SENDER_UUID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001") // 삭제 예정
    fun sendMessage(token: String, content: String, roomId: Long, senderUUID: UUID) {
        val json = """
            {
              "roomId": $roomId,
              "senderId": "$senderUUID",
              "message": "$content",
              "type": "TALK"
            }
        """.trimIndent()

        val headers = listOf(
            StompHeader("Authorization", "Bearer $token"),
            StompHeader("destination", "/pub/chat/goods/message")
        )

        val message = StompMessage(
            StompCommand.SEND,
            headers,
            json
        )

        stompClient.send(message)
            .subscribe({
                Log.d("STOMP", "메시지 전송 성공: $json")
            }, { error ->
                Log.e("STOMP", "메시지 전송 실패", error)
            })
    }


    fun disconnect() {
        stompClient.disconnect()
    }
}
