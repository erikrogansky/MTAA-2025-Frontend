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
import com.roganskyerik.cookly.network.RegisterRequest
import com.roganskyerik.cookly.network.RegisterResponse
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

                // Prepare cover photo
                val coverPhotoPart = recipe.coverPhoto?.let { uri ->
                    val inputStream = contentResolver.openInputStream(uri)
                    val tempFile = File.createTempFile("coverPhoto", ".jpg", context.cacheDir)
                    inputStream?.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val filePart = MultipartBody.Part.createFormData("coverPhoto", tempFile.name, requestBody)
                    Log.d("RecipeUpload", "Cover photo: ${filePart.headers}")
                    filePart
                }

                // Prepare other photos
                val imagesParts = recipe.photos.map { uri ->
                    val inputStream = contentResolver.openInputStream(uri)
                    val tempFile = File.createTempFile("photo", ".jpg", context.cacheDir)
                    inputStream?.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val filePart = MultipartBody.Part.createFormData("images[]", tempFile.name, requestBody)
                    Log.d("RecipeUpload", "Image photo: ${filePart.headers}")
                    filePart
                }

                // Prepare other fields (title, tags, ingredients, etc.)
                val titlePart = MultipartBody.Part.createFormData("title", recipe.title)
                Log.d("RecipeUpload", "Title: ${recipe.title}")

                val descriptionPart = MultipartBody.Part.createFormData("description", recipe.description)
                Log.d("RecipeUpload", "Description: ${recipe.description}")

                val tagsPart = MultipartBody.Part.createFormData("tags", recipe.tags.joinToString(","))
                Log.d("RecipeUpload", "Tags: ${recipe.tags}")

                val ingredientsPart = MultipartBody.Part.createFormData("ingredients", recipe.ingredients.joinToString(",") { it.name })
                Log.d("RecipeUpload", "Ingredients: ${recipe.ingredients.joinToString(",") { it.name }}")

                val instructionsPart = MultipartBody.Part.createFormData("instructions", recipe.instructions.joinToString("\n"))
                Log.d("RecipeUpload", "Instructions: ${recipe.instructions.joinToString("\n")}")

                val isPublicPart = MultipartBody.Part.createFormData("isPublic", recipe.isPublic.toString())
                Log.d("RecipeUpload", "Is Public: ${recipe.isPublic}")

                val detailsPart = MultipartBody.Part.createFormData("details", "prepTime: ${recipe.prepTime}, difficulty: ${recipe.difficulty}, servings: ${recipe.servings}, calories: ${recipe.calories}")
                Log.d("RecipeUpload", "Details: prepTime=${recipe.prepTime}, difficulty=${recipe.difficulty}, servings=${recipe.servings}, calories=${recipe.calories}")

                // Make the request
                apiService.createRecipe(
                    title = titlePart,
                    tags = tagsPart,
                    ingredients = ingredientsPart,
                    instructions = instructionsPart,
                    isPublic = isPublicPart,
                    coverPhoto = coverPhotoPart,
                    images = imagesParts,
                    description = descriptionPart,
                    details = detailsPart
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
}