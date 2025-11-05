package com.livon.app.data.remote.socket

import io.reactivex.android.schedulers.AndroidSchedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import android.util.Log
import ua.naiksoftware.stomp.dto.StompHeader

object ChatStompManager {

    private lateinit var stompClient: StompClient

    fun connect(token: String) {
        stompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "https://k13s406.p.ssafy.io/api/v1/ws/chat"
        )

        val headers = listOf(
            StompHeader("Authorization", "Bearer $token")
        )

        stompClient.connect(headers)

        stompClient.lifecycle()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { event ->
                when (event.type) {
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED ->
                        Log.d("STOMP", "연결 성공")
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR ->
                        Log.e("STOMP", "연결 오류", event.exception)
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED ->
                        Log.d("STOMP", "연결 종료")
                    else ->
                        Log.w("STOMP", "기타 이벤트: ${event.type}")
                }
            }
    }

    fun subscribe(roomId: Long = 3) {
        stompClient.topic("/topic/chat/room/$roomId")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { message ->
                Log.d("STOMP", "메시지 수신: ${message.payload}")
            }
    }

    fun sendMessage(sender: String, content: String, roomId: Long = 3) {
        val json = """
            {
              "senderNickName": "$sender",
              "message": "$content",
              "date": "2025-11-05"
            }
        """.trimIndent()

        stompClient.send("/app/chat.sendMessage", json).subscribe()
    }

    fun disconnect() {
        stompClient.disconnect()
    }
}
