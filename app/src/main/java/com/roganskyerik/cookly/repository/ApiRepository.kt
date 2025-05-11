package com.roganskyerik.cookly.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.roganskyerik.cookly.network.ApiService
import com.roganskyerik.cookly.network.ChangePasswordRequest
import com.roganskyerik.cookly.network.LoginRequest
import com.roganskyerik.cookly.network.LoginResponse
import com.roganskyerik.cookly.network.LogoutAllRequest
import com.roganskyerik.cookly.network.LogoutRequest
import com.roganskyerik.cookly.network.OauthLoginRequest
import com.roganskyerik.cookly.network.PostReviewRequest
import com.roganskyerik.cookly.network.RecipeAnalysisResponse
import com.roganskyerik.cookly.network.RecipeByIdResponse
import com.roganskyerik.cookly.network.RecipeDescriptionResponse
import com.roganskyerik.cookly.network.RecipeOverview
import com.roganskyerik.cookly.network.RecipeResponse
import com.roganskyerik.cookly.network.RegisterRequest
import com.roganskyerik.cookly.network.RegisterResponse
import com.roganskyerik.cookly.network.ReminderRequest
import com.roganskyerik.cookly.network.UpdateUserRequest
import com.roganskyerik.cookly.network.UserData
import com.roganskyerik.cookly.ui.Mode
import com.roganskyerik.cookly.ui.Recipe
import com.roganskyerik.cookly.ui.Tag
import com.roganskyerik.cookly.utils.getDeviceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.net.URL
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

    suspend fun changePicture(imageUri: Uri, context: Context): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(imageUri)
                val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)

                inputStream?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val requestBody = tempFile
                    .asRequestBody("image/jpeg".toMediaTypeOrNull())

                val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

                apiService.changePicture(filePart)

                Result.success(Unit)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }



    suspend fun fetchTags(): Result<List<Tag>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.fetchTags()
                Result.success(response)
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

    suspend fun createRecipe(recipe: Recipe, context: Context): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver

                fun isRemoteUri(uri: Uri): Boolean {
                    return uri.scheme == "http" || uri.scheme == "https"
                }

                fun createTempFileFromUri(uri: Uri, filePrefix: String): File {
                    val tempFile = File.createTempFile(filePrefix, ".jpg", context.cacheDir)

                    if (isRemoteUri(uri)) {
                        URL(uri.toString()).openStream().use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    } else {
                        contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output)
                            }
                        } ?: throw IllegalArgumentException("Unable to open URI: $uri")
                    }

                    return tempFile
                }

                val coverPhotoPart = recipe.coverPhoto?.let { uri ->
                    val tempFile = createTempFileFromUri(uri, "coverPhoto")
                    val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("coverPhoto", tempFile.name, requestBody)
                }

                val imagesParts = recipe.photos.map { uri ->
                    val tempFile = createTempFileFromUri(uri, "photo")
                    val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("images[]", tempFile.name, requestBody)
                }

                var recipeIdPart: MultipartBody.Part? = null
                if (recipe.recipeId != null) {
                    recipeIdPart = MultipartBody.Part.createFormData("recipeId", recipe.recipeId)
                }
                val titlePart = MultipartBody.Part.createFormData("title", recipe.title)
                val descriptionPart = MultipartBody.Part.createFormData("description", recipe.description)
                val tagsPart = MultipartBody.Part.createFormData("tags", recipe.tags.joinToString(","))
                val ingredientsPart = MultipartBody.Part.createFormData(
                    "ingredients",
                    recipe.ingredients.joinToString(",") { "${it.icon},${it.name},${it.quantity}" }
                )
                val instructionsPart = MultipartBody.Part.createFormData("instructions", recipe.instructions.joinToString("\n"))
                val isPublicPart = MultipartBody.Part.createFormData("isPublic", recipe.isPublic.toString())
                val detailsPart = MultipartBody.Part.createFormData(
                    "details",
                    "prepTime: ${recipe.prepTime}, difficulty: ${recipe.difficulty}, servings: ${recipe.servings}, calories: ${recipe.calories}"
                )
                var countryPart: MultipartBody.Part? = null
                if (recipe.country != null) {
                    countryPart = MultipartBody.Part.createFormData("country", recipe.country)
                }

                apiService.createRecipe(
                    recipeId = recipeIdPart,
                    title = titlePart,
                    tags = tagsPart,
                    ingredients = ingredientsPart,
                    instructions = instructionsPart,
                    isPublic = isPublicPart,
                    coverPhoto = coverPhotoPart,
                    images = imagesParts,
                    description = descriptionPart,
                    details = detailsPart,
                    country = countryPart
                )

                Result.success(Unit)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Log.e("RecipeUpload", "Error: $errorMessage")
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Log.e("RecipeUpload", "Error: ${e.message}")
                Result.failure(e)
            }
        }
    }


    suspend fun getOwnRecipes(): Result<RecipeResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOwnRecipes()
                Result.success(response)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getPublicRecipes(): Result<RecipeResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPublicRecipes()
                Result.success(response)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getRecipeById(recipeId: String): Result<RecipeByIdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRecipeById(recipeId)
                Result.success(response)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun postReview(recipeId: String, rating: Int, comment: String?): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.postReview(PostReviewRequest(recipeId.toInt(), rating, comment))
                Result.success(Unit)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun generateDescription(jsonString: MutableMap<String, Any>): Result<RecipeDescriptionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.generateDescription(jsonString)
                Result.success(response)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun generateDetails(jsonString: MutableMap<String, Any>): Result<RecipeAnalysisResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.generateDetails(jsonString)
                Result.success(response)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun setHydrationReminder(
        timezone: String?,
        startHour: Int?,
        endHour: Int?,
        interval: Int?,
        remove: Boolean?,
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.setHydrationReminder(
                    ReminderRequest(
                        timezone = timezone,
                        startHour = startHour,
                        endHour = endHour,
                        interval = interval,
                        remove = remove
                    )
                )
                Result.success(Unit)
            } catch (e: HttpException) {
                val errorMessage = extractErrorMessage(e)
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}