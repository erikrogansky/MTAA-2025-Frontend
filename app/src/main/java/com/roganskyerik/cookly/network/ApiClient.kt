package com.roganskyerik.cookly.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.roganskyerik.cookly.utils.TokenManager
import kotlinx.coroutines.runBlocking

suspend fun refreshAccessToken(context: Context): String? {
    val refreshToken = TokenManager.getRefreshToken(context) ?: return null

    return try {
        val response = ApiClient.create(context).refreshToken(refreshToken) // ✅ Call suspend function inside coroutine

        if (response.isNotEmpty()) { // ✅ Fix unresolved reference `isSuccessful`
            TokenManager.saveTokens(context, response, refreshToken) // ✅ Save new access token
            response
        } else null
    } catch (e: Exception) {
        null
    }
}

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = TokenManager.getAccessToken(context)
        val requestBuilder = chain.request().newBuilder()

        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        var response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            runBlocking {
                val newAccessToken = refreshAccessToken(context)
                if (newAccessToken != null) {
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $newAccessToken")
                        .build()
                    response = chain.proceed(newRequest)
                }
            }
        }

        return response
    }
}
object ApiClient {
    private const val BASE_URL = "http://176.123.1.22/"

    fun create(context: Context): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}