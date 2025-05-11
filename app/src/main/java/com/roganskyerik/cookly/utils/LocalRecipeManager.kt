package com.roganskyerik.cookly.utils

import android.content.Context
import com.google.gson.Gson
import com.roganskyerik.cookly.network.FullRecipe
import java.io.File

object LocalRecipeManager {
    private const val DIRECTORY = "recipes"

    fun saveRecipe(context: Context, recipe: FullRecipe) {
        val dir = File(context.filesDir, DIRECTORY)
        if (!dir.exists()) dir.mkdir()
        val file = File(dir, "${recipe.id}.json")
        file.writeText(Gson().toJson(recipe))
    }

    fun deleteRecipe(context: Context, recipeId: String) {
        val file = File(File(context.filesDir, DIRECTORY), "$recipeId.json")
        if (file.exists()) file.delete()
    }

    fun isRecipeSaved(context: Context, recipeId: String): Boolean {
        val file = File(File(context.filesDir, DIRECTORY), "$recipeId.json")
        return file.exists()
    }

    fun getAllSavedRecipes(context: Context): List<FullRecipe> {
        val dir = File(context.filesDir, DIRECTORY)
        if (!dir.exists()) return emptyList()

        return dir.listFiles()?.mapNotNull { file ->
            try {
                Gson().fromJson(file.readText(), FullRecipe::class.java)
            } catch (e: Exception) {
                null // Skip malformed or unreadable files
            }
        } ?: emptyList()
    }

    fun loadRecipeById(context: Context, recipeId: String): FullRecipe? {
        val file = File(File(context.filesDir, "recipes"), "$recipeId.json")
        return if (file.exists()) {
            try {
                Gson().fromJson(file.readText(), FullRecipe::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun deleteAllRecipes(context: Context) {
        val dir = File(context.filesDir, "recipes")
        if (dir.exists()) {
            dir.listFiles()?.forEach { it.delete() }
        }
    }
}
