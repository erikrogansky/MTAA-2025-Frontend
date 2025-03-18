package com.roganskyerik.cookly


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.facebook.CallbackManager
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.roganskyerik.cookly.permissions.PreferencesManager
import com.roganskyerik.cookly.ui.AccountScreen
import com.roganskyerik.cookly.ui.BottomNavigationBar
import com.roganskyerik.cookly.ui.CreateScreen
import com.roganskyerik.cookly.ui.DiscoverScreen
import com.roganskyerik.cookly.ui.HomeScreen
import com.roganskyerik.cookly.ui.LoginScreen
import com.roganskyerik.cookly.ui.Mode
import com.roganskyerik.cookly.ui.RegistrationScreen
import com.roganskyerik.cookly.ui.SplashScreen
import com.roganskyerik.cookly.ui.modals.ModalManager
import com.roganskyerik.cookly.ui.modals.ModalManagerViewModel
import com.roganskyerik.cookly.ui.theme.CooklyTheme
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import com.roganskyerik.cookly.utils.AppLifecycleObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val modalManagerViewModel: ModalManagerViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callbackManager = CallbackManager.Factory.create()

        val preferencesManager = PreferencesManager(this)
        val isNotificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        val isFileManagerEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        lifecycleScope.launch {
            preferencesManager.setNotificationsEnabled(isNotificationsEnabled)
            preferencesManager.setFileManagerEnabled(isFileManagerEnabled)
        }

        enableEdgeToEdge()
        setContent {
            val themeMode by mainViewModel.themeMode.collectAsState()

            CooklyTheme(
                darkTheme = when (themeMode) {
                    Mode.DARK -> true
                    Mode.LIGHT -> false
                    Mode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                AppNavigation(mainViewModel, modalManagerViewModel, callbackManager)
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val handled = callbackManager.onActivityResult(requestCode, resultCode, data)
        Log.d("Facebook Sign-In", "callbackManager.onActivityResult handled: $handled")
    }

}


@Composable
fun AppNavigation(viewModel: MainViewModel, modalManagerViewModel: ModalManagerViewModel, callbackManager: CallbackManager) {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf("splash") }
    val modalType by modalManagerViewModel.modalType.collectAsState()
    val colors = LocalCooklyColors.current
    val systemUiController = rememberSystemUiController()
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5
    val statusBarIconColor = !isDarkTheme

    val shouldNavigate by viewModel.forceLogout.collectAsState()
    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            viewModel.clearTokens()
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
            delay(100)
            viewModel.resetForceLogout()
        }
    }


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
        val accessToken = viewModel.getAccessToken()
        if (accessToken != null) {
            viewModel.startWebSocket(accessToken)
        }
    }

    LaunchedEffect(Unit) {
        val refreshToken = viewModel.getRefreshToken()
        delay(1500)
        startDestination = when {
            refreshToken != null -> {
                val newAccessToken = viewModel.refreshToken()
                if (newAccessToken != null) {
                    viewModel.saveAccessToken(newAccessToken)
                    "home"
                } else {
                    "login"
                }
            }
            else -> "login"
        }
        if (startDestination == "login") {
            viewModel.clearTokens()
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
                modifier = Modifier
                    .padding(paddingValues)
                    .background(colors.Background)
            ) {
                composable("splash") { SplashScreen() }
                composable("login") { LoginScreen(navController, viewModel, callbackManager) }
                composable("register") { RegistrationScreen(navController, viewModel, callbackManager) }
                composable("home") { HomeScreen(navController) }
                composable("discover") { DiscoverScreen(navController) }
                composable("create") { CreateScreen(navController, modalManagerViewModel::showModal, viewModel) }
                composable("account") { AccountScreen(navController, modalManagerViewModel::showModal, callbackManager) }
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
