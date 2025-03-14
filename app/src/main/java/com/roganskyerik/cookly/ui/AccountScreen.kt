package com.roganskyerik.cookly.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.roganskyerik.cookly.MainViewModel
import com.roganskyerik.cookly.R
import com.roganskyerik.cookly.ui.components.RotatingLoader
import com.roganskyerik.cookly.ui.modals.ModalType
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import com.roganskyerik.cookly.ui.theme.Nunito

enum class Mode(val value: String, val displayName: String) {
    LIGHT("n", "Light Mode"),
    DARK("y", "Dark Mode"),
    SYSTEM("s", "System Preferences");

    companion object {
        fun fromValue(value: String): Mode {
            return entries.firstOrNull { it.value == value } ?: SYSTEM
        }
    }
}


@Composable
fun AccountScreen(navController: NavController, showModal: (ModalType) -> Unit, callbackManager: CallbackManager, viewModel: MainViewModel = hiltViewModel()) {
    val context = navController.context
    val colors = LocalCooklyColors.current

    val userData by viewModel.userData.collectAsState()
    val isLoading = userData?.name.isNullOrEmpty()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mode by remember { mutableStateOf(Mode.SYSTEM) }

    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val isCameraEnabled by viewModel.isCameraEnabled.collectAsState()
    //val isFileManagerEnabled by viewModel.isFileManagerEnabled.collectAsState()
    val isLocationEnabled by viewModel.isLocationEnabled.collectAsState()

    LaunchedEffect(userData) {
        mode = Mode.fromValue(userData?.darkMode ?: "s")
    }

    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background),
        contentAlignment = Alignment.Center
    ){
        if (isLoading) {
            RotatingLoader()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(colors.Background),
                verticalArrangement = Arrangement.Top,
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.user_icon_placeholder),
                        contentDescription = "User Icon",
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.CenterVertically)
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = userData?.name ?: "Anonymous User",
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.weight(1f))

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
                                    popUpTo("splash") { inclusive = true }
                                }
                                viewModel.clearTokens()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterVertically),
                        //.shadow(elevation = 1.dp, shape = RoundedCornerShape(50.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.Background,
                            contentColor = colors.FontColor
                        ),
                        border = BorderStroke(1.dp, colors.FontColor),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logout_icon),
                            contentDescription = "Logout Icon",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Log out",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Column()
                {
                    Text(
                        text = "Account",
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    Row {
                        Text(
                            text = "Name",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        Row (modifier = Modifier.clickable {
                            showModal(
                                ModalType.Custom { onDismiss ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Enter your name",
                                            style = TextStyle(
                                                fontFamily = Nunito,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 22.sp,
                                                textAlign = TextAlign.Center
                                            ),
                                            color = colors.FontColor
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        var name by remember { mutableStateOf(userData?.name ?: "") }

                                        CustomOutlinedTextField(
                                            value = name,
                                            onValueChange = { name = it },
                                            label = "Name",
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = TextStyle(
                                                fontFamily = Nunito,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row {
                                            Button(
                                                onClick = onDismiss,
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colors.Background,
                                                    contentColor = colors.FontColor
                                                ),
                                                border = BorderStroke(1.dp, colors.FontColor),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                            ) {
                                                Text(
                                                    text = "Cancel",
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp
                                                    ),
                                                    modifier = Modifier.align(Alignment.CenterVertically)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Button(
                                                onClick = {
                                                    viewModel.updateUser(name = name)
                                                    onDismiss()
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colors.Orange100,
                                                    contentColor = Color.White
                                                ),
                                                border = BorderStroke(1.dp, colors.Orange100),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                            ) {
                                                Text(
                                                    text = "Save",
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp
                                                    ),
                                                    modifier = Modifier.align(Alignment.CenterVertically)
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }) {
                            Text(
                                text = userData?.name ?: "Anonymous User",
                                style = TextStyle(
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )

                        Spacer(Modifier.width(6.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.edit_icon),
                            contentDescription = "Edit Icon",
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row {
                        Text(
                            text = "Profile photo",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        Text (
                            text = "Change photo",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textDecoration = TextDecoration.Underline,
                            )
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Row {
                        Text(
                            text = "Google account",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        val oneTapClient: SignInClient = Identity.getSignInClient(context)
                        val signInRequest = BeginSignInRequest.builder()
                            .setGoogleIdTokenRequestOptions(
                                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                    .setSupported(true)
                                    .setServerClientId(context.getString(R.string.default_web_client_id))
                                    .setFilterByAuthorizedAccounts(false)
                                    .build()
                            )
                            .build()

                        val signInLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.StartIntentSenderForResult()) { result ->
                            if (result.resultCode == Activity.RESULT_OK) {
                                val credential = result.data?.let { oneTapClient.getSignInCredentialFromIntent(it) }
                                val googleToken = credential?.googleIdToken

                                if (googleToken == null) {
                                    Log.e("Google Sign-In", "Google ID Token is null")
                                    return@rememberLauncherForActivityResult
                                }

                                val firebaseCredential = GoogleAuthProvider.getCredential(googleToken, null)
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                if (currentUser == null) {
                                    FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                                        .addOnCompleteListener { authTask ->
                                            if (authTask.isSuccessful) {
                                                FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                                                    if (tokenTask.isSuccessful) {
                                                        val firebaseToken = tokenTask.result
                                                        val firebaseIdToken = authTask.result?.user?.getIdToken(false)

                                                        firebaseIdToken?.addOnSuccessListener { result ->
                                                            val idToken = result.token
                                                            Log.d("Google Sign-In", "Firebase ID Token: $idToken")
                                                            Log.d("Google Sign-In", "Firebase Notification Token: $firebaseToken")

                                                            viewModel.loginWithGoogle(idToken.toString(), firebaseToken, "google") { response, error ->
                                                                if (response != null) {
                                                                    viewModel.saveTokens(response.accessToken, response.refreshToken)
                                                                    viewModel.startWebSocket(response.accessToken)
                                                                } else {
                                                                    Log.e("Google Sign-In", "Google sign-in failed: $error")
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                val exception = authTask.exception
                                                if (exception is FirebaseAuthUserCollisionException) {
                                                    Toast.makeText(context, "This email is already linked to another account. Please log in first.", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "Authentication failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    return@rememberLauncherForActivityResult
                                }
                                currentUser.linkWithCredential(firebaseCredential)
                                    .addOnCompleteListener { authTask ->
                                        if (authTask.isSuccessful) {
                                            FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                                                if (tokenTask.isSuccessful) {
                                                    val firebaseToken = tokenTask.result
                                                    val firebaseIdToken = authTask.result?.user?.getIdToken(false)

                                                    firebaseIdToken?.addOnSuccessListener { result ->
                                                        val idToken = result.token
                                                        Log.d("Google Sign-In", "Firebase ID Token: $idToken")
                                                        Log.d("Google Sign-In", "Firebase Notification Token: $firebaseToken")

                                                        viewModel.loginWithGoogle(idToken.toString(), firebaseToken, "google") { response, error ->
                                                            if (response != null) {
                                                                viewModel.saveTokens(response.accessToken, response.refreshToken)
                                                                viewModel.startWebSocket(response.accessToken)
                                                            } else {
                                                                Log.e("Google Sign-In", "Google sign-in failed: $error")
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            val exception = authTask.exception
                                            if (exception is FirebaseAuthUserCollisionException) {
                                                Toast.makeText(context, "This email is already linked to another account. Please log in first.", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Authentication failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                            } else {
                                Log.e("Google Sign-In", "Google sign-in cancelled")
                            }
                        }

                        Text (
                            text = if (userData?.hasGoogleAuth == true) "Linked" else "Link account",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textDecoration = if (userData?.hasGoogleAuth == true) TextDecoration.None else TextDecoration.Underline,
                            ),
                            color = if (userData?.hasGoogleAuth == true) colors.Positive else colors.FontColor,
                            modifier = if (userData?.hasGoogleAuth == true) Modifier else Modifier.clickable {
                                oneTapClient.beginSignIn(signInRequest)
                                    .addOnSuccessListener { result ->
                                        signInLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Google Sign-In", "One Tap failed, user might need to manually select an account", e)
                                    }
                            }
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    val auth = FirebaseAuth.getInstance()

                    val loginCallback = object : FacebookCallback<LoginResult> {
                        override fun onSuccess(loginResult: LoginResult) {
                            val accessToken = loginResult.accessToken
                            val credential = FacebookAuthProvider.getCredential(accessToken.token)

                            val currentUser = auth.currentUser
                            if (currentUser == null) {
                                auth.signInWithCredential(credential)
                                    .addOnCompleteListener { authTask ->
                                        if (authTask.isSuccessful) {
                                            val firebaseUser = auth.currentUser
                                            firebaseUser?.getIdToken(false)?.addOnSuccessListener { result ->
                                                val idToken = result.token
                                                FirebaseMessaging.getInstance().token.addOnSuccessListener { firebaseToken ->
                                                    Log.d("Facebook Sign-In", "Firebase ID Token: $idToken")
                                                    Log.d("Facebook Sign-In", "Firebase Notification Token: $firebaseToken")

                                                    viewModel.loginWithGoogle(idToken.toString(), firebaseToken, "facebook") { response, error ->
                                                        if (response != null) {
                                                            viewModel.saveTokens(response.accessToken, response.refreshToken)
                                                            viewModel.startWebSocket(response.accessToken)
                                                        } else {
                                                            Log.e("Facebook Sign-In", "Facebook sign-in failed: $error")
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            val exception = authTask.exception
                                            if (exception is FirebaseAuthUserCollisionException) {
                                                Toast.makeText(context, "This email is already linked to another account. Please log in first.", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Authentication failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                return
                            }
                            currentUser.linkWithCredential(credential)
                                .addOnCompleteListener { authTask ->
                                    if (authTask.isSuccessful) {
                                        val firebaseUser = auth.currentUser
                                        firebaseUser?.getIdToken(false)?.addOnSuccessListener { result ->
                                            val idToken = result.token
                                            FirebaseMessaging.getInstance().token.addOnSuccessListener { firebaseToken ->
                                                Log.d("Facebook Sign-In", "Firebase ID Token: $idToken")
                                                Log.d("Facebook Sign-In", "Firebase Notification Token: $firebaseToken")

                                                viewModel.loginWithGoogle(idToken.toString(), firebaseToken, "facebook") { response, error ->
                                                    if (response != null) {
                                                        viewModel.saveTokens(response.accessToken, response.refreshToken)
                                                        viewModel.startWebSocket(response.accessToken)
                                                    } else {
                                                        Log.e("Facebook Sign-In", "Facebook sign-in failed: $error")
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        val exception = authTask.exception
                                        if (exception is FirebaseAuthUserCollisionException) {
                                            Toast.makeText(context, "This email is already linked to another account. Please log in first.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Authentication failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                        }

                        override fun onCancel() {
                            Log.d("Facebook Sign-In", "Login canceled")
                        }

                        override fun onError(error: FacebookException) {
                            Log.e("Facebook Sign-In", "Login error: ${error.message}")
                        }
                    }

                    Row {
                        Text(
                            text = "Facebook account",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        Text (
                            text = if (userData?.hasFacebookAuth == true) "Linked" else "Link account",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textDecoration = if (userData?.hasFacebookAuth == true) TextDecoration.None else TextDecoration.Underline,
                            ),
                            color = if (userData?.hasFacebookAuth == true) colors.Positive else colors.FontColor,
                            modifier = if (userData?.hasFacebookAuth == true) Modifier else Modifier.clickable {
                                val loginManager = LoginManager.getInstance()
                                loginManager.logInWithReadPermissions(
                                    context as ComponentActivity,
                                    listOf("email", "public_profile")
                                )
                                loginManager.registerCallback(callbackManager, loginCallback)
                            }
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Row {
                        Text(
                            text = "Password",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        Text (
                            text = if (userData?.hasPassword == true) "Change password" else "Set password",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textDecoration = TextDecoration.Underline,
                            ),
                            modifier = Modifier.clickable {
                                showModal (
                                    ModalType.Custom { onDismiss ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Enter your new password",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 22.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                color = colors.FontColor
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            var currentPassword by remember { mutableStateOf("") }
                                            var newPassword by remember { mutableStateOf("") }
                                            var confirmPassword by remember { mutableStateOf("") }

                                            if (userData?.hasPassword == true) {

                                                CustomOutlinedTextField(
                                                    value = currentPassword,
                                                    onValueChange = {
                                                        currentPassword = it
                                                        errorMessage = null
                                                    },
                                                    label = "Current Password",
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textStyle = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    ),
                                                    isError = errorMessage != null,
                                                    singleLine = true,
                                                    visualTransformation = PasswordVisualTransformation(),
                                                )

                                                Spacer(modifier = Modifier.height(16.dp))
                                            }

                                            CustomOutlinedTextField(
                                                value = newPassword,
                                                onValueChange = {
                                                    newPassword = it
                                                    errorMessage = null
                                                },
                                                label = "Password",
                                                modifier = Modifier.fillMaxWidth(),
                                                textStyle = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                isError = errorMessage != null,
                                                singleLine = true,
                                                visualTransformation = PasswordVisualTransformation()
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            CustomOutlinedTextField(
                                                value = confirmPassword,
                                                onValueChange = {
                                                    confirmPassword = it
                                                    errorMessage = null
                                                },
                                                label = "Confirm Password",
                                                modifier = Modifier.fillMaxWidth(),
                                                textStyle = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                isError = errorMessage != null,
                                                singleLine = true,
                                                visualTransformation = PasswordVisualTransformation()
                                            )

                                            if (errorMessage != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = errorMessage!!,
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = colors.Error
                                                    ),
                                                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Row {
                                                Button(
                                                    onClick = onDismiss,
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colors.Background,
                                                        contentColor = colors.FontColor
                                                    ),
                                                    border = BorderStroke(1.dp, colors.FontColor),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                                ) {
                                                    Text(
                                                        text = "Cancel",
                                                        style = TextStyle(
                                                            fontFamily = Nunito,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 16.sp
                                                        ),
                                                        modifier = Modifier.align(Alignment.CenterVertically)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(16.dp))

                                                Button(
                                                    onClick = {
                                                        if (userData?.hasPassword == true) {
                                                            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                                                                errorMessage =
                                                                    "All fields are required"
                                                                return@Button
                                                            }
                                                            if (newPassword == currentPassword) {
                                                                errorMessage = "New password must be different from current password"
                                                                return@Button
                                                            }
                                                        } else {
                                                            currentPassword = ""
                                                            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                                                                errorMessage =
                                                                    "All fields are required"
                                                                return@Button
                                                            }
                                                        }
                                                        if (newPassword != confirmPassword) {
                                                            errorMessage = "Passwords do not match"
                                                            return@Button
                                                        }
                                                        if (newPassword.length < 6) {
                                                            errorMessage = "Password must be at least 8 characters"
                                                            return@Button
                                                        }
                                                        if (newPassword.contains(" ")) {
                                                            errorMessage = "Password cannot contain spaces"
                                                            return@Button
                                                        }
                                                        viewModel.changePassword(currentPassword, newPassword) { response, error ->
                                                            if (response != null) {
                                                                onDismiss()
                                                                if (userData?.hasPassword == true) {
                                                                    Toast.makeText(context, "Password changed successfully", Toast.LENGTH_LONG).show()
                                                                } else {
                                                                    viewModel.setPassword()
                                                                    Toast.makeText(context, "Password set successfully", Toast.LENGTH_LONG).show()
                                                                }
                                                            } else {
                                                                errorMessage = error
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colors.Orange100,
                                                        contentColor = Color.White
                                                    ),
                                                    border = BorderStroke(1.dp, colors.Orange100),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                                ) {
                                                    Text(
                                                        text = "Save",
                                                        style = TextStyle(
                                                            fontFamily = Nunito,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 16.sp
                                                        ),
                                                        modifier = Modifier.align(Alignment.CenterVertically)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                val tempContext = LocalContext.current
                val activity = tempContext as? Activity

                Column()
                {
                    Text(
                        text = "Preferences",
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    Row {
                        Text(
                            text = "Camera",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        CustomSwitch(
                            isChecked = isCameraEnabled,
                            onCheckedChange = { viewModel.toggleCamera(it) },
                            onColor = colors.Orange100,
                            onBorderColor = colors.Orange100,
                            offColor = colors.Background,
                            offBorderColor = colors.FontColor,
                            offBorderOpacity = 0.5f,
                            offThumbColor = colors.FontColor,
                            offThumbOpacity = 0.2f,
                            onThumbColor = Color.White,
                            onThumbOpacity = 1f,
                        )
                    }

                    Spacer(Modifier.height(14.dp))

//                    fun requestFilePermission(activity: Activity) {
//                        when {
//                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
//                                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
//                                    val intent = Intent(MediaStore.ACTION_PICK_IMAGES).apply {
//                                        putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 10)
//                                    }
//                                    activity.startActivityForResult(intent, 1003)
//                                } else {
//                                    ActivityCompat.requestPermissions(
//                                        activity,
//                                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
//                                        1002
//                                    )
//                                }
//                            }
//
//                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> { // Android 13
//                                when {
//                                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED -> {
//                                        // Permission already granted
//                                    }
//                                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_MEDIA_IMAGES) -> {
//                                        ActivityCompat.requestPermissions(
//                                            activity,
//                                            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
//                                            1002
//                                        )
//                                    }
//                                    else -> {
//                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//                                            data = Uri.fromParts("package", activity.packageName, null)
//                                        }
//                                        activity.startActivity(intent)
//                                    }
//                                }
//                            }
//
//                            else -> {
//                                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                                    arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
//                                } else {
//                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
//                                }
//
//                                when {
//                                    permissions.all { ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED } -> {
//                                        // Permission already granted
//                                    }
//                                    permissions.any { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) } -> {
//                                        ActivityCompat.requestPermissions(activity, permissions, 1002)
//                                    }
//                                    else -> {
//                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//                                            data = Uri.fromParts("package", activity.packageName, null)
//                                        }
//                                        activity.startActivity(intent)
//                                    }
//                                }
//                            }
//                        }
//                    }



//                    Row {
//                        Text(
//                            text = "File manager",
//                            style = TextStyle(
//                                fontFamily = Nunito,
//                                fontWeight = FontWeight.Bold,
//                                fontSize = 16.sp
//                            )
//                        )
//
//                        Spacer(Modifier.weight(1f))
//
//                        CustomSwitch(
//                            isChecked = isFileManagerEnabled,
//                            onCheckedChange = { isChecked ->
////                                if (isChecked) {
////                                    activity?.let { requestFilePermission(it) }
////                                    viewModel.toggleFileManager(isChecked)
////                                } else {
////                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
////                                        data = Uri.parse("package:${context.packageName}")
////                                    }
////                                    context.startActivity(intent)
////                                }
//                                viewModel.toggleFileManager(isChecked)
//                            },
//                            onColor = colors.Orange100,
//                            onBorderColor = colors.Orange100,
//                            offColor = colors.Background,
//                            offBorderColor = colors.FontColor,
//                            offBorderOpacity = 0.5f,
//                            offThumbColor = colors.FontColor,
//                            offThumbOpacity = 0.2f,
//                            onThumbColor = Color.White,
//                            onThumbOpacity = 1f,
//                            onText = "Manage"
//                        )
//                    }
//
//                    Spacer(Modifier.height(14.dp))

                    Row {
                        Text(
                            text = "Notifications",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        fun requestNotificationPermission(activity: Activity) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                when {
                                    ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                                        // Permission already granted
                                    }
                                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS) -> {
                                        ActivityCompat.requestPermissions(
                                            activity,
                                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                            1001
                                        )
                                    }
                                    else -> {
                                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        }



                        CustomSwitch(
                            isChecked = isNotificationsEnabled,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                            activity?.let { requestNotificationPermission(it) }
                                        }
                                    }
                                    viewModel.toggleNotifications(isChecked)
                                } else {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            onColor = colors.Orange100,
                            onBorderColor = colors.Orange100,
                            offColor = colors.Background,
                            offBorderColor = colors.FontColor,
                            offBorderOpacity = 0.5f,
                            offThumbColor = colors.FontColor,
                            offThumbOpacity = 0.2f,
                            onThumbColor = Color.White,
                            onThumbOpacity = 1f,
                            onText = "Manage"
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Row {
                        Text(
                            text = "Location",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        CustomSwitch(
                            isChecked = isLocationEnabled,
                            onCheckedChange = { viewModel.toggleLocation(it) },
                            onColor = colors.Orange100,
                            onBorderColor = colors.Orange100,
                            offColor = colors.Background,
                            offBorderColor = colors.FontColor,
                            offBorderOpacity = 0.5f,
                            offThumbColor = colors.FontColor,
                            offThumbOpacity = 0.2f,
                            onThumbColor = Color.White,
                            onThumbOpacity = 1f,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                Column()
                {
                    Text(
                        text = "Visualization",
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    Row {
                        Text(
                            text = "Dark mode",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        Text (
                            text = mode.displayName,
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.clickable {
                                showModal(
                                    ModalType.Custom { onDismiss ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Select mode",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 22.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                color = colors.FontColor
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Button(
                                                onClick = {
                                                    viewModel.updateUser(mode = Mode.LIGHT)
                                                    onDismiss()
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colors.ModalBackground,
                                                    contentColor = colors.FontColor
                                                ),
                                                border = BorderStroke(1.dp, colors.FontColor),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                            ) {
                                                Text(
                                                    text = Mode.LIGHT.displayName,
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp
                                                    ),
                                                    modifier = Modifier.align(Alignment.CenterVertically)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = {
                                                    viewModel.updateUser(mode = Mode.DARK)
                                                    onDismiss()
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colors.ModalBackground,
                                                    contentColor = colors.FontColor
                                                ),
                                                border = BorderStroke(1.dp, colors.FontColor),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                            ) {
                                                Text(
                                                    text = Mode.DARK.displayName,
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp
                                                    ),
                                                    modifier = Modifier.align(Alignment.CenterVertically)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = {
                                                    viewModel.updateUser(mode = Mode.SYSTEM)
                                                    onDismiss()
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colors.ModalBackground,
                                                    contentColor = colors.FontColor
                                                ),
                                                border = BorderStroke(1.dp, colors.FontColor),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                            ) {
                                                Text(
                                                    text = Mode.SYSTEM.displayName,
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp
                                                    ),
                                                    modifier = Modifier.align(Alignment.CenterVertically)
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        )

                        Spacer(Modifier.width(6.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.arrow_bottom_icon),
                            contentDescription = "Edit Icon",
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Column()
                {
                    Text(
                        text = "Danger zone",
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    Row {
                        Text(
                            text = "Sign out of all devices",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        Text(
                            text = "Sign out everywhere",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textDecoration = TextDecoration.Underline,
                                color = colors.Error
                            ),
                            modifier = Modifier.clickable {
                                showModal(
                                    ModalType.Custom { onDismiss ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Are you sure?",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 22.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                color = colors.FontColor
                                            )

                                            Spacer(Modifier.height(14.dp))

                                            Text(
                                                text = "You will be logged out of all devices.",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 16.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                color = colors.FontColor
                                            )

                                            Spacer(Modifier.height(24.dp))

                                            Row(Modifier.fillMaxWidth()){
                                                Button(
                                                    onClick = { onDismiss() },
                                                    modifier = Modifier
                                                        .align(Alignment.CenterVertically)
                                                        .weight(1f),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colors.ModalBackground,
                                                        contentColor = colors.FontColor
                                                    ),
                                                    border = BorderStroke(1.dp, colors.FontColor),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                                ) {
                                                    Text(
                                                        text = "Cancel",
                                                        style = TextStyle(
                                                            fontFamily = Nunito,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 16.sp
                                                        ),
                                                        modifier = Modifier.align(Alignment.CenterVertically)
                                                    )
                                                }

                                                Spacer(Modifier.width(10.dp))

                                                Button(
                                                    onClick = {
                                                        val refreshToken = viewModel.getRefreshToken() ?: "no_token"

                                                        viewModel.logoutAll(refreshToken) { response, error ->
                                                            if (response != null) {
                                                                // Do nothing
                                                            } else {
                                                                errorMessage = error
                                                            }

                                                            navController.navigate("login") {
                                                                popUpTo("splash") { inclusive = true }
                                                            }
                                                            viewModel.clearTokens()

                                                            onDismiss()
                                                        }
                                                    },

                                                    modifier = Modifier
                                                        .align(Alignment.CenterVertically)
                                                        .weight(1f),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colors.Orange100,
                                                        contentColor = Color.White
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                                ) {
                                                    Text(
                                                        text = "Confirm",
                                                        style = TextStyle(
                                                            fontFamily = Nunito,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 16.sp
                                                        ),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                        )

                    }

                    Spacer(Modifier.height(14.dp))

                    Row {
                        Text(
                            text = "Account deletion",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        Text (
                            text = "Delete account",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textDecoration = TextDecoration.Underline,
                                color = colors.Error
                            ),
                            modifier = Modifier.clickable {
                                showModal(
                                    ModalType.Custom { onDismiss ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Are you sure?",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 22.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                color = colors.FontColor
                                            )

                                            Spacer(Modifier.height(14.dp))

                                            Text(
                                                text = "This action cannot be undone.",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 16.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                color = colors.FontColor
                                            )

                                            Spacer(Modifier.height(24.dp))

                                            Row(Modifier.fillMaxWidth()) {
                                                Button(
                                                    onClick = { onDismiss() },
                                                    modifier = Modifier
                                                        .align(Alignment.CenterVertically)
                                                        .weight(1f),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colors.ModalBackground,
                                                        contentColor = colors.FontColor
                                                    ),
                                                    border = BorderStroke(1.dp, colors.FontColor),
                                                    contentPadding = PaddingValues(
                                                        horizontal = 12.dp,
                                                        vertical = 12.dp
                                                    ),
                                                ) {
                                                    Text(
                                                        text = "Cancel",
                                                        style = TextStyle(
                                                            fontFamily = Nunito,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 16.sp
                                                        ),
                                                        modifier = Modifier.align(Alignment.CenterVertically)
                                                    )
                                                }

                                                Spacer(Modifier.width(10.dp))

                                                Button(
                                                    onClick = {
                                                        viewModel.deleteAccount { response, error ->
                                                            if (response != null) {
                                                                onDismiss()
                                                                navController.navigate("login") {
                                                                    popUpTo("splash") {
                                                                        inclusive = true
                                                                    }
                                                                }
                                                                viewModel.clearTokens()
                                                            } else {
                                                                onDismiss()
                                                                Toast.makeText(context, "Account deletion failed: $error", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                    },

                                                    modifier = Modifier
                                                        .align(Alignment.CenterVertically)
                                                        .weight(1f),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colors.Error,
                                                        contentColor = Color.White
                                                    ),
                                                    contentPadding = PaddingValues(
                                                        horizontal = 12.dp,
                                                        vertical = 12.dp
                                                    ),
                                                ) {
                                                    Text(
                                                        text = "Confirm",
                                                        style = TextStyle(
                                                            fontFamily = Nunito,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 16.sp
                                                        ),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun CustomSwitch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 48.dp,
    height: Dp = 29.dp,
    onColor: Color = Color.Yellow,
    offColor: Color = Color.Gray,
    onOpacity: Float = 1f,
    offOpacity: Float = 1f,
    onThumbColor: Color = Color.White,
    offThumbColor: Color = Color.LightGray,
    onThumbOpacity: Float = 1f,
    offThumbOpacity: Float = 1f,
    onBorderColor: Color = Color.Black,
    offBorderColor: Color = Color.DarkGray,
    onBorderOpacity: Float = 1f,
    offBorderOpacity: Float = 1f,
    borderWidth: Dp = 1.dp,
    onText: String = "On",
    offText: String = "Off"
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = if (isChecked) onText else offText,
            modifier = Modifier.padding(end = 8.dp),
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(50))
                .border(
                    borderWidth,
                    if (isChecked) onBorderColor.copy(alpha = onBorderOpacity)
                    else offBorderColor.copy(alpha = offBorderOpacity),
                    RoundedCornerShape(50)
                )
                .background(
                    if (isChecked) onColor.copy(alpha = onOpacity)
                    else offColor.copy(alpha = offOpacity)
                )
                .clickable { onCheckedChange(!isChecked) }
                .padding(3.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(height - 6.dp)
                    .background(
                        if (isChecked) onThumbColor.copy(alpha = onThumbOpacity)
                        else offThumbColor.copy(alpha = offThumbOpacity),
                        shape = CircleShape
                    )
                    .align(if (isChecked) Alignment.CenterEnd else Alignment.CenterStart)
            )
        }
    }
}