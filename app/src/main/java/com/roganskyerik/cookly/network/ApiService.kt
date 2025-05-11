package com.roganskyerik.cookly.network

import android.telecom.Call.Details
import com.roganskyerik.cookly.ui.Ingredient
import com.roganskyerik.cookly.ui.RecipeDetails
import com.roganskyerik.cookly.ui.Tag
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

data class LoginRequest(val email: String, val password: String, val firebaseToken: String, val deviceId: String)
data class LoginResponse(val accessToken: String, val refreshToken: String, val darkMode: String)

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

data class UserData(val name: String, val hasPassword: Boolean, val hasFacebookAuth: Boolean, val hasGoogleAuth: Boolean, val darkMode: String, val profilePictureUrl: String)

data class UpdateUserRequest(val name: String? = null, val profilePicture: String? = null, val mode: String? = null, val preferences: List<String>? = null)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)

data class RecipeResponse(
    val recipes: List<RecipeOverview>
)

data class RecipeOverview(
    val id: Int,
    val title: String,
    val coverPhotoUrl: String,
    val prepTime: Float,
    val difficulty: String,
    val servings: String,
    val calories: String,
    val firstTag: Tag?,
    val overallRating: Int,
    val country: String?
)

data class RecipeByIdResponse(
    val recipe: FullRecipe
)

data class FullRecipe(
    val id: Int,
    val title: String,
    val coverPhotoUrl: String,
    val prepTime: Float,
    val difficulty: String,
    val servings: String,
    val calories: String,
    val tags: List<Tag>,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val isPublic: Boolean,
    val description: String,
    val images: List<String>,
    val reviews: List<Review>,
    val isOwn: Boolean,
    val country: String?,
)

data class Review(
    val rating: Int,
    val text: String?,
    val user: ReviewUser,
    val createdAt: String,
)

data class ReviewUser(
    val name: String,
    val profilePictureUrl: String
)

data class PostReviewRequest(
    val recipeId: Int,
    val rating: Int,
    val comment: String?
)

data class RecipeDescriptionResponse(
    val description: String
)

data class RecipeAnalysisResponse(
    val cook_length: Float,
    val difficulty: String,
    val portions: Int,
    val calories: Int
)

data class ReminderRequest(
    val timezone: String?,
    val startHour: Int?,
    val endHour: Int?,
    val interval: Int?,
    val remove: Boolean?,
)


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

    @Multipart
    @POST("users/change-picture")
    suspend fun changePicture(@Part file: MultipartBody.Part)

    @POST("users/set-hydration-reminder")
    suspend fun setHydrationReminder(@Body request: ReminderRequest)

    @GET("tags/get-all")
    suspend fun fetchTags(): List<Tag>

    @Multipart
    @POST("recipes/create")
    suspend fun createRecipe(
        @Part recipeId: MultipartBody.Part?,
        @Part title: MultipartBody.Part,
        @Part tags: MultipartBody.Part,
        @Part ingredients: MultipartBody.Part,
        @Part instructions: MultipartBody.Part,
        @Part isPublic: MultipartBody.Part,
        @Part coverPhoto: MultipartBody.Part?,
        @Part images: List<MultipartBody.Part>,
        @Part description: MultipartBody.Part,
        @Part details: MultipartBody.Part,
        @Part country: MultipartBody.Part?,
    )

    @GET("recipes/get-own")
    suspend fun getOwnRecipes(): RecipeResponse

    @GET("recipes/get-public")
    suspend fun getPublicRecipes(): RecipeResponse

    @GET("recipes/get-by-id/{id}")
    suspend fun getRecipeById(@Path("id") id: String): RecipeByIdResponse

    @POST("recipes/post-review")
    suspend fun postReview(@Body review: PostReviewRequest)

    @POST("ai/recipe-description")
    suspend fun generateDescription(@Body json: MutableMap<String, Any>): RecipeDescriptionResponse

    @POST("ai/recipe-details")
    suspend fun generateDetails(@Body json: MutableMap<String, Any>): RecipeAnalysisResponse
}