package com.roganskyerik.cookly.repository

import android.content.Context
import com.roganskyerik.cookly.network.ApiClient
import com.roganskyerik.cookly.network.ApiService
import com.roganskyerik.cookly.network.LoginRequest
import com.roganskyerik.cookly.network.LoginResponse
import com.roganskyerik.cookly.utils.getDeviceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class ApiRepository(context: Context) {
    private val apiService: ApiService = ApiClient.create(context)
    private val deviceId = getDeviceId(context)

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password, deviceId)) // ✅ Call API
                Result.success(response)
            } catch (e: HttpException) {  // ✅ Catch API errors (like 400 Bad Request)
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun extractErrorMessage(exception: HttpException): String {
        return try {
            val errorBody = exception.response()?.errorBody()?.string()
            val json = JSONObject(errorBody ?: "{}")
            json.optString("message", "Something went wrong")  // ✅ Get "message" field from API
        } catch (e: Exception) {
            "Something went wrong"
        }
    }
}
