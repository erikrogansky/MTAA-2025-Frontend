package com.roganskyerik.cookly.utils

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        Log.d("WebSocket", "App moved to background. Keeping WebSocket open.")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d("WebSocket", "App fully exited. Closing WebSocket.")
        WebSocketManager.closeWebSocket()
    }
}