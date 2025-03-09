package com.roganskyerik.cookly

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roganskyerik.cookly.network.LoginResponse
import com.roganskyerik.cookly.network.RegisterResponse
import com.roganskyerik.cookly.repository.ApiRepository
import com.roganskyerik.cookly.utils.TokenManager
import com.roganskyerik.cookly.utils.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ApiRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _forceLogout = MutableStateFlow(false)  // Use StateFlow
    val forceLogout: StateFlow<Boolean> = _forceLogout

    fun triggerForceLogout() {
        viewModelScope.launch {
            _forceLogout.emit(false) // Reset first
            delay(50) // Small delay to allow UI to recognize the state change
            _forceLogout.emit(true) // Now trigger logout
        }
    }

    fun resetForceLogout() {
        viewModelScope.launch {
            _forceLogout.emit(false)
        }
    }


    fun login(email: String, password: String, onResult: (LoginResponse?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(email, password)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun register(name: String, email: String, password: String, onResult: (RegisterResponse?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.register(name, email, password)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun logout(refreshToken: String, onResult: (Unit?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.logout(refreshToken)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun logoutAll(refreshToken: String, onResult: (Unit?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.logoutAll(refreshToken)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    // Socket implementation
    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket", "Connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val jsonObject = JSONObject(text)

            if (!jsonObject.has("type")) {
                Log.e("WebSocket", "Received message missing 'type': $text")
                return
            }

            val type = jsonObject.getString("type")

            when (type) {
                "force_logout" -> {
                    val message = jsonObject.getString("message")
                    Log.d("WebSocket", "Forced logout: $message")

                    if(!viewModelScope.isActive) {
                        Log.e("ViewModel", "Coroutine scope is not active")
                        return
                    }

                    viewModelScope.launch {
                        Log.d("ViewModel", "Inside coroutine - Executing toggleForceLogout()")
                        triggerForceLogout()
                    }
                }
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("WebSocket", "Received bytes: ${bytes.hex()}")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            val formattedReason = reason.ifEmpty { "No reason provided" }
            Log.d("WebSocket", "Closed. Code: $code, Reason: $formattedReason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "Error: ${t.message}")
        }
    }

    fun startWebSocket(accessToken: String) {
        if (WebSocketManager.isConnected()) {
            return
        }
        val wsUrl = "ws://176.123.1.22:80?token=$accessToken"
        WebSocketManager.connectWebSocket(wsUrl, webSocketListener)
    }


//    fun sendMessageToServer(message: String) {
//        WebSocketManager.sendMessage(message)
//    }

    override fun onCleared() {
        // Do not close the WebSocket here, as it should be kept open until the user logs out
        Log.d("ViewModel", "ViewModel cleared")
    }


    fun saveTokens(accessToken: String, refreshToken: String) {
        tokenManager.saveTokens(accessToken, refreshToken)
    }

    fun saveAccessToken(accessToken: String) {
        tokenManager.saveAccessToken(accessToken)
    }

    fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }

    fun getRefreshToken(): String? {
        return tokenManager.getRefreshToken()
    }

    fun clearTokens() {
        tokenManager.clearTokens()
    }

    suspend fun refreshToken(): String? {
        return withContext(Dispatchers.IO) {
            tokenManager.refreshAccessToken()
        }
    }
}
