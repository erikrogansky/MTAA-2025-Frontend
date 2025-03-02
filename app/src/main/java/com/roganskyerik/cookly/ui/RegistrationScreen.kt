package com.roganskyerik.cookly.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roganskyerik.cookly.R
import com.roganskyerik.cookly.ui.theme.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

import androidx.compose.ui.text.withStyle
import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


@Composable
fun RegistrationScreen(navController: NavController = rememberNavController()) {
    val colors = LocalCooklyColors.current

    var name by remember { mutableStateOf("") }
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
                painter = painterResource(id = R.drawable.logo_horizontal),
                contentDescription = "Cookly logo",
                modifier = Modifier
                    .width(222.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.FillWidth
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Let's create an account!",
                style = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp
                ),
                color = colors.FontColor
            )

            Text(
                text = "Register for free, no strings attached",
                style = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                ),
                color = colors.FontColor
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Full Name",
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
                value = name,
                onValueChange = { name = it },
                label = "John Doe",
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
                onValueChange = { email = it },
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
                onValueChange = { password = it },
                label = "•••••••••••",
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
                Text(text = errorMessage!!, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = {
                if (email == "admin" && password == "password") {
                    //onLoginSuccess()
                } else {
                    errorMessage = "Invalid credentials"
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
                    text = "Sign up",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                )
            }

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

            Button(
                onClick = {
                    //
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(50.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, // Background color
                    contentColor = colors.FontDark // Text color
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
                    text = "Sign up with Google",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    //
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(50.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, // Background color
                    contentColor = colors.FontDark // Text color
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
                    text = "Sign up with Facebook",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                )
            }

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
                            append("Already have an account? ")
                        }
                        val startIndex = length
                        withStyle(style = SpanStyle(color = colors.LinkColor, textDecoration = TextDecoration.Underline, fontSize = 16.sp)) {
                            append("Log in!")
                        }
                        addStringAnnotation(
                            tag = "Login",
                            annotation = "login",
                            start = startIndex,
                            end = length
                        )
                    },
                    modifier = Modifier.clickable {
                        navController.navigate("login")
                    }
                )
            }

        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewLightRegistration() {
    CooklyTheme(darkTheme = false) {
        RegistrationScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDarkRegistration() {
    CooklyTheme(darkTheme = true) {
        RegistrationScreen()
    }
}
