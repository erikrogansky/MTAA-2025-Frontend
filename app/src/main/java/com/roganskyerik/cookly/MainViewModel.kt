package com.roganskyerik.cookly

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roganskyerik.cookly.network.LoginResponse
import com.roganskyerik.cookly.network.RegisterResponse
import com.roganskyerik.cookly.network.UserData
import com.roganskyerik.cookly.permissions.PreferencesManager
import com.roganskyerik.cookly.repository.ApiRepository
import com.roganskyerik.cookly.ui.Mode
import com.roganskyerik.cookly.utils.TokenManager
import com.roganskyerik.cookly.utils.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    // Authentication methods
    private val _forceLogout = MutableStateFlow(false)
    val forceLogout: StateFlow<Boolean> = _forceLogout

    fun triggerForceLogout() {
        viewModelScope.launch {
            _forceLogout.emit(false)
            delay(50)
            _forceLogout.emit(true)
        }
    }

    fun resetForceLogout() {
        viewModelScope.launch {
            _forceLogout.emit(false)
        }
    }


    fun login(email: String, password: String, firebaseToken: String, onResult: (LoginResponse?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(email, password, firebaseToken)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun loginWithGoogle(idToken: String,firebaseToken: String, provider: String, onResult: (LoginResponse?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.loginWithGoogle(idToken, firebaseToken, provider)
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


    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            val result = repository.fetchUserData()
            result.onSuccess { response ->
                _userData.value = response
                Log.d("MainViewModel", "Fetched user data: $response")
            }
            result.onFailure { error ->
                Log.e("MainViewModel", "Failed to fetch user data: $error")
            }
        }
    }

    fun setMode(mode: Mode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode.value)
            _userData.value = _userData.value?.copy(darkMode = mode.value)
            val result = repository.updateMode(mode)
            result.onFailure { error ->
                Log.e("MainViewModel", "Failed to update mode: $error")
            }
            result.onSuccess {
                Log.d("MainViewModel", "Updated mode to: ${mode.value}")
            }
        }
    }

    val themeMode = preferencesManager.themeMode.stateIn(
        viewModelScope, SharingStarted.Lazily, Mode.SYSTEM
    )


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

                    WebSocketManager.closeWebSocket()
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
        Log.d("MainViewModel", "onCleared")
    }


    // Token management
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



    // Preferences
    val isNotificationsEnabled = preferencesManager.isNotificationsEnabled.stateIn(
        viewModelScope, SharingStarted.Lazily, false
    )

    val isCameraEnabled = preferencesManager.isCameraEnabled.stateIn(
        viewModelScope, SharingStarted.Lazily, false
    )

    val isFileManagerEnabled = preferencesManager.isFileManagerEnabled.stateIn(
        viewModelScope, SharingStarted.Lazily, false
    )

    val isLocationEnabled = preferencesManager.isLocationEnabled.stateIn(
        viewModelScope, SharingStarted.Lazily, false
    )

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    fun toggleCamera(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setCameraEnabled(enabled)
        }
    }

    fun toggleFileManager(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setFileManagerEnabled(enabled)
        }
    }

    fun toggleLocation(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setLocationEnabled(enabled)
        }
    }
}
