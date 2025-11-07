//package com.livon.app.data.remote.socket
//
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.WebSocket
//
//object ChatWebSocketManager {
//
//    private val client = OkHttpClient()
//    private var webSocket: WebSocket? = null
//
//    fun connect() {
//        val request = Request.Builder()
//            .url("https://k13s406.p.ssafy.io/api/v1/ws/chat")
//            .build()
//
//        val listener = ChatWebSocketListener()
//        webSocket = client.newWebSocket(request, listener)
//    }
//
//    fun send(message: String) {
//        webSocket?.send(message)
//    }
//
//    fun close() {
//        webSocket?.close(1000, "User left")
//    }
//}
