package com.roganskyerik.cookly.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.roganskyerik.cookly.MainViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to Cookly!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val refreshToken = viewModel.getRefreshToken() ?: "no_token"

                viewModel.logout(refreshToken) { response, error ->
                    if (response != null) {
                        // Do nothing
                    } else {
                        errorMessage = error
                    }

                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                    viewModel.clearTokens()
                }
        }) {
            Text("Logout")
        }
    }
}
