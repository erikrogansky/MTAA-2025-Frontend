package com.roganskyerik.cookly


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
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
import androidx.compose.ui.platform.LocalContext
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
import com.roganskyerik.cookly.network.isOnline
import com.roganskyerik.cookly.network.observeNetworkStatus
import com.roganskyerik.cookly.permissions.PreferencesManager
import com.roganskyerik.cookly.ui.AccountScreen
import com.roganskyerik.cookly.ui.BottomNavigationBar
import com.roganskyerik.cookly.ui.CreateScreen
import com.roganskyerik.cookly.ui.DiscoverScreen
import com.roganskyerik.cookly.ui.HomeScreen
import com.roganskyerik.cookly.ui.LoginScreen
import com.roganskyerik.cookly.ui.Mode
import com.roganskyerik.cookly.ui.RecipeScreen
import com.roganskyerik.cookly.ui.RegistrationScreen
import com.roganskyerik.cookly.ui.SplashScreen
import com.roganskyerik.cookly.ui.modals.ModalManager
import com.roganskyerik.cookly.ui.modals.ModalManagerViewModel
import com.roganskyerik.cookly.ui.theme.CooklyTheme
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import com.roganskyerik.cookly.utils.AppLifecycleObserver
import com.roganskyerik.cookly.utils.LocalRecipeManager
import com.roganskyerik.cookly.utils.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    lateinit var tts: TextToSpeech
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

        val isCameraPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED


        lifecycleScope.launch {
            preferencesManager.setNotificationsEnabled(isNotificationsEnabled)
            preferencesManager.setCameraEnabled(isCameraPermissionGranted)
            preferencesManager.setLocationEnabled(isLocationPermissionGranted)
        }

        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale.US // or Locale.US
            }
        }

        enableEdgeToEdge()
        setContent {
            val themeMode by mainViewModel.themeMode.collectAsState()

            Log.d("Theme Mode", "Current theme mode: $themeMode")

            val isDarkTheme = when (themeMode) {
                Mode.DARK -> true
                Mode.LIGHT -> false
                Mode.SYSTEM -> isSystemInDarkTheme()
            }

            CooklyTheme(
                darkTheme = when (themeMode) {
                    Mode.DARK -> true
                    Mode.LIGHT -> false
                    Mode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                AppNavigation(mainViewModel, modalManagerViewModel, callbackManager, isDarkTheme)
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val handled = callbackManager.onActivityResult(requestCode, resultCode, data)
        Log.d("Facebook Sign-In", "callbackManager.onActivityResult handled: $handled")
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}


@Composable
fun AppNavigation(viewModel: MainViewModel, modalManagerViewModel: ModalManagerViewModel, callbackManager: CallbackManager, isDarkTheme: Boolean) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val isOnline by observeNetworkStatus(context).collectAsState(initial = isOnline(context))
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

    LaunchedEffect(isOnline) {
        val accessToken = viewModel.getAccessToken()
        if (accessToken != null) {
            viewModel.startWebSocket(accessToken)
        }
    }

    LaunchedEffect(isOnline) {
        if (!isOnline && viewModel.getRefreshToken() != null) {
            startDestination = "home_offline"
        } else {
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
                LocalRecipeManager.deleteAllRecipes(context)
            } else {
            }
        }
        navController.navigate(startDestination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    val bottomNavScreens = listOf("home", "discover", "create/{id}", "create", "account", "recipe_detail/{recipeId}")

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
                composable("splash") { SplashScreen(isDarkTheme) }
                composable("login") {
                    LaunchedEffect(Unit) {
                        viewModel.resetTheme()
                    }
                    LoginScreen(navController, viewModel, callbackManager)
                }
                composable("register") {
                    LaunchedEffect(Unit) {
                        viewModel.resetTheme()
                    }
                    RegistrationScreen(navController, viewModel, callbackManager)
                }
                composable("home") { HomeScreen(navController, false, viewModel, modalManagerViewModel::showModal) }
                composable("discover") { DiscoverScreen(navController, viewModel, modalManagerViewModel::showModal) }
                composable("create/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")
                    CreateScreen(navController, modalManagerViewModel::showModal, viewModel, id.toString())
                }
                composable("create") { CreateScreen(navController, modalManagerViewModel::showModal, viewModel) }
                composable("account") { AccountScreen(navController, modalManagerViewModel::showModal, callbackManager) }
                composable("recipe_detail/{recipeId}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId")
                    recipeId?.let {
                        RecipeScreen(recipeId = it, navController, modalManagerViewModel::showModal, viewModel, isOffline = false)
                    }
                }
                composable("offline_recipe_detail/{recipeId}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId")
                    recipeId?.let {
                        RecipeScreen(recipeId = it, navController, modalManagerViewModel::showModal, viewModel, isOffline = true)
                    }
                }
                composable("home_offline") {
                    HomeScreen(navController = navController, isOffline = true, viewModel = viewModel, modalManagerViewModel::showModal)
                }
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
