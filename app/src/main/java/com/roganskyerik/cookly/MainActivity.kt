package com.roganskyerik.cookly

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.roganskyerik.cookly.ui.theme.CooklyTheme

import com.roganskyerik.cookly.ui.LoginScreen
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.roganskyerik.cookly.network.refreshAccessToken
import com.roganskyerik.cookly.ui.HomeScreen
import com.roganskyerik.cookly.ui.RegistrationScreen
import com.roganskyerik.cookly.ui.SplashScreen
import com.roganskyerik.cookly.utils.TokenManager
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CooklyTheme {
                AppNavigation(LocalContext.current)
            }
        }
    }
}


@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()

    var startDestination by remember { mutableStateOf("splash") }

    LaunchedEffect(Unit) {
        val refreshToken = TokenManager.getRefreshToken(context)

        delay(1500)

        startDestination = when {
            refreshToken != null -> {
                val newAccessToken = refreshAccessToken(context)
                if (newAccessToken != null) {
                    TokenManager.saveAccessToken(context, newAccessToken)
                    "home"
                } else {
                    "login"
                }
            }
            else -> "login"
        }

        if (startDestination == "login") {
            TokenManager.clearTokens(context)
        }

        navController.navigate(startDestination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen() }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegistrationScreen(navController) }
        composable("home") { HomeScreen(navController) }
    }
}
