package com.roganskyerik.cookly.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.roganskyerik.cookly.MainViewModel
import com.roganskyerik.cookly.MainViewModelFactory
import com.roganskyerik.cookly.repository.ApiRepository
import com.roganskyerik.cookly.utils.TokenManager

@Composable
fun AccountScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(ApiRepository(context)))

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to Cookly!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val refreshToken = TokenManager.getRefreshToken(context) ?: "no_token"

                viewModel.logout(refreshToken) { response, error ->
                    if (response != null) {
                        // Do nothing
                    } else {
                        errorMessage = error
                    }

                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                    TokenManager.clearTokens(context)
                }
        }) {
            Text("Logout")
        }
    }
}
