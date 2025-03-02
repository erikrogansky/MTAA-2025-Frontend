package com.roganskyerik.cookly.network

import retrofit2.http.*

data class LoginRequest(val email: String, val password: String, val deviceId: String)
data class LoginResponse(val accessToken: String, val refreshToken: String)

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

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest)

    @POST("auth/refresh")
    suspend fun refreshToken(@Body refreshToken: String): String
}