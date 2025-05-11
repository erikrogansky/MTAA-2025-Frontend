package com.roganskyerik.cookly.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.widget.Space
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.Slider
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.roganskyerik.cookly.MainViewModel
import com.roganskyerik.cookly.R
import com.roganskyerik.cookly.network.RecipeOverview
import com.roganskyerik.cookly.ui.modals.ModalType
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import com.roganskyerik.cookly.ui.theme.Nunito
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun DiscoverScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel(), showModal: (ModalType) -> Unit) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var recipes by remember { mutableStateOf(listOf<RecipeOverview>()) }
    var search by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    var selectedCountry by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("") }
    var selectedMaxPrepTime by remember { mutableStateOf<Float?>(null) }
    var selectedMinRating by remember { mutableStateOf<Float?>(null) }


    val context = LocalContext.current

    val colors = LocalCooklyColors.current

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val lat = location.latitude
                val lon = location.longitude

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Non-blocking version for API 33+
                    geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            country = addresses.firstOrNull()?.countryName ?: ""
                        }

                        override fun onError(errorMessageGeocoder: String?) {
                            errorMessage = "Failed to detect country"
                        }
                    })
                } else {
                    // Blocking version in coroutine for API < 33
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val addresses = geocoder.getFromLocation(lat, lon, 1)
                            val country1 = addresses?.firstOrNull()?.countryName

                            withContext(Dispatchers.Main) {
                                country = country1 ?: ""
                            }
                        } catch (e: Exception) {
                            Log.e("Geocoder", "Geocoding failed: ${e.message}")
                            withContext(Dispatchers.Main) {
                                errorMessage = "Failed to detect country"
                            }
                        }
                    }
                }
            } else {
                errorMessage = "Unable to get your location"
            }
        }
    }

    val filteredRecipes = recipes
        .filter {
        it.title.contains(search, ignoreCase = true) &&
                (selectedCountry.isEmpty() || it.country.equals(selectedCountry, ignoreCase = true)) &&
                (selectedDifficulty.isEmpty() || it.difficulty.equals(selectedDifficulty, ignoreCase = true)) &&
                (selectedMaxPrepTime == null || (it.prepTime ?: 0f) <= selectedMaxPrepTime!!) &&
                (selectedMinRating == null || (it.overallRating) >= selectedMinRating!!.toInt())
        }
        .sortedBy { it.title.lowercase() }


    val myCountryRecipes = recipes.filter {
        it.country == country
    }

    LaunchedEffect(Unit) {
        viewModel.getPublicRecipes { response, error ->
            if (error != null) {
                errorMessage = error
            }
            if (response != null) {
                recipes = response.recipes
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
        Text(
            text = "Discover recipes",
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            ),
            color = colors.FontColor
        )

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

        if (search.isEmpty() && selectedCountry.isEmpty() && selectedDifficulty.isEmpty() && selectedMaxPrepTime == null && selectedMinRating == null) {
            DiscoverSection(
                title = "Trending recipes",
            ) {
                for (recipe in recipes) {
                    RecipeDiscover(recipe, navController)
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (country.isNotEmpty()) {
                DiscoverSection(
                    title = "From $country",
                ) {
                    for (recipe in myCountryRecipes) {
                        RecipeDiscover(recipe, navController)
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
        } else {
            val scrollState = rememberScrollState()

            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                for (recipe in filteredRecipes) {
                    Recipe(recipe, navController)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun RecipeDiscover(recipe: RecipeOverview, navController: NavController) {
    val colors = LocalCooklyColors.current

    Column (
        modifier = Modifier
            .shadow(12.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.ItemBackground)
            //.fillMaxWidth()
            .clickable {
                navController.navigate("recipe_detail/${recipe.id}")
            }
    ) {
        AsyncImage(
            model = recipe.coverPhotoUrl,
            contentDescription = "Recipe image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 222.dp, height = 143.dp)
        )

        Column(
            modifier = Modifier
                .padding(start = 10.dp, top = 6.dp, bottom = 10.dp, end = 10.dp)
                .width(202.dp)
                .wrapContentHeight(),
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

            Spacer(modifier = Modifier.height(4.dp))

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

@Composable
fun DiscoverSection(title: String, content: @Composable () -> Unit) {
    val scrollState = rememberScrollState()

    Column {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            ),
            color = LocalCooklyColors.current.FontColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            content()
        }
    }
}