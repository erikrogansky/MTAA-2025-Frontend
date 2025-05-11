package com.roganskyerik.cookly.utils

import android.util.Log
import okhttp3.*
import java.util.concurrent.TimeUnit

object WebSocketManager {

    private var webSocket: WebSocket? = null
    private var webSocketListener: WebSocketListener? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    fun connectWebSocket(url: String, listener: WebSocketListener) {
        if (webSocket == null) {
            val request = Request.Builder().url(url).build()
            webSocket = client.newWebSocket(request, listener)
            webSocketListener = listener
        } else {
            Log.d("WebSocket", "Already connected, skipping reconnection")
        }
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun closeWebSocket() {
        Log.d("WebSocket", "Closing WebSocket.")
        webSocket?.close(1000, "Closing connection.")
        webSocket = null
    }

    fun isConnected(): Boolean = webSocket != null
}
