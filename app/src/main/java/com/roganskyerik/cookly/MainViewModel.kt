package com.roganskyerik.cookly

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.roganskyerik.cookly.network.FullRecipe
import com.roganskyerik.cookly.network.LoginResponse
import com.roganskyerik.cookly.network.RecipeAnalysisResponse
import com.roganskyerik.cookly.network.RecipeByIdResponse
import com.roganskyerik.cookly.network.RecipeOverview
import com.roganskyerik.cookly.network.RecipeResponse
import com.roganskyerik.cookly.network.RegisterResponse
import com.roganskyerik.cookly.network.UserData
import com.roganskyerik.cookly.network.isOnline
import com.roganskyerik.cookly.permissions.PreferencesManager
import com.roganskyerik.cookly.repository.ApiRepository
import com.roganskyerik.cookly.ui.Ingredient
import com.roganskyerik.cookly.ui.Mode
import com.roganskyerik.cookly.ui.Recipe
import com.roganskyerik.cookly.ui.Tag
import com.roganskyerik.cookly.utils.LocalRecipeManager
import com.roganskyerik.cookly.utils.TokenManager
import com.roganskyerik.cookly.utils.WebSocketManager
import dagger.hilt.android.internal.Contexts.getApplication
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
    private var appContext: Context? = null

    fun setContext(context: Context) {
        appContext = context.applicationContext
    }

    private val _recipe = MutableStateFlow<FullRecipe?>(null)
    val recipe: StateFlow<FullRecipe?> = _recipe

    fun loadRecipe(recipeId: String, isOffline: Boolean, context: Context) {
        viewModelScope.launch {
            if (isOffline) {
                val localRecipe = LocalRecipeManager.loadRecipeById(context, recipeId)
                _recipe.value = localRecipe
            } else {
                getRecipeById(recipeId) { response, error ->
                    if (response != null) {
                        _recipe.value = response.recipe
                    } else {
                        Log.e("ViewModel", "Error loading recipe: $error")
                    }
                }
            }
        }
    }

    fun removeRecipe() {
        viewModelScope.launch {
            _recipe.value = null
        }
    }



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
                val urlWithTimestamp = response.profilePictureUrl?.let { "$it?ts=${System.currentTimeMillis()}" }
                _userData.value = response.copy(profilePictureUrl = urlWithTimestamp ?: "")
            }
            result.onFailure { error ->
                // handle error if needed
            }
        }
    }


    fun setPassword() {
        viewModelScope.launch {
            _userData.value = _userData.value?.copy(hasPassword = true)
        }
    }

    fun updateUser(name: String? = null, profilePicture: String? = null, mode: Mode? = null, preferences: List<String>? = null) {
        viewModelScope.launch {
            if (mode != null) {
                preferencesManager.setThemeMode(mode.value)
                _userData.value = _userData.value?.copy(darkMode = mode.value)
            }
            if (name != null) {
                _userData.value = _userData.value?.copy(name = name)
            }

            val result = repository.updateUser(name, profilePicture, mode, preferences)
            result.onFailure {

            }
            result.onSuccess {

            }
        }
    }

    val themeMode = preferencesManager.themeMode.stateIn(
        viewModelScope, SharingStarted.Lazily, Mode.SYSTEM
    )

    fun setTheme(mode: Mode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode.value)
        }
    }

    fun resetTheme() {
        viewModelScope.launch {
            preferencesManager.setThemeMode(Mode.SYSTEM.value)
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, onResult: (Unit?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.changePassword(currentPassword, newPassword)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun deleteAccount(onResult: (Unit?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteAccount()
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun changePicture(picture: Uri, context: Context, onResult: (Unit?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.changePicture(picture, context)
            result.onSuccess { response ->
                val currentUrl = _userData.value?.profilePictureUrl

                val cleanUrl = currentUrl
                    ?.replace(Regex("[?&]ts=\\d+"), "")
                    ?.removeSuffix("?")
                    ?.removeSuffix("&")

                val updatedUrl = if (cleanUrl?.contains('?') == true) {
                    "$cleanUrl&ts=${System.currentTimeMillis()}"
                } else {
                    "$cleanUrl?ts=${System.currentTimeMillis()}"
                }

                _userData.value = _userData.value?.copy(profilePictureUrl = updatedUrl)
            }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }


    // Tags
    fun fetchTags(onResult: (List<Tag>?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.fetchTags()
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

                    WebSocketManager.closeWebSocket()
                }
                "recipe_update" -> {
                    Log.d("WebSocket", "Recipe update received: $text")
                    val recipeId = jsonObject.getString("recipeId")

                    viewModelScope.launch {
                        val offline = !appContext?.let { isOnline(it) }!!
                        loadRecipe(recipeId, offline, appContext!!)
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


    fun sendMessageToServer(message: String) {
        WebSocketManager.sendMessage(message)
    }

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

//    val isFileManagerEnabled = preferencesManager.isFileManagerEnabled.stateIn(
//        viewModelScope, SharingStarted.Lazily, false
//    )

    val isLocationEnabled = preferencesManager.isLocationEnabled.stateIn(
        viewModelScope, SharingStarted.Lazily, false
    )

    val isReminderEnabled = preferencesManager.isReminderEnabled.stateIn(
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

//    fun toggleFileManager(enabled: Boolean) {
//        viewModelScope.launch {
//            preferencesManager.setFileManagerEnabled(enabled)
//        }
//    }

    fun toggleLocation(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setLocationEnabled(enabled)
        }
    }

    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setReminderEnabled(enabled)
        }
    }


    // Recipe methods
    fun createRecipe(recipe: Recipe, context: Context, onResult: (Unit?, String?) -> Unit) {
        Log.d("MainViewModel", recipe.toString())

        viewModelScope.launch {
            val result = repository.createRecipe(recipe, context)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }


    fun getOwnRecipes(onResult: (RecipeResponse?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.getOwnRecipes()
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun getPublicRecipes(onResult: (RecipeResponse?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.getPublicRecipes()
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun getRecipeById(recipeId: String, onResult: (RecipeByIdResponse?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.getRecipeById(recipeId)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun postReview(recipeId: String, rating: Int, comment: String, onResult: (Unit?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.postReview(recipeId, rating, comment)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun generateDescription(title: String, ingredients: List<Ingredient>, instructions: List<String>, onResult: (String?, String?) -> Unit) {
        viewModelScope.launch {
            val jsonResponse = mutableMapOf<String, Any>()
            jsonResponse["title"] = title
            val ingredientsList = ingredients.map { ingredient ->
                mapOf("name" to ingredient.name, "quantity" to ingredient.quantity)
            }
            jsonResponse["ingredients"] = ingredientsList
            jsonResponse["instructions"] = instructions

            val result = repository.generateDescription(jsonResponse)
            result.onSuccess { response -> onResult(response.description, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }

    fun generateDetails(title: String, ingredients: List<Ingredient>, instructions: List<String>, onResult: (RecipeAnalysisResponse?, String?) -> Unit) {
        viewModelScope.launch {
            val jsonResponse = mutableMapOf<String, Any>()
            jsonResponse["title"] = title
            val ingredientsList = ingredients.map { ingredient ->
                mapOf("name" to ingredient.name, "quantity" to ingredient.quantity)
            }
            jsonResponse["ingredients"] = ingredientsList
            jsonResponse["instructions"] = instructions

            val result = repository.generateDetails(jsonResponse)
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }


    fun setHydrationReminder(
        timezone: String?,
        startHour: Int?,
        endHour: Int?,
        interval: Int?,
        remove: Boolean?,
        onResult: (Unit?, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.setHydrationReminder(
                timezone,
                startHour,
                endHour,
                interval,
                remove
            )
            result.onSuccess { response -> onResult(response, null) }
            result.onFailure { error -> onResult(null, error.message) }
        }
    }
}
