package com.roganskyerik.cookly


import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.roganskyerik.cookly.ui.theme.CooklyTheme

import com.roganskyerik.cookly.ui.LoginScreen
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.roganskyerik.cookly.network.refreshAccessToken
import com.roganskyerik.cookly.repository.ApiRepository
import com.roganskyerik.cookly.ui.AccountScreen
import com.roganskyerik.cookly.ui.BottomNavigationBar
import com.roganskyerik.cookly.ui.CreateScreen
import com.roganskyerik.cookly.ui.DiscoverScreen
import com.roganskyerik.cookly.ui.HomeScreen
import com.roganskyerik.cookly.ui.RegistrationScreen
import com.roganskyerik.cookly.ui.SplashScreen
import com.roganskyerik.cookly.ui.modals.ModalManager
import com.roganskyerik.cookly.ui.modals.ModalManagerViewModel
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import com.roganskyerik.cookly.utils.AppLifecycleObserver
import com.roganskyerik.cookly.utils.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CooklyTheme {
                AppNavigation(LocalContext.current)
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }
}


@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()

    val modalManagerViewModel: ModalManagerViewModel = viewModel()
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(ApiRepository(context)))

    var startDestination by remember { mutableStateOf("splash") }
    val modalType by modalManagerViewModel.modalType.collectAsState()

    val colors = LocalCooklyColors.current

    val systemUiController = rememberSystemUiController()
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5
    val statusBarIconColor = !isDarkTheme

    SideEffect {
        systemUiController.setStatusBarColor(
            color = colors.Background,
            darkIcons = statusBarIconColor,

        )
        systemUiController.setNavigationBarColor(
            color = colors.DarkOrange,
            darkIcons = true
        )
    }

    LaunchedEffect(Unit) {
        viewModel.forceLogoutEvent.collectLatest {
            TokenManager.clearTokens(context)
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        val accessToken = TokenManager.getAccessToken(context)
        if (accessToken != null) {
            viewModel.startWebSocket(accessToken)
        }
    }

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
        } else {
            // Connect to user's socket
        }
        navController.navigate(startDestination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    val bottomNavScreens = listOf("home", "discover", "create", "account")

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (bottomNavScreens.contains(currentRoute(navController))) {
                    BottomNavigationBar(navController)
                }
            },
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.padding(paddingValues).background(colors.Background)
            ) {
                composable("splash") { SplashScreen() }
                composable("login") { LoginScreen(navController) }
                composable("register") { RegistrationScreen(navController) }
                composable("home") { HomeScreen(navController) }
                composable("discover") { DiscoverScreen(navController) }
                composable("create") { CreateScreen(navController) }
                composable("account") { AccountScreen(navController, modalManagerViewModel::showModal) }
            }
        }

        ModalManager(modalType, modalManagerViewModel::dismissModal)
    }
}


@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}