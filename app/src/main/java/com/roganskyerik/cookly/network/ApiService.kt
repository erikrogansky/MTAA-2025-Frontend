package com.roganskyerik.cookly.network

import retrofit2.http.*

data class LoginRequest(val email: String, val password: String, val deviceId: String)
data class LoginResponse(val accessToken: String, val refreshToken: String)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body refreshToken: String): String
}