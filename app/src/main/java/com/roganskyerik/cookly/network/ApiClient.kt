package com.roganskyerik.cookly.network

import android.content.Context
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.runBlocking
import com.roganskyerik.cookly.utils.TokenManager
import com.roganskyerik.cookly.utils.getDeviceId

class TokenAuthenticator(private val context: Context) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val newAccessToken = runBlocking { refreshAccessToken(context) } ?: return null

        // Update TokenManager with the new access token
        TokenManager.saveAccessToken(context, newAccessToken)

        // Retry the request with the new token
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }
}

suspend fun refreshAccessToken(context: Context): String? {
    val refreshToken = TokenManager.getRefreshToken(context) ?: return null

    return try {
        val response = ApiClient.createWithoutAuth().refreshToken(RefreshTokenRequest(refreshToken, getDeviceId(context)))
        TokenManager.saveAccessToken(context, response.accessToken)
        response.accessToken
    } catch (e: Exception) {
        null
    }
}

object ApiClient {
    private const val BASE_URL = "http://176.123.1.22/"

    fun create(context: Context): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .authenticator(TokenAuthenticator(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun createWithoutAuth(): ApiService {
        val client = OkHttpClient.Builder().build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = TokenManager.getAccessToken(context)
        val requestBuilder = chain.request().newBuilder()

        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(requestBuilder.build())
    }
}