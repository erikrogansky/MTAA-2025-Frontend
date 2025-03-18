package com.roganskyerik.cookly.ui

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.roganskyerik.cookly.MainViewModel
import com.roganskyerik.cookly.R
import com.roganskyerik.cookly.ui.modals.ModalType
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import com.roganskyerik.cookly.ui.theme.Nunito
import kotlin.math.roundToInt

enum class Difficulty {
    EASY,
    MODERATE,
    DIFFICULT
}

data class RecipeDetails(
    val prepTime: Float? = null,
    val difficulty: Difficulty? = null,
    val servings: Int? = null,
    var calories: Int? = null
)

data class Tag(
    val name: String,
    val color: String,
    val group: String
)

data class Ingredient(
    val icon: String,
    val name: String,
    val quantity: String,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateScreen(navController: NavController, showModal: (ModalType) -> Unit, viewModel: MainViewModel = hiltViewModel()) {
    val colors = LocalCooklyColors.current

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var details by remember { mutableStateOf<RecipeDetails?>(null) }
    var tags by remember { mutableStateOf<List<Tag>>(emptyList()) }
    var ingredients by remember { mutableStateOf<List<Ingredient>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(colors.Background),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = "Add Recipe",
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            ),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Section("About") {
            if (title != "" && description != "") {
                Column(
                    modifier = Modifier.clickable {
                        showModal(
                            ModalType.Custom { onDismiss ->
                                showModal(
                                    ModalType.Custom { onDismiss ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Add title and description",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 22.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                color = colors.FontColor
                                            )

                                            Spacer(Modifier.height(24.dp))

                                            var modalTitle by remember { mutableStateOf(title) }
                                            var modalDescription by remember { mutableStateOf(description) }
                                            var modalError by remember { mutableStateOf("") }

                                            CustomOutlinedTextField(
                                                value = modalTitle,
                                                onValueChange = {
                                                    modalError = ""
                                                    modalTitle = it
                                                },
                                                label = "Title",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp),
                                                textStyle = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                singleLine = true,
                                                shape = RoundedCornerShape(8.dp),
                                                borderColor = colors.DarkOrange,
                                                focusedBorderColor = colors.Orange100,
                                                isError = modalError != "",
                                            )

                                            Spacer(Modifier.height(10.dp))

                                            CustomMultilineTextField(
                                                value = modalDescription,
                                                onValueChange = {
                                                    modalError = ""
                                                    modalDescription = it
                                                },
                                                label = "Description",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp),
                                                textStyle = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                borderColor = colors.DarkOrange,
                                                focusedBorderColor = colors.Orange100,
                                                isError = modalError != "",
                                            )

                                            Spacer(Modifier.height(4.dp))

                                            if (modalError != "") {
                                                Text(
                                                    text = modalError,
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = colors.Error
                                                    ),
                                                    modifier = Modifier
                                                        .padding(start = 4.dp)
                                                        .align(Alignment.Start),

                                                    )
                                            }

                                            Spacer(Modifier.height(24.dp))

                                            Row(Modifier.fillMaxWidth()) {
                                                Button(
                                                    onClick = { onDismiss() },
                                                    modifier = Modifier
                                                        .align(Alignment.CenterVertically)
                                                        .weight(1f),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colors.ModalBackground,
                                                        contentColor = colors.AI
                                                    ),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        colors.LightOutline
                                                    ),
                                                    contentPadding = PaddingValues(
                                                        horizontal = 12.dp,
                                                        vertical = 12.dp
                                                    ),
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.ai_icon),
                                                        contentDescription = "AI",
                                                        modifier = Modifier
                                                            .align(Alignment.CenterVertically)
                                                            .size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Suggest",
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
                                                        if (modalTitle.isEmpty() || modalDescription.isEmpty()) {
                                                            modalError =
                                                                "Please fill out all fields"
                                                        } else {
                                                            title = modalTitle
                                                            description = modalDescription
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
                                                    contentPadding = PaddingValues(
                                                        horizontal = 12.dp,
                                                        vertical = 12.dp
                                                    ),
                                                ) {
                                                    Text(
                                                        text = "Save details",
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
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = colors.FontColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = description,
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                        ),
                        color = colors.FontColor
                    )
                }
            } else {
                Row {
                    Button(
                        onClick = {
                            showModal(
                                ModalType.Custom { onDismiss ->
                                    showModal(
                                        ModalType.Custom { onDismiss ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "Add title and description",
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 22.sp,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    color = colors.FontColor
                                                )

                                                Spacer(Modifier.height(24.dp))

                                                var modalTitle by remember { mutableStateOf("") }
                                                var modalDescription by remember { mutableStateOf("") }
                                                var modalError by remember { mutableStateOf("") }

                                                CustomOutlinedTextField(
                                                    value = modalTitle,
                                                    onValueChange = {
                                                        modalError = ""
                                                        modalTitle = it
                                                    },
                                                    label = "Title",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(50.dp),
                                                    textStyle = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    ),
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(8.dp),
                                                    borderColor = colors.DarkOrange,
                                                    focusedBorderColor = colors.Orange100,
                                                    isError = modalError != "",
                                                )

                                                Spacer(Modifier.height(10.dp))

                                                CustomMultilineTextField(
                                                    value = modalDescription,
                                                    onValueChange = {
                                                        modalError = ""
                                                        modalDescription = it
                                                    },
                                                    label = "Description",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(100.dp),
                                                    textStyle = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    ),
                                                    borderColor = colors.DarkOrange,
                                                    focusedBorderColor = colors.Orange100,
                                                    isError = modalError != "",
                                                )

                                                Spacer(Modifier.height(4.dp))

                                                if (modalError != "") {
                                                    Text(
                                                        text = modalError,
                                                        style = TextStyle(
                                                            fontFamily = Nunito,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = colors.Error
                                                        ),
                                                        modifier = Modifier
                                                            .padding(start = 4.dp)
                                                            .align(Alignment.Start),

                                                        )
                                                }

                                                Spacer(Modifier.height(24.dp))

                                                Row(Modifier.fillMaxWidth()) {
                                                    Button(
                                                        onClick = { onDismiss() },
                                                        modifier = Modifier
                                                            .align(Alignment.CenterVertically)
                                                            .weight(1f),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = colors.ModalBackground,
                                                            contentColor = colors.AI
                                                        ),
                                                        border = BorderStroke(
                                                            1.dp,
                                                            colors.LightOutline
                                                        ),
                                                        contentPadding = PaddingValues(
                                                            horizontal = 12.dp,
                                                            vertical = 12.dp
                                                        ),
                                                    ) {
                                                        Image(
                                                            painter = painterResource(id = R.drawable.ai_icon),
                                                            contentDescription = "AI",
                                                            modifier = Modifier
                                                                .align(Alignment.CenterVertically)
                                                                .size(24.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Suggest",
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
                                                            if (modalTitle.isEmpty() || modalDescription.isEmpty()) {
                                                                modalError =
                                                                    "Please fill out all fields"
                                                            } else {
                                                                title = modalTitle
                                                                description = modalDescription
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
                                                        contentPadding = PaddingValues(
                                                            horizontal = 12.dp,
                                                            vertical = 12.dp
                                                        ),
                                                    ) {
                                                        Text(
                                                            text = "Save details",
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
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.Background,
                            contentColor = colors.FontColor
                        ),
                        border = BorderStroke(1.dp, colors.LightOutline),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.plus_icon),
                            contentDescription = "Add",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add about",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.Background,
                            contentColor = colors.AI
                        ),
                        border = BorderStroke(1.dp, colors.LightOutline),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ai_icon),
                            contentDescription = "Add",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Suggest",
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

        Spacer(modifier = Modifier.height(24.dp))

        Section(
            title = "Additional Details"
        ) {
            if (details != null) {
                Row (
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).clickable{
                        showModal(
                            ModalType.Custom { onDismiss ->
                                showModal(
                                    ModalType.Custom { onDismiss ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Add details",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 22.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                color = colors.FontColor
                                            )

                                            Spacer(Modifier.height(24.dp))

                                            var modalError by remember { mutableStateOf("") }
                                            var modalHours by remember(details) { mutableStateOf(details?.prepTime?.toInt().toString()) }
                                            var modalMinutes by remember(details) {
                                                mutableStateOf(
                                                    details?.prepTime?.let { prepTime ->
                                                        val hours = prepTime.toInt()
                                                        val minutes = ((prepTime - hours) * 60).roundToInt()
                                                        minutes.toString()
                                                    } ?: "0"
                                                )
                                            }
                                            var modalDifficulty by remember(details) { mutableStateOf(
                                                when (details?.difficulty) {
                                                    Difficulty.EASY -> "Easy"
                                                    Difficulty.MODERATE -> "Moderate"
                                                    Difficulty.DIFFICULT -> "Difficult"
                                                    else -> ""
                                                }
                                            ) }
                                            var modalServings by remember(details) { mutableStateOf(details?.servings.toString()) }
                                            var modalCalories by remember(details) { mutableStateOf(details?.calories.toString()) }


                                            Text(
                                                text = "Preparation time",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                color = colors.FontColor,
                                                modifier = Modifier.padding(start = 4.dp).align(Alignment.Start)
                                            )

                                            Spacer(Modifier.height(4.dp))

                                            Row {
                                                CustomOutlinedTextField(
                                                    value = modalHours,
                                                    onValueChange = {
                                                        modalError = ""
                                                        modalHours = it
                                                    },
                                                    label = "Hours",
                                                    modifier = Modifier.weight(1f),
                                                    textStyle = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    ),
                                                    singleLine = true,
                                                    borderColor = colors.DarkOrange,
                                                    focusedBorderColor = colors.Orange100,
                                                    isError = modalError != "",
                                                )

                                                Spacer(Modifier.width(14.dp))

                                                Text(
                                                    text = "hr",
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp
                                                    ),
                                                    color = colors.FontColor,
                                                    modifier = Modifier.align(Alignment.CenterVertically)
                                                )

                                                Spacer(Modifier.width(24.dp))

                                                CustomOutlinedTextField(
                                                    value = modalMinutes,
                                                    onValueChange = {
                                                        modalError = ""
                                                        modalMinutes = it
                                                    },
                                                    label = "Minutes",
                                                    modifier = Modifier.weight(1f),
                                                    textStyle = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    ),
                                                    singleLine = true,
                                                    borderColor = colors.DarkOrange,
                                                    focusedBorderColor = colors.Orange100,
                                                    isError = modalError != "",
                                                )

                                                Spacer(Modifier.width(14.dp))

                                                Text(
                                                    text = "min",
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp
                                                    ),
                                                    color = colors.FontColor,
                                                    modifier = Modifier.align(Alignment.CenterVertically)
                                                )
                                            }

                                            Spacer(Modifier.height(14.dp))

                                            Text(
                                                text = "Difficulty",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                color = colors.FontColor,
                                                modifier = Modifier.padding(start = 4.dp).align(Alignment.Start)
                                            )

                                            Spacer(Modifier.height(4.dp))

                                            CustomDropdownField(
                                                options = listOf("Easy", "Moderate", "Difficult"),
                                                selectedOption = modalDifficulty,
                                                onOptionSelected = { modalDifficulty = it },
                                                borderColor = colors.DarkOrange,
                                                focusedBorderColor = colors.Orange100,
                                                label = "Select a difficulty",
                                            )

                                            Spacer(Modifier.height(14.dp))

                                            Text(
                                                text = "Servings",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                color = colors.FontColor,
                                                modifier = Modifier.padding(start = 4.dp).align(Alignment.Start)
                                            )

                                            Spacer(Modifier.height(4.dp))

                                            CustomOutlinedTextField(
                                                value = modalServings,
                                                onValueChange = {
                                                    modalError = ""
                                                    modalServings = it
                                                },
                                                label = "Enter servings",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp),
                                                textStyle = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                singleLine = true,
                                                borderColor = colors.DarkOrange,
                                                focusedBorderColor = colors.Orange100,
                                                isError = modalError != "",
                                            )

                                            Spacer(Modifier.height(14.dp))

                                            Text(
                                                text = "Calories",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                color = colors.FontColor,
                                                modifier = Modifier.padding(start = 4.dp).align(Alignment.Start)
                                            )

                                            Spacer(Modifier.height(4.dp))

                                            CustomOutlinedTextField(
                                                value = modalCalories,
                                                onValueChange = {
                                                    modalError = ""
                                                    modalCalories = it
                                                },
                                                label = "Enter calories",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp),
                                                textStyle = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                singleLine = true,
                                                borderColor = colors.DarkOrange,
                                                focusedBorderColor = colors.Orange100,
                                                isError = modalError != "",
                                            )

                                            Spacer(Modifier.height(4.dp))

                                            if (modalError != "") {
                                                Text(
                                                    text = modalError,
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = colors.Error
                                                    ),
                                                    modifier = Modifier
                                                        .padding(start = 4.dp)
                                                        .align(Alignment.Start),

                                                    )
                                            }

                                            Spacer(Modifier.height(24.dp))

                                            Row(Modifier.fillMaxWidth()) {
                                                Button(
                                                    onClick = { onDismiss() },
                                                    modifier = Modifier
                                                        .align(Alignment.CenterVertically)
                                                        .weight(1f),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colors.ModalBackground,
                                                        contentColor = colors.AI
                                                    ),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        colors.LightOutline
                                                    ),
                                                    contentPadding = PaddingValues(
                                                        horizontal = 12.dp,
                                                        vertical = 12.dp
                                                    ),
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.ai_icon),
                                                        contentDescription = "AI",
                                                        modifier = Modifier
                                                            .align(Alignment.CenterVertically)
                                                            .size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Suggest",
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
                                                        if (modalCalories == "" || modalDifficulty == "" || modalHours == "" || modalMinutes == "" || modalServings == "") {
                                                            modalError =
                                                                "Please fill out all the fields"
                                                        } else {
                                                            details = RecipeDetails(
                                                                prepTime = modalHours.toFloat() + (modalMinutes.toFloat() / 60),
                                                                difficulty = when (modalDifficulty) {
                                                                    "Easy" -> Difficulty.EASY
                                                                    "Moderate" -> Difficulty.MODERATE
                                                                    "Difficult" -> Difficulty.DIFFICULT
                                                                    else -> null
                                                                },
                                                                servings = modalServings.toInt(),
                                                                calories = modalCalories.toInt()
                                                            )
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
                                                    contentPadding = PaddingValues(
                                                        horizontal = 12.dp,
                                                        vertical = 12.dp
                                                    ),
                                                ) {
                                                    Text(
                                                        text = "Save details",
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
                ) {
                    Column (
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_timer),
                            contentDescription = "Clock",
                            modifier = Modifier.size(30.dp).align(Alignment.CenterHorizontally),
                            colorFilter = ColorFilter.tint(colors.FontColor.copy(alpha = 0.75f))
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val prepTime = details?.prepTime ?: 0f
                        val hours = prepTime.toInt()
                        val minutes = ((prepTime - hours) * 60).roundToInt()

                        Text(
                            text = "${hours}hr ${minutes}min",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.FontColor.copy(alpha = 0.75f),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(colors.FontColor.copy(alpha = 0.75f))
                    )

                    Column (
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_weight),
                            contentDescription = "Difficulty",
                            modifier = Modifier.size(30.dp).align(Alignment.CenterHorizontally),
                            colorFilter = ColorFilter.tint(colors.FontColor.copy(alpha = 0.75f))
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = when (details!!.difficulty) {
                                Difficulty.EASY -> "Easy"
                                Difficulty.MODERATE -> "Moderate"
                                Difficulty.DIFFICULT -> "Difficult"
                                else -> ""
                            },
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.FontColor.copy(alpha = 0.75f),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(colors.FontColor.copy(alpha = 0.75f))
                    )

                    Column (
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_users),
                            contentDescription = "Servings",
                            modifier = Modifier.size(30.dp).align(Alignment.CenterHorizontally),
                            colorFilter = ColorFilter.tint(colors.FontColor.copy(alpha = 0.75f))
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${details!!.servings} por.",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.FontColor.copy(alpha = 0.75f),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(colors.FontColor.copy(alpha = 0.75f))
                    )

                    Column (
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_bolt),
                            contentDescription = "Calories",
                            modifier = Modifier.size(30.dp).align(Alignment.CenterHorizontally),
                            colorFilter = ColorFilter.tint(colors.FontColor.copy(alpha = 0.75f))
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${details!!.calories} kcal",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.FontColor.copy(alpha = 0.75f),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            } else {
                Row {
                    Button(
                        onClick = {
                            showModal(
                                ModalType.Custom { onDismiss ->
                                    showModal(
                                        ModalType.Custom { onDismiss ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "Add details",
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 22.sp,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    color = colors.FontColor
                                                )

                                                Spacer(Modifier.height(24.dp))

                                                var modalError by remember { mutableStateOf("") }
                                                var modalHours by remember { mutableStateOf("") }
                                                var modalMinutes by remember { mutableStateOf("") }
                                                var modalDifficulty by remember { mutableStateOf("") }
                                                var modalServings by remember { mutableStateOf("") }
                                                var modalCalories by remember { mutableStateOf("") }

                                                Text(
                                                    text = "Preparation time",
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    ),
                                                    color = colors.FontColor,
                                                    modifier = Modifier.padding(start = 4.dp).align(Alignment.Start)
                                                )

                                                Spacer(Modifier.height(4.dp))

                                                Row {
                                                    CustomOutlinedTextField(
                                                        value = modalHours,
                                                        onValueChange = {
                                                            modalError = ""
                                                            modalHours = it
                                                        },
                                                        label = "Hours",
                                                        modifier = Modifier.weight(1f),
                                                        textStyle = TextStyle(
                                                            fontFamily = Nunito,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 16.sp
                                                        ),
                                                        singleLine = true,
                                                        borderColor = colors.DarkOrange,
                                                        focusedBorderColor = colors.Orange100,
                                                        isError = modalError != "",
                                                    )

                                                    Spacer(Modifier.width(14.dp))

                                                    Text(
                                                        text = "hr",
                                                        style = TextStyle(
                                                            fontFamily = Nunito,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 16.sp
                                                        ),
                                                        color = colors.FontColor,
                                                        modifier = Modifier.align(Alignment.CenterVertically)
                                                    )

                                                    Spacer(Modifier.width(24.dp))

                                                    CustomOutlinedTextField(
                                                        value = modalMinutes,
                                                        onValueChange = {
                                                            modalError = ""
                                                            modalMinutes = it
                                                        },
                                                        label = "Minutes",
                                                        modifier = Modifier.weight(1f),
                                                        textStyle = TextStyle(
                                                            fontFamily = Nunito,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 16.sp
                                                        ),
                                                        singleLine = true,
                                                        borderColor = colors.DarkOrange,
                                                        focusedBorderColor = colors.Orange100,
                                                        isError = modalError != "",
                                                    )

                                                    Spacer(Modifier.width(14.dp))

                                                    Text(
                                                        text = "min",
                                                        style = TextStyle(
                                                            fontFamily = Nunito,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 16.sp
                                                        ),
                                                        color = colors.FontColor,
                                                        modifier = Modifier.align(Alignment.CenterVertically)
                                                    )
                                                }

                                                Spacer(Modifier.height(14.dp))

                                                Text(
                                                    text = "Difficulty",
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    ),
                                                    color = colors.FontColor,
                                                    modifier = Modifier.padding(start = 4.dp).align(Alignment.Start)
                                                )

                                                Spacer(Modifier.height(4.dp))

                                                CustomDropdownField(
                                                    options = listOf("Easy", "Moderate", "Difficult"),
                                                    selectedOption = modalDifficulty,
                                                    onOptionSelected = { modalDifficulty = it },
                                                    borderColor = colors.DarkOrange,
                                                    focusedBorderColor = colors.Orange100,
                                                    label = "Select a difficulty",
                                                )

                                                Spacer(Modifier.height(14.dp))

                                                Text(
                                                    text = "Servings",
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    ),
                                                    color = colors.FontColor,
                                                    modifier = Modifier.padding(start = 4.dp).align(Alignment.Start)
                                                )

                                                Spacer(Modifier.height(4.dp))

                                                CustomOutlinedTextField(
                                                    value = modalServings,
                                                    onValueChange = {
                                                        modalError = ""
                                                        modalServings = it
                                                    },
                                                    label = "Enter servings",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(50.dp),
                                                    textStyle = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    ),
                                                    singleLine = true,
                                                    borderColor = colors.DarkOrange,
                                                    focusedBorderColor = colors.Orange100,
                                                    isError = modalError != "",
                                                )

                                                Spacer(Modifier.height(14.dp))

                                                Text(
                                                    text = "Calories",
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    ),
                                                    color = colors.FontColor,
                                                    modifier = Modifier.padding(start = 4.dp).align(Alignment.Start)
                                                )

                                                Spacer(Modifier.height(4.dp))

                                                CustomOutlinedTextField(
                                                    value = modalCalories,
                                                    onValueChange = {
                                                        modalError = ""
                                                        modalCalories = it
                                                    },
                                                    label = "Enter calories",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(50.dp),
                                                    textStyle = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    ),
                                                    singleLine = true,
                                                    borderColor = colors.DarkOrange,
                                                    focusedBorderColor = colors.Orange100,
                                                    isError = modalError != "",
                                                )

                                                Spacer(Modifier.height(4.dp))

                                                if (modalError != "") {
                                                    Text(
                                                        text = modalError,
                                                        style = TextStyle(
                                                            fontFamily = Nunito,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = colors.Error
                                                        ),
                                                        modifier = Modifier
                                                            .padding(start = 4.dp)
                                                            .align(Alignment.Start),

                                                        )
                                                }

                                                Spacer(Modifier.height(24.dp))

                                                Row(Modifier.fillMaxWidth()) {
                                                    Button(
                                                        onClick = { onDismiss() },
                                                        modifier = Modifier
                                                            .align(Alignment.CenterVertically)
                                                            .weight(1f),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = colors.ModalBackground,
                                                            contentColor = colors.AI
                                                        ),
                                                        border = BorderStroke(
                                                            1.dp,
                                                            colors.LightOutline
                                                        ),
                                                        contentPadding = PaddingValues(
                                                            horizontal = 12.dp,
                                                            vertical = 12.dp
                                                        ),
                                                    ) {
                                                        Image(
                                                            painter = painterResource(id = R.drawable.ai_icon),
                                                            contentDescription = "AI",
                                                            modifier = Modifier
                                                                .align(Alignment.CenterVertically)
                                                                .size(24.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Suggest",
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
                                                            if (modalCalories == "" || modalDifficulty == "" || modalHours == "" || modalMinutes == "" || modalServings == "") {
                                                                modalError =
                                                                    "Please fill out all the fields"
                                                            } else {
                                                                details = RecipeDetails(
                                                                    prepTime = modalHours.toFloat() + (modalMinutes.toFloat() / 60),
                                                                    difficulty = when (modalDifficulty) {
                                                                        "Easy" -> Difficulty.EASY
                                                                        "Moderate" -> Difficulty.MODERATE
                                                                        "Difficult" -> Difficulty.DIFFICULT
                                                                        else -> null
                                                                    },
                                                                    servings = modalServings.toInt(),
                                                                    calories = modalCalories.toInt()
                                                                )
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
                                                        contentPadding = PaddingValues(
                                                            horizontal = 12.dp,
                                                            vertical = 12.dp
                                                        ),
                                                    ) {
                                                        Text(
                                                            text = "Save details",
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
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.Background,
                            contentColor = colors.FontColor
                        ),
                        border = BorderStroke(1.dp, colors.LightOutline),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.plus_icon),
                            contentDescription = "Add",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add details",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.Background,
                            contentColor = colors.AI
                        ),
                        border = BorderStroke(1.dp, colors.LightOutline),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ai_icon),
                            contentDescription = "Add",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Suggest",
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

        Spacer(modifier = Modifier.height(24.dp))

        Section(
            title = "Tags"
        ) {
            if (tags.isNotEmpty()) {
                FlowRow (
                    modifier = Modifier.clickable {
                        showModal(
                            ModalType.Custom { onDismiss ->
                                showModal(
                                    ModalType.Custom { onDismiss ->
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Add tags",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 22.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                color = colors.FontColor
                                            )

                                            Spacer(Modifier.height(18.dp))

                                            var modalError by remember { mutableStateOf("") }
                                            var allTags by remember { mutableStateOf<List<Tag>>(emptyList()) }
                                            var modalTags by remember { mutableStateOf(tags.toMutableSet()) }

                                            LaunchedEffect(Unit) {
                                                viewModel.fetchTags { response, error ->
                                                    if (error != null) {
                                                        modalError = error
                                                    } else {
                                                        allTags = response ?: emptyList()
                                                    }
                                                }
                                            }

                                            val groupedTags = allTags.groupBy { it.group }

                                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                                groupedTags.forEach { (group, groupTags) ->
                                                    item {
                                                        Column(
                                                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                                                        ) {
                                                            Text(
                                                                text = group,
                                                                style = TextStyle(
                                                                    fontFamily = Nunito,
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    fontSize = 16.sp
                                                                ),
                                                                modifier = Modifier.padding(bottom = 4.dp)
                                                            )
                                                            FlowRow(
                                                                modifier = Modifier.fillMaxWidth(),
                                                            ) {
                                                                groupTags.forEach { tag ->
                                                                    val isSelected = remember { mutableStateOf(tag in modalTags) }
                                                                    TagItem(
                                                                        tag = tag,
                                                                        isSelected = isSelected.value,
                                                                        onToggle = {
                                                                            isSelected.value = !isSelected.value
                                                                            if (isSelected.value) {
                                                                                modalTags.add(tag)
                                                                            } else {
                                                                                modalTags.remove(tag)
                                                                            }
                                                                        },
                                                                        isClickable = true
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(Modifier.height(4.dp))

                                            if (modalError.isNotEmpty()) {
                                                Text(
                                                    text = modalError,
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = colors.Error
                                                    ),
                                                    modifier = Modifier.padding(start = 4.dp).align(Alignment.Start)
                                                )
                                            }

                                            Spacer(Modifier.height(24.dp))

                                            Button(
                                                onClick = {
                                                    if (modalTags.isEmpty()) {
                                                        modalError = "Please select at least one tag"
                                                        return@Button
                                                    }
                                                    if (modalTags.size > 5) {
                                                        modalError = "You can only select up to 5 tags"
                                                        return@Button
                                                    }

                                                    tags = modalTags.toList()
                                                    onDismiss()
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colors.Orange100,
                                                    contentColor = Color.White
                                                ),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                                            ) {
                                                Text(
                                                    text = "Save tags",
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        )

                    }
                ) {
                    tags.forEach { tag ->
                        TagItem(
                            tag = tag,
                            isSelected = true,
                            onToggle = {},
                            isClickable = false
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                                showModal(
                                    ModalType.Custom { onDismiss ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Add tags",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 22.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                color = colors.FontColor
                                            )

                                            Spacer(Modifier.height(18.dp))

                                            var modalError by remember { mutableStateOf("") }
                                            var allTags by remember {
                                                mutableStateOf<List<Tag>>(
                                                    emptyList()
                                                )
                                            }
                                            var modalTags by remember {
                                                mutableStateOf<List<Tag>>(
                                                    emptyList()
                                                )
                                            }

                                            LaunchedEffect(Unit) {
                                                viewModel.fetchTags { response, error ->
                                                    if (error != null) {
                                                        errorMessage = error
                                                    } else {
                                                        allTags = response ?: emptyList()
                                                    }
                                                }
                                            }

                                            val groupedTags = allTags.groupBy { it.group }

                                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                                groupedTags.forEach { (group, groupTags) ->
                                                    item {
                                                        Column(
                                                            modifier = Modifier.fillMaxWidth()
                                                                .padding(8.dp)
                                                        ) {
                                                            Text(
                                                                text = group,
                                                                style = TextStyle(
                                                                    fontFamily = Nunito,
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    fontSize = 16.sp
                                                                ),
                                                                modifier = Modifier.padding(bottom = 4.dp)
                                                            )
                                                            FlowRow(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                //horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                groupTags.forEach { tag ->
                                                                    TagItem(
                                                                        tag = tag,
                                                                        isSelected = modalTags.contains(
                                                                            tag
                                                                        ),
                                                                        onToggle = {
                                                                            modalTags =
                                                                                if (modalTags.contains(
                                                                                        tag
                                                                                    )
                                                                                ) {
                                                                                    modalTags - tag
                                                                                } else {
                                                                                    modalTags + tag
                                                                                }
                                                                        },
                                                                        isClickable = true
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(Modifier.height(4.dp))

                                            if (modalError != "") {
                                                Text(
                                                    text = modalError,
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = colors.Error
                                                    ),
                                                    modifier = Modifier
                                                        .padding(start = 4.dp)
                                                        .align(Alignment.Start),

                                                    )
                                            }

                                            Spacer(Modifier.height(24.dp))

                                            Button(
                                                onClick = {
                                                    if (modalTags.isEmpty()) {
                                                        modalError =
                                                            "Please select at least one tag"
                                                        return@Button
                                                    }
                                                    if (modalTags.size > 5) {
                                                        modalError =
                                                            "You can only select up to 5 tags"
                                                        return@Button
                                                    }

                                                    tags = modalTags
                                                    onDismiss()
                                                },

                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colors.Orange100,
                                                    contentColor = Color.White
                                                ),
                                                contentPadding = PaddingValues(
                                                    horizontal = 12.dp,
                                                    vertical = 12.dp
                                                ),
                                            ) {
                                                Text(
                                                    text = "Save tags",
                                                    style = TextStyle(
                                                        fontFamily = Nunito,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp
                                                    ),
                                                )
                                            }
                                        }

                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.Background,
                        contentColor = colors.FontColor
                    ),
                    border = BorderStroke(1.dp, colors.LightOutline),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        text = "Add tags",
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Section(
            title = "Ingredients"
        ) {
            for (ingredient in ingredients) {
                IngredientItem(
                    ingredient,
                    onClick = {
                        showModal(
                            ModalType.Custom { onDismiss ->
                                val modalError = remember { mutableStateOf("") }
                                val modalName = remember { mutableStateOf(ingredient.name) }
                                val modalQuantity = remember { mutableStateOf(ingredient.quantity) }
                                val modalIcon = remember { mutableStateOf(ingredient.icon) }
                                val showPickIconScreen = remember { mutableStateOf(false) }

                                val context = LocalContext.current
                                val iconPath = "emojis/${modalIcon.value}.png"
                                val bitmap = remember(modalIcon.value) {
                                    try {
                                        BitmapFactory.decodeStream(context.assets.open(iconPath))
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                if (!showPickIconScreen.value) { // Normal ingredient form
                                    Text(
                                        text = "Add ingredient",
                                        style = TextStyle(
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp,
                                            textAlign = TextAlign.Center
                                        ),
                                        color = colors.FontColor
                                    )

                                    Spacer(Modifier.height(24.dp))

                                    CustomOutlinedTextField(
                                        value = modalName.value,
                                        onValueChange = { modalName.value = it },
                                        label = "Ingredient name",
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        singleLine = true,
                                        borderColor = colors.DarkOrange,
                                        focusedBorderColor = colors.Orange100,
                                        isError = modalError.value != "",
                                    )

                                    Spacer(Modifier.height(14.dp))

                                    CustomOutlinedTextField(
                                        value = modalQuantity.value,
                                        onValueChange = { modalQuantity.value = it },
                                        label = "Quantity",
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        singleLine = true,
                                        borderColor = colors.DarkOrange,
                                        focusedBorderColor = colors.Orange100,
                                        isError = modalError.value != "",
                                    )

                                    Spacer(Modifier.height(14.dp))

                                    Row {
                                        Image(
                                            bitmap = bitmap?.asImageBitmap() ?: ImageBitmap(1, 1),
                                            contentDescription = "Ingredient Icon",
                                            modifier = Modifier.size(52.dp)
                                        )

                                        Spacer(Modifier.weight(1f))

                                        Button(
                                            onClick = {
                                                showPickIconScreen.value = true // Switch to pick icon screen
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.Background,
                                                contentColor = colors.FontColor
                                            ),
                                            border = BorderStroke(1.dp, colors.DarkOrange),
                                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                            shape = RoundedCornerShape(50.dp)
                                        ) {
                                            Text(
                                                text = "Pick icon",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 16.sp
                                                ),
                                            )
                                        }
                                    }

                                    if (modalError.value != "") {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = modalError.value,
                                            style = TextStyle(
                                                fontFamily = Nunito,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = colors.Error
                                            ),
                                            modifier = Modifier
                                                .padding(start = 4.dp)
                                                .align(Alignment.Start),

                                            )
                                    }

                                    Spacer(Modifier.height(24.dp))

                                    Row {
                                        Button(
                                            onClick = {
                                                ingredients = ingredients.filter { it != ingredient }
                                                onDismiss()
                                            },
                                            modifier = Modifier
                                                .align(Alignment.CenterVertically)
                                                .weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.ModalBackground,
                                                contentColor = colors.Error
                                            ),
                                            border = BorderStroke(1.dp, colors.Error),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                        ) {
                                            Text(
                                                text = "Delete ingredient",
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
                                                if (modalName.value == "" || modalQuantity.value == "") {
                                                    modalError.value = "Please fill out all the fields"
                                                } else {
                                                    ingredients = ingredients.map {
                                                        if (it == ingredient) {
                                                            it.copy(
                                                                name = modalName.value,
                                                                quantity = modalQuantity.value,
                                                                icon = modalIcon.value
                                                            )
                                                        } else {
                                                            it
                                                        }
                                                    }
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
                                                text = "Save ingredient",
                                                style = TextStyle(
                                                    fontFamily = Nunito,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 16.sp
                                                ),
                                            )
                                        }
                                    }

                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            showPickIconScreen.value = false
                                        }) {
                                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                        }

                                        Text(
                                            text = "Pick Icon",
                                            style = TextStyle(
                                                fontFamily = Nunito,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 22.sp,
                                                textAlign = TextAlign.Center
                                            ),
                                            color = colors.FontColor
                                        )
                                    }

                                    Spacer(Modifier.height(16.dp))

                                    val iconResources: List<String> = context.assets
                                        .list("emojis")?.toList() ?: emptyList()

                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(4),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(iconResources) { iconName ->
                                            val iconPath = "emojis/$iconName"
                                            val bitmap = remember(iconName) {
                                                try {
                                                    BitmapFactory.decodeStream(context.assets.open(iconPath))
                                                } catch (e: Exception) {
                                                    null
                                                }
                                            }

                                            if (bitmap != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .clickable {
                                                            modalIcon.value = iconName.substringBeforeLast(".")
                                                            showPickIconScreen.value = false
                                                        }
                                                        .size(64.dp)
                                                ) {
                                                    Image(
                                                        bitmap = bitmap.asImageBitmap(),
                                                        contentDescription = iconName,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    showModal(
                        ModalType.Custom { onDismiss ->
                            val modalError = remember { mutableStateOf("") }
                            val modalName = remember { mutableStateOf("") }
                            val modalQuantity = remember { mutableStateOf("") }
                            val modalIcon = remember { mutableStateOf("emoji_fork_and_knife_with_plate") }
                            val showPickIconScreen = remember { mutableStateOf(false) }

                            val context = LocalContext.current
                            val iconPath = "emojis/${modalIcon.value}.png"
                            val bitmap = remember(modalIcon.value) {
                                try {
                                    BitmapFactory.decodeStream(context.assets.open(iconPath))
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (!showPickIconScreen.value) { // Normal ingredient form
                                Text(
                                    text = "Add ingredient",
                                    style = TextStyle(
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    color = colors.FontColor
                                )

                                Spacer(Modifier.height(24.dp))

                                CustomOutlinedTextField(
                                    value = modalName.value,
                                    onValueChange = { modalName.value = it },
                                    label = "Ingredient name",
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    singleLine = true,
                                    borderColor = colors.DarkOrange,
                                    focusedBorderColor = colors.Orange100,
                                    isError = modalError.value != "",
                                )

                                Spacer(Modifier.height(14.dp))

                                CustomOutlinedTextField(
                                    value = modalQuantity.value,
                                    onValueChange = { modalQuantity.value = it },
                                    label = "Quantity",
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    singleLine = true,
                                    borderColor = colors.DarkOrange,
                                    focusedBorderColor = colors.Orange100,
                                    isError = modalError.value != "",
                                )

                                Spacer(Modifier.height(14.dp))

                                Row {
                                    Image(
                                        bitmap = bitmap?.asImageBitmap() ?: ImageBitmap(1, 1),
                                        contentDescription = "Ingredient Icon",
                                        modifier = Modifier.size(52.dp)
                                    )

                                    Spacer(Modifier.weight(1f))

                                    Button(
                                        onClick = {
                                            showPickIconScreen.value = true // Switch to pick icon screen
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.Background,
                                            contentColor = colors.FontColor
                                        ),
                                        border = BorderStroke(1.dp, colors.DarkOrange),
                                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                        shape = RoundedCornerShape(50.dp)
                                    ) {
                                        Text(
                                            text = "Pick icon",
                                            style = TextStyle(
                                                fontFamily = Nunito,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp
                                            ),
                                        )
                                    }
                                }

                                if (modalError.value != "") {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = modalError.value,
                                        style = TextStyle(
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = colors.Error
                                        ),
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .align(Alignment.Start),

                                        )
                                }
                                
                                Spacer(Modifier.height(24.dp))
                                
                                Row {
                                    Button(
                                        onClick = { onDismiss() },
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.ModalBackground,
                                            contentColor = colors.FontColor
                                        ),
                                        border = BorderStroke(1.dp, colors.LightOutline),
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
                                            if (modalName.value == "" || modalQuantity.value == "") {
                                                modalError.value = "Please fill out all the fields"
                                            } else {
                                                ingredients = ingredients +
                                                    Ingredient(
                                                        name = modalName.value,
                                                        quantity = modalQuantity.value,
                                                        icon = modalIcon.value
                                                    )
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
                                            text = "Save ingredient",
                                            style = TextStyle(
                                                fontFamily = Nunito,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp
                                            ),
                                        )
                                    }
                                }
                                
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        showPickIconScreen.value = false
                                    }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                    }

                                    Text(
                                        text = "Pick Icon",
                                        style = TextStyle(
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp,
                                            textAlign = TextAlign.Center
                                        ),
                                        color = colors.FontColor
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                val iconResources: List<String> = context.assets
                                    .list("emojis")?.toList() ?: emptyList()

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(4),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(iconResources) { iconName ->
                                        val iconPath = "emojis/$iconName"
                                        val bitmap = remember(iconName) {
                                            try {
                                                BitmapFactory.decodeStream(context.assets.open(iconPath))
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }

                                        if (bitmap != null) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .clickable {
                                                        modalIcon.value = iconName.substringBeforeLast(".")
                                                        showPickIconScreen.value = false
                                                    }
                                                    .size(64.dp)
                                            ) {
                                                Image(
                                                    bitmap = bitmap.asImageBitmap(),
                                                    contentDescription = iconName,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.Background,
                    contentColor = colors.FontColor
                ),
                border = BorderStroke(1.dp, colors.LightOutline),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                shape = RoundedCornerShape(50.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.plus_icon),
                    contentDescription = "Add",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(24.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Add ingredient",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit
) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            ),
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
}


@Composable
fun TagItem(tag: Tag, isClickable: Boolean, isSelected: Boolean, onToggle: () -> Unit) {
    val backgroundColor = if (isSelected) Color(android.graphics.Color.parseColor(tag.color)).copy(alpha = 0.12f) else Color.White
    val textColor = Color(android.graphics.Color.parseColor(tag.color))

    Box(
        modifier = Modifier
            .then(if (isClickable) Modifier.clickable { onToggle() } else Modifier)
            .padding(4.dp)
            .then(if (!isSelected) Modifier.border(1.dp, textColor, shape = MaterialTheme.shapes.small.copy(all = androidx.compose.foundation.shape.CornerSize(50.dp))) else Modifier)
            .background(backgroundColor, shape = MaterialTheme.shapes.small.copy(all = androidx.compose.foundation.shape.CornerSize(50.dp)))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tag.name,
            color = textColor,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = Nunito
            )
        )
    }
}

@Composable
fun IngredientItem (
    ingredient: Ingredient,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val iconPath = "emojis/${ingredient.icon}.png"
    val bitmap = remember(ingredient.icon) {
        try {
            BitmapFactory.decodeStream(context.assets.open(iconPath))
        } catch (e: Exception) {
            null
        }
    }


    val colors = LocalCooklyColors.current

    Row(
        modifier = Modifier
            .then(if (onClick != {}) Modifier.clickable { onClick() } else Modifier)
            .fillMaxWidth()
            .background(colors.LightGray,
        shape = RoundedCornerShape(10.dp)),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                bitmap = bitmap?.asImageBitmap() ?: ImageBitmap(1, 1),
                contentDescription = "Ingredient Icon",
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = ingredient.name,
                style = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                ),
                color = colors.FontColor
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = ingredient.quantity,
                style = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = colors.FontColor.copy(0.75f)
            )
        }
    }
}