package com.roganskyerik.cookly.repository

import android.content.Context
import com.roganskyerik.cookly.network.ApiService
import com.roganskyerik.cookly.network.ChangePasswordRequest
import com.roganskyerik.cookly.network.LoginRequest
import com.roganskyerik.cookly.network.LoginResponse
import com.roganskyerik.cookly.network.LogoutAllRequest
import com.roganskyerik.cookly.network.LogoutRequest
import com.roganskyerik.cookly.network.OauthLoginRequest
import com.roganskyerik.cookly.network.RegisterRequest
import com.roganskyerik.cookly.network.RegisterResponse
import com.roganskyerik.cookly.network.UpdateUserRequest
import com.roganskyerik.cookly.network.UserData
import com.roganskyerik.cookly.ui.Mode
import com.roganskyerik.cookly.utils.getDeviceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class ApiRepository @Inject constructor(
    private val apiService: ApiService,
    private val context: Context
) {
    private val deviceId by lazy { getDeviceId(context) }

    suspend fun login(email: String, password: String, firebaseToken: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password, firebaseToken, deviceId))
                Result.success(response)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun loginWithGoogle(idToken: String, firebaseToken: String, provider: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.loginWithGoogle(OauthLoginRequest(idToken, firebaseToken, provider, deviceId))
                Result.success(response)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<RegisterResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(RegisterRequest(name, email, password, emptyArray(), deviceId))
                Result.success(response)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun logout(refreshToken: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.logout(LogoutRequest(refreshToken, deviceId))
                Result.success(Unit)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun logoutAll(refreshToken: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.logoutAll(LogoutAllRequest(refreshToken))
                Result.success(Unit)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchUserData(): Result<UserData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.fetchUserData()
                Result.success(response)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateUser(name: String? = null, profilePicture: String? = null, mode: Mode? = null, preferences: List<String>? = null): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateUser(UpdateUserRequest(name, profilePicture, mode?.value, preferences))
                Result.success(response)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.changePassword(ChangePasswordRequest(currentPassword, newPassword))
                Result.success(Unit)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.deleteAccount()
                Result.success(Unit)
            } catch (e: HttpException) {
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
            json.optString("message", "Something went wrong")
        } catch (e: Exception) {
            "Something went wrong"
        }
    }
}