package com.roganskyerik.cookly.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

data class LoginRequest(val email: String, val password: String, val firebaseToken: String, val deviceId: String)
data class LoginResponse(val accessToken: String, val refreshToken: String)

data class OauthLoginRequest(val idToken: String, val firebaseToken: String, val provider: String, val deviceId: String)

data class RegisterRequest(val name: String, val email: String, val password: String, val preferences: Array<String>, val deviceId: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegisterRequest

        return preferences.contentEquals(other.preferences)
    }

    override fun hashCode(): Int {
        return preferences.contentHashCode()
    }
}
data class RegisterResponse(val accessToken: String, val refreshToken: String)

data class LogoutRequest(val refreshToken: String, val deviceId: String)
data class LogoutAllRequest(val refreshToken: String)

data class RefreshTokenRequest(val refreshToken: String, val deviceId: String)
data class RefreshTokenResponse(val accessToken: String)

data class UserData(val name: String, val hasPassword: Boolean, val hasFacebookAuth: Boolean, val hasGoogleAuth: Boolean, val darkMode: String)

data class UpdateUserRequest(val name: String? = null, val profilePicture: String? = null, val mode: String? = null, val preferences: List<String>? = null)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/oauth")
    suspend fun loginWithGoogle(@Body request: OauthLoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest)

    @POST("auth/logout-all")
    suspend fun logoutAll(@Body request: LogoutAllRequest)

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponse

    @GET("users/get-data")
    suspend fun fetchUserData(): UserData

    @PUT("users/update")
    suspend fun updateUser(@Body request: UpdateUserRequest)

    @PUT("users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest)

    @DELETE("users/delete")
    suspend fun deleteAccount()
}