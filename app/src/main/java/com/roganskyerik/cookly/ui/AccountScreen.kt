package com.roganskyerik.cookly.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.roganskyerik.cookly.MainViewModel
import com.roganskyerik.cookly.R
import com.roganskyerik.cookly.ui.modals.ModalType
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import com.roganskyerik.cookly.ui.theme.Nunito

@Composable
fun AccountScreen(navController: NavController, showModal: (ModalType) -> Unit, viewModel: MainViewModel = hiltViewModel()) {
    val colors = LocalCooklyColors.current

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isCameraEnabled by remember { mutableStateOf(false) }
    var isFileManagerEnabled by remember { mutableStateOf(false) }
    var isNotificationsEnabled by remember { mutableStateOf(false) }
    var isLocationEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).background(colors.Background),
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
                text = "Alex Smith",
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
                            popUpTo("home") { inclusive = true }
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

                Text (
                    text = "Alex Smith",
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
                    modifier = Modifier.size(16.dp).align(Alignment.CenterVertically)
                )
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
                    text = "Password",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )

                Spacer(Modifier.weight(1f))

                Text (
                    text = "Change password",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.Underline,
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

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
                    onCheckedChange = { isCameraEnabled = it },
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

            Row {
                Text(
                    text = "File manager",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )

                Spacer(Modifier.weight(1f))

                CustomSwitch(
                    isChecked = isFileManagerEnabled,
                    onCheckedChange = { isFileManagerEnabled = it },
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

                CustomSwitch(
                    isChecked = isNotificationsEnabled,
                    onCheckedChange = { isNotificationsEnabled = it },
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
                    onCheckedChange = { isLocationEnabled = it },
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
                    text = "System preferences",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )

                Spacer(Modifier.width(6.dp))

                Icon(
                    painter = painterResource(id = R.drawable.arrow_bottom_icon),
                    contentDescription = "Edit Icon",
                    modifier = Modifier.size(16.dp).align(Alignment.CenterVertically)
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
                                            modifier = Modifier.align(Alignment.CenterVertically)
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
                                                        popUpTo("account") { inclusive = true }
                                                    }
                                                    viewModel.clearTokens()

                                                    onDismiss()
                                                }
                                            },

                                            modifier = Modifier.align(Alignment.CenterVertically)
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
                    )
                )
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