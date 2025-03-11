package com.roganskyerik.cookly.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import com.roganskyerik.cookly.ui.theme.Nunito


@Composable
fun LoginScreen(navController: NavController = rememberNavController(), viewModel: MainViewModel = hiltViewModel(), callbackManager: CallbackManager) {
    val colors = LocalCooklyColors.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                )
            )
        ) {
        Image(
            painter = painterResource(
                id = if (isSystemInDarkTheme()) R.drawable.login_background_dark
                else R.drawable.login_background_light
            ),
            contentDescription = "Login Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 36.dp, top = 40.dp, bottom = 10.dp, end = 36.dp),
        verticalArrangement = Arrangement.Top,
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo_vertical),
            contentDescription = "Cookly logo",
            modifier = Modifier
                .width(140.dp)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Hi, welcome back!",
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp
            ),
            color = colors.FontColor
        )

        Text(
            text = "Sign in to your account to continue",
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            ),
            color = colors.FontColor
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Email",
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = colors.FontColor,
            modifier = Modifier.padding(9.dp, 0.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        CustomOutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = "example@email.com",
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp),
            borderColor = colors.Orange100,
            focusedBorderColor = colors.DarkOrange,
            isError = errorMessage != null,
            errorColor = colors.Error
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Password",
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = colors.FontColor,
            modifier = Modifier.padding(9.dp, 0.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        CustomOutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = "Your password",
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp),
            borderColor = colors.Orange100,
            focusedBorderColor = colors.DarkOrange,
            isError = errorMessage != null,
            errorColor = colors.Error
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = errorMessage!!, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(9.dp, 0.dp))
        }

        Spacer(modifier = Modifier.height(26.dp))

        Button(
            onClick = {
                errorMessage = null

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseToken = task.result

                        Log.d("FCM", "Firebase token: $firebaseToken")

                        viewModel.login(email, password, firebaseToken) { response, error ->
                            if (response != null) {
                                viewModel.saveTokens(response.accessToken, response.refreshToken)
                                viewModel.startWebSocket(response.accessToken)
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                errorMessage = error
                            }
                        }
                    } else {
                        errorMessage = "Failed to fetch Firebase token"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.Orange100, // Background color
                contentColor = Color.White // Text color
            ),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Text(
                text = "Sign in",
                style = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Forgot password?",
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                textDecoration = TextDecoration.Underline,
            ),
            color = colors.LinkColor,
            modifier = Modifier.padding(9.dp, 0.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp),
                color = colors.DarkOrange
            )

            Text(
                text = "or",
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                style = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.FontColor
                )
            )

            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp),
                color = colors.DarkOrange
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        GoogleLoginButton(navController.context, viewModel, navController)

        Spacer(modifier = Modifier.height(16.dp))

        FacebookLoginButton(navController.context, viewModel, navController, callbackManager)

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = colors.FontColor, fontSize = 16.sp)) {
                        append("Don't have an account yet? ")
                    }
                    val startIndex = length
                    withStyle(style = SpanStyle(color = colors.LinkColor, textDecoration = TextDecoration.Underline, fontSize = 16.sp)) {
                        append("Register for free!")
                    }
                    addStringAnnotation(
                        tag = "Register",
                        annotation = "register",
                        start = startIndex,
                        end = length
                    )
                },
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }

    }
    }
}


@Composable
fun GoogleLoginButton(context: Context, viewModel: MainViewModel, navController: NavController) {
    val colors = LocalCooklyColors.current

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

    val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val credential = result.data?.let { oneTapClient.getSignInCredentialFromIntent(it) }
            val googleToken = credential?.googleIdToken

            if (googleToken == null) {
                Log.e("Google Sign-In", "Google ID Token is null")
                return@rememberLauncherForActivityResult
            }

            val firebaseCredential = GoogleAuthProvider.getCredential(googleToken, null)
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
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
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

    Button(
        onClick = {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    signInLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
                }
                .addOnFailureListener { e ->
                    Log.e("Google Sign-In", "One Tap failed, user might need to manually select an account", e)
                }
        },
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(50.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = colors.FontDark
        ),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.google_logo),
            contentDescription = "Google icon",
            modifier = Modifier.size(20.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Sign in with Google",
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        )
    }
}


@Composable
fun FacebookLoginButton(
    context: Context,
    viewModel: MainViewModel,
    navController: NavController,
    callbackManager: CallbackManager
) {
    val auth = FirebaseAuth.getInstance()

    val loginCallback = object : FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {
            val accessToken = loginResult.accessToken
            val credential = FacebookAuthProvider.getCredential(accessToken.token)

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
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
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

    Button(
        onClick = {
            val loginManager = LoginManager.getInstance()
            loginManager.logInWithReadPermissions(
                context as ComponentActivity,
                listOf("email", "public_profile")
            )
            loginManager.registerCallback(callbackManager, loginCallback)
        },
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(50.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = LocalCooklyColors.current.FontDark
        ),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.facebook_logo),
            contentDescription = "Facebook icon",
            modifier = Modifier.size(20.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Sign in with Facebook",
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        )
    }
}
