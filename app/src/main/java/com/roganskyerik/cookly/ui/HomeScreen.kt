package com.roganskyerik.cookly.ui

import android.util.Log
import android.widget.Space
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.roganskyerik.cookly.MainViewModel
import com.roganskyerik.cookly.R
import com.roganskyerik.cookly.network.RecipeOverview
import com.roganskyerik.cookly.ui.modals.ModalType
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import com.roganskyerik.cookly.ui.theme.Nunito
import com.roganskyerik.cookly.utils.LocalRecipeManager

@Composable
fun HomeScreen(navController: NavController, isOffline: Boolean = false, viewModel: MainViewModel = hiltViewModel(), showModal: (ModalType) -> Unit) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var recipes by remember { mutableStateOf(listOf<RecipeOverview>()) }
    var search by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("") }
    var selectedMaxPrepTime by remember { mutableStateOf<Float?>(null) }
    var selectedMinRating by remember { mutableStateOf<Float?>(null) }
    val context = LocalContext.current

    val colors = LocalCooklyColors.current
    val scrollState = rememberScrollState()

    val filteredRecipes = recipes
        .filter {
            it.title.contains(search, ignoreCase = true) &&
                    (selectedCountry.isEmpty() || it.country.equals(selectedCountry, ignoreCase = true)) &&
                    (selectedDifficulty.isEmpty() || it.difficulty.equals(selectedDifficulty, ignoreCase = true)) &&
                    (selectedMaxPrepTime == null || (it.prepTime ?: 0f) <= selectedMaxPrepTime!!) &&
                    (selectedMinRating == null || (it.overallRating) >= selectedMinRating!!.toInt())
        }
        .sortedBy { it.title.lowercase() }

    LaunchedEffect(Unit) {
        if (!isOffline) {
            viewModel.getOwnRecipes { response, error ->
                if (error != null) {
                    errorMessage = error
                    Log.d("HomeScreen", "Error: $error")
                }
                if (response != null) {
                    recipes = response.recipes
                    Log.d("HomeScreen", "Response: $response")
                }
            }
        } else {
            recipes = LocalRecipeManager
                .getAllSavedRecipes(context)
                .map {
                    RecipeOverview(
                        id = it.id,
                        title = it.title,
                        prepTime = it.prepTime,
                        difficulty = it.difficulty,
                        coverPhotoUrl = it.coverPhotoUrl,
                        overallRating = it.reviews.map { r -> r.rating }.average().toInt(),
                        firstTag = it.tags.firstOrNull(),
                        servings = it.servings,
                        calories = it.calories,
                        country = it.country
                    )
                }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .background(colors.Background),
        verticalArrangement = Arrangement.Top,
    ) {
        if (isOffline) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Offline recipes",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    ),
                    color = colors.FontColor
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        LocalRecipeManager.deleteAllRecipes(context)
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                        viewModel.clearTokens()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
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
        } else {
            Text(
                text = "My recipes",
                style = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = colors.FontColor
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row {
            var textFieldHeight by remember { mutableStateOf(0) }

            CustomOutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = "Search",
                borderColor = colors.Orange100,
                focusedBorderColor = colors.DarkOrange,
                modifier = Modifier.weight(1f).onGloballyPositioned {
                    textFieldHeight = it.size.height
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box (
                modifier = Modifier
                    .height(with(LocalDensity.current) { textFieldHeight.toDp() })
                    .width(with(LocalDensity.current) { textFieldHeight.toDp() })
                    .border(
                        BorderStroke(1.dp, colors.Orange100),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .clickable {
                        showModal(
                            ModalType.Custom { onDismiss ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    var modalCountry by remember { mutableStateOf(selectedCountry) }
                                    var modalDifficulty by remember { mutableStateOf(selectedDifficulty) }
                                    var modalPrepTime by remember { mutableStateOf(selectedMaxPrepTime ?: 2f) }
                                    var modalRating by remember { mutableStateOf(selectedMinRating ?: 3f) }

                                    Text(
                                        text = "Filter Recipes",
                                        style = TextStyle(
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp
                                        ),
                                        color = colors.FontColor
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    Text(
                                        "Country",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = colors.FontColor,
                                        modifier = Modifier.padding(start = 4.dp).align(Alignment.Start)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    CustomOutlinedTextField(
                                        value = modalCountry,
                                        onValueChange = { modalCountry = it },
                                        label = "Enter country",
                                        borderColor = colors.Orange100,
                                        focusedBorderColor = colors.DarkOrange,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(Modifier.height(14.dp))

                                    Text(
                                        "Difficulty",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = colors.FontColor,
                                        modifier = Modifier.padding(start = 4.dp).align(Alignment.Start)
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    CustomDropdownField(
                                        options = listOf("Easy", "Moderate", "Difficult"),
                                        selectedOption = modalDifficulty,
                                        onOptionSelected = { modalDifficulty = it },
                                        borderColor = colors.Orange100,
                                        focusedBorderColor = colors.DarkOrange,
                                        label = "Select difficulty"
                                    )

                                    Spacer(Modifier.height(14.dp))

                                    Text("Max prep time: ${modalPrepTime.toInt()} hrs", fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = colors.FontColor,
                                        modifier = Modifier.padding(start = 4.dp).align(Alignment.Start))
                                    Slider(
                                        value = modalPrepTime,
                                        onValueChange = { modalPrepTime = it },
                                        valueRange = 0f..6f,
                                        steps = 5,
                                        colors = SliderColors(
                                            thumbColor = colors.Orange100,
                                            activeTrackColor = colors.Orange100,
                                            inactiveTrackColor = colors.Orange100.copy(alpha = 0.5f),
                                            activeTickColor = colors.Orange100,
                                            inactiveTickColor = colors.Orange100.copy(alpha = 0.5f),
                                            disabledThumbColor = colors.Orange100.copy(alpha = 0.5f),
                                            disabledActiveTrackColor = colors.Orange100.copy(alpha = 0.5f),
                                            disabledActiveTickColor = colors.Orange100.copy(alpha = 0.5f),
                                            disabledInactiveTrackColor = colors.Orange100.copy(alpha = 0.5f),
                                            disabledInactiveTickColor = colors.Orange100.copy(alpha = 0.5f),
                                        )
                                    )

                                    Spacer(Modifier.height(14.dp))

                                    Text("Min rating: ${modalRating.toInt()}", fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = colors.FontColor,
                                        modifier = Modifier.padding(start = 4.dp).align(Alignment.Start))

                                    Slider(
                                        value = modalRating,
                                        onValueChange = { modalRating = it },
                                        valueRange = 0f..5f,
                                        steps = 4,
                                        colors = SliderColors(
                                            thumbColor = colors.Orange100,
                                            activeTrackColor = colors.Orange100,
                                            inactiveTrackColor = colors.Orange100.copy(alpha = 0.5f),
                                            activeTickColor = colors.Orange100,
                                            inactiveTickColor = colors.Orange100.copy(alpha = 0.5f),
                                            disabledThumbColor = colors.Orange100.copy(alpha = 0.5f),
                                            disabledActiveTrackColor = colors.Orange100.copy(alpha = 0.5f),
                                            disabledActiveTickColor = colors.Orange100.copy(alpha = 0.5f),
                                            disabledInactiveTrackColor = colors.Orange100.copy(alpha = 0.5f),
                                            disabledInactiveTickColor = colors.Orange100.copy(alpha = 0.5f),
                                        )
                                    )

                                    Spacer(Modifier.height(24.dp))

                                    Row(Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = {
                                                selectedCountry = ""
                                                selectedDifficulty = ""
                                                selectedMaxPrepTime = null
                                                selectedMinRating = null
                                                onDismiss()
                                            },
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
                                            Text("Reset", style = TextStyle(
                                                fontFamily = Nunito,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp
                                            ),
                                                modifier = Modifier.align(Alignment.CenterVertically))
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Button(
                                            onClick = {
                                                selectedCountry = modalCountry
                                                selectedDifficulty = modalDifficulty
                                                selectedMaxPrepTime = modalPrepTime
                                                selectedMinRating = modalRating
                                                onDismiss()
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
                                            Text("Apply", style = TextStyle(
                                                fontFamily = Nunito,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp
                                            ),
                                                modifier = Modifier.align(Alignment.CenterVertically))
                                        }
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_bolt),
                    contentDescription = "Search icon",
                    modifier = Modifier.size(24.dp),
                    tint = colors.FontColor
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Display filtered recipes
        Column (
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            for (recipe in filteredRecipes) {
                Recipe(recipe, navController, isOffline = isOffline)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun Recipe(recipe: RecipeOverview, navController: NavController, isOffline: Boolean = false) {
    val colors = LocalCooklyColors.current

    Row(
        modifier = Modifier
            .shadow(12.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.ItemBackground)
            .fillMaxWidth()
            .clickable {
                if(isOffline){
                    navController.navigate("offline_recipe_detail/${recipe.id}")
                } else {
                    navController.navigate("recipe_detail/${recipe.id}")
                }
            },
    ) {
        AsyncImage(
            model = recipe.coverPhotoUrl,
            contentDescription = "Recipe image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 103.dp, height = 105.dp)
        )

        Column(
            modifier = Modifier
                .padding(start = 10.dp, top = 6.dp, bottom = 0.dp, end = 10.dp)
                .fillMaxWidth()
                .height(95.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.title,
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    ),
                    color = colors.FontColor
                )
            }

            Spacer(modifier = Modifier.height(1.dp))

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row (
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_timer),
                        contentDescription = "Clock icon",
                        modifier = Modifier
                            .size(24.dp),
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = recipe.prepTime?.let { "${it.toInt()} hr ${((it % 1) * 60).toInt()} min" }.toString(),
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = colors.FontColor
                    )
                }

                Row (
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_weight),
                        contentDescription = "Clock icon",
                        modifier = Modifier
                            .size(24.dp),
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = (recipe.difficulty).lowercase().replaceFirstChar { it.uppercase() },
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = colors.FontColor
                    )
                }
            }

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fullStars = recipe.overallRating.coerceIn(0, 5)
                val emptyStars = 5 - fullStars

                Row {
                    repeat(fullStars) {
                        Icon(
                            painter = painterResource(R.drawable.icon_star_full),
                            contentDescription = "Full star",
                            modifier = Modifier.size(18.dp),
                            tint = colors.Orange100
                        )
                    }
                    repeat(emptyStars) {
                        Icon(
                            painter = painterResource(R.drawable.icon_star_empty),
                            contentDescription = "Empty star",
                            modifier = Modifier.size(18.dp),
                            tint = colors.Orange100
                        )
                    }
                }


                if (recipe.firstTag != null) {
                    TagItem(recipe.firstTag, isSelected = true, isClickable = false, onToggle = {})
                }
            }
        }
    }
}