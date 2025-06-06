package com.roganskyerik.cookly.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.roganskyerik.cookly.MainViewModel
import com.roganskyerik.cookly.R
import com.roganskyerik.cookly.network.FullRecipe
import com.roganskyerik.cookly.ui.modals.ModalType
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import com.roganskyerik.cookly.ui.theme.Nunito
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
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

data class Recipe(
    val recipeId: String?,
    val title: String,
    val description: String,
    val prepTime: Float,
    val difficulty: String,
    val servings: Int,
    val calories: Int,
    val tags: List<String>,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val isPublic: Boolean,
    val coverPhoto: Uri,
    val photos: List<Uri>,
    val country: String?,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateScreen(navController: NavController, showModal: (ModalType) -> Unit, viewModel: MainViewModel = hiltViewModel(), id: String? = null) {
    val colors = LocalCooklyColors.current

    var errorMessage by remember { mutableStateOf("") }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var details by remember { mutableStateOf<RecipeDetails?>(null) }
    var tags by remember { mutableStateOf<List<Tag>>(emptyList()) }
    var ingredients by remember { mutableStateOf<List<Ingredient>>(emptyList()) }
    var instructions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isPublic by remember { mutableStateOf(false) }
    val imageUris = remember { mutableStateListOf<Uri>() }
    var coverPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    var country: String = ""

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val availableSlots = 10 - imageUris.size
        if (availableSlots > 0) {
            val toAdd = uris.take(availableSlots)
            imageUris.addAll(toAdd)
            if (coverPhotoUri == null && toAdd.isNotEmpty()) {
                coverPhotoUri = toAdd.first()
            }
        }
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri.value?.let { uri ->
                if (imageUris.size < 10) {
                    imageUris.add(uri)
                    if (coverPhotoUri == null) {
                        coverPhotoUri = uri
                    }
                }
            }
        }
    }

    val context = LocalContext.current
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val newUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
            )
            cameraImageUri.value = newUri
            takePhotoLauncher.launch(newUri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        if (id != null) {
            viewModel.getRecipeById(id) { response, error ->
                if (error != null) {
                    errorMessage = error
                    Log.d("RecipeScreen", "Error: $error")
                }
                if (response != null) {
                    title = response.recipe.title
                    description = response.recipe.description
                    details = RecipeDetails(
                        prepTime = response.recipe.prepTime,
                        difficulty = when (response.recipe.difficulty) {
                            "EASY" -> Difficulty.EASY
                            "MODERATE" -> Difficulty.MODERATE
                            "DIFFICULT" -> Difficulty.DIFFICULT
                            else -> null
                        },
                        servings = response.recipe.servings.toInt(),
                        calories = response.recipe.calories.toInt()
                    )
                    tags = response.recipe.tags
                    ingredients = response.recipe.ingredients
                    instructions = response.recipe.instructions
                    isPublic = response.recipe.isPublic
                    imageUris.addAll(response.recipe.images.map { Uri.parse(it) })
                    coverPhotoUri = Uri.parse(response.recipe.coverPhotoUrl)
                    country = response.recipe.country.toString()

                    Log.d("RecipeScreen", "Response: $response")
                }
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
            text = if (id != null) "Edit recipe" else "Add recipe",
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            ),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column (
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
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
                                                    onClick = { if (modalTitle == "") {
                                                        modalError = "Please fill out title first for more accurate AI description"
                                                    } else if (ingredients == null || instructions == null) {
                                                        modalError = "You cannot have AI description without ingredients and instructions"
                                                    } else {
                                                        viewModel.generateDescription(
                                                            title = modalTitle,
                                                            ingredients = ingredients,
                                                            instructions = instructions
                                                        ) { generatedDescription, error ->
                                                            if (error != null) {
                                                                modalError = error
                                                            } else {
                                                                if (generatedDescription != null) {
                                                                    modalDescription = generatedDescription
                                                                } else {
                                                                    modalError = "Something went wrong"
                                                                }
                                                            }
                                                        }
                                                    } },
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
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ai_icon),
                                                        contentDescription = "AI",
                                                        modifier = Modifier
                                                            .align(Alignment.CenterVertically)
                                                            .size(24.dp),
                                                        tint = colors.AI
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
                                                        onClick = {
                                                            if (modalTitle == "") {
                                                                modalError = "Please fill out title first for more accurate AI description"
                                                            } else if (ingredients == null || instructions == null) {
                                                                modalError = "You cannot have AI description without ingredients and instructions"
                                                            } else {
                                                                viewModel.generateDescription(
                                                                    title = modalTitle,
                                                                    ingredients = ingredients,
                                                                    instructions = instructions
                                                                ) { generatedDescription, error ->
                                                                    if (error != null) {
                                                                        modalError = error
                                                                    } else {
                                                                        if (generatedDescription != null) {
                                                                            modalDescription = generatedDescription
                                                                        } else {
                                                                            modalError = "Something went wrong"
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        },
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
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.ai_icon),
                                                            contentDescription = "AI",
                                                            modifier = Modifier
                                                                .align(Alignment.CenterVertically)
                                                                .size(24.dp),
                                                            tint = colors.AI
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
                        Icon(
                            painter = painterResource(id = R.drawable.plus_icon),
                            contentDescription = "Add",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(24.dp),
                            tint = colors.FontColor
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
                                                    onClick = {
                                                         if (title == "" || ingredients == null || instructions == null) {
                                                            modalError = "You cannot have AI details without title, ingredients and instructions"
                                                        } else {
                                                            viewModel.generateDetails(
                                                                title = title,
                                                                ingredients = ingredients,
                                                                instructions = instructions
                                                            ) { generatedDetails, error ->
                                                                if (error != null) {
                                                                    modalError = error
                                                                } else {
                                                                    if (generatedDetails != null) {
                                                                        modalCalories = generatedDetails.calories.toString()
                                                                        modalServings = generatedDetails.portions.toString()
                                                                        modalHours = generatedDetails.cook_length.toInt().toString()
                                                                        modalMinutes = ((generatedDetails.cook_length - generatedDetails.cook_length.toInt()) * 60).roundToInt().toString()
                                                                        modalDifficulty = generatedDetails.difficulty.replaceFirstChar { it.uppercaseChar() }
                                                                    } else {
                                                                        modalError = "Something went wrong"
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    },
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
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ai_icon),
                                                        contentDescription = "AI",
                                                        modifier = Modifier
                                                            .align(Alignment.CenterVertically)
                                                            .size(24.dp),
                                                        tint = colors.AI
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
                                                        onClick = {
                                                            if (title == "" || ingredients == null || instructions == null) {
                                                                modalError = "You cannot have AI details without title, ingredients and instructions"
                                                            } else {
                                                                viewModel.generateDetails(
                                                                    title = title,
                                                                    ingredients = ingredients,
                                                                    instructions = instructions
                                                                ) { generatedDetails, error ->
                                                                    if (error != null) {
                                                                        modalError = error
                                                                    } else {
                                                                        if (generatedDetails != null) {
                                                                            modalCalories = generatedDetails.calories.toString()
                                                                            modalServings = generatedDetails.portions.toString()
                                                                            modalHours = generatedDetails.cook_length.toInt().toString()
                                                                            modalMinutes = ((generatedDetails.cook_length - generatedDetails.cook_length.toInt()) * 60).roundToInt().toString()
                                                                            modalDifficulty = generatedDetails.difficulty.replaceFirstChar { it.uppercaseChar() }
                                                                        } else {
                                                                            modalError = "Something went wrong"
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        },
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
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.ai_icon),
                                                            contentDescription = "AI",
                                                            modifier = Modifier
                                                                .align(Alignment.CenterVertically)
                                                                .size(24.dp),
                                                            tint = colors.AI
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
                        Icon(
                            painter = painterResource(id = R.drawable.plus_icon),
                            contentDescription = "Add",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(24.dp),
                            tint = colors.FontColor
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
                        onClick = { if (title == "" || ingredients == null || instructions == null) {
                            errorMessage = "You cannot have AI details without title, ingredients and instructions"
                        } else {
                            viewModel.generateDetails(
                                title = title,
                                ingredients = ingredients,
                                instructions = instructions
                            ) { generatedDetails, error ->
                                if (error != null) {
                                    errorMessage = error
                                } else {
                                    if (generatedDetails != null) {

                                        details = RecipeDetails(
                                            prepTime = generatedDetails.cook_length,
                                            difficulty = when (generatedDetails.difficulty) {
                                                "Easy" -> Difficulty.EASY
                                                "Moderate" -> Difficulty.MODERATE
                                                "Hard" -> Difficulty.DIFFICULT
                                                "easy" -> Difficulty.EASY
                                                "moderate" -> Difficulty.MODERATE
                                                "hard" -> Difficulty.DIFFICULT
                                                else -> null
                                            },
                                            servings = generatedDetails.portions.toInt(),
                                            calories = generatedDetails.calories.toInt()
                                        )
                                    } else {
                                        errorMessage = "Something went wrong"
                                    }
                                }
                            }
                        } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.Background,
                            contentColor = colors.AI
                        ),
                        border = BorderStroke(1.dp, colors.LightOutline),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ai_icon),
                            contentDescription = "Add",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(24.dp),
                            tint = colors.AI
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
                    Icon(
                        painter = painterResource(id = R.drawable.plus_icon),
                        contentDescription = "Add",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(24.dp),
                        tint = colors.FontColor
                    )

                    Spacer(modifier = Modifier.width(6.dp))

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
                                                showPickIconScreen.value = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.ModalBackground,
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
                Icon(
                    painter = painterResource(id = R.drawable.plus_icon),
                    contentDescription = "Add",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(24.dp),
                    tint = colors.FontColor
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

        Spacer(modifier = Modifier.height(24.dp))

        Section(
            title = "Preparation"
        ) {
            for ((index, instruction) in instructions.withIndex()) {
                InstructionItem(
                    instruction = instruction,
                    number = index + 1,
                    onClick = {
                        showModal(
                            ModalType.Custom { onDismiss ->
                                val modalError = remember { mutableStateOf("") }
                                val modalInstruction = remember { mutableStateOf(instruction) }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Add instruction",
                                        style = TextStyle(
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp,
                                            textAlign = TextAlign.Center
                                        ),
                                        color = colors.FontColor
                                    )

                                    Spacer(Modifier.height(24.dp))

                                    CustomMultilineTextField(
                                        value = modalInstruction.value,
                                        onValueChange = { modalInstruction.value = it },
                                        label = "Instruction",
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        borderColor = colors.DarkOrange,
                                        focusedBorderColor = colors.Orange100,
                                        isError = modalError.value != "",
                                    )

                                    Spacer(Modifier.height(14.dp))

                                    if (modalError.value != "") {
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
                                                instructions = instructions.filter { it != instruction }
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
                                            contentPadding = PaddingValues(
                                                horizontal = 12.dp,
                                                vertical = 12.dp
                                            ),
                                        ) {
                                            Text(
                                                text = "Delete instruction",
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
                                                if (modalInstruction.value == "") {
                                                    modalError.value = "Please fill out all the fields"
                                                } else {
                                                    instructions = instructions.map {
                                                        if (it == instruction) {
                                                            modalInstruction.value
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
                                            contentPadding = PaddingValues(
                                                horizontal = 12.dp,
                                                vertical = 12.dp
                                            ),
                                        ) {
                                            Text(
                                                text = "Save instruction",
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
                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = {
                    showModal(
                        ModalType.Custom { onDismiss ->
                            val modalError = remember { mutableStateOf("") }
                            val modalInstruction = remember { mutableStateOf("") }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Add instruction",
                                    style = TextStyle(
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    color = colors.FontColor
                                )

                                Spacer(Modifier.height(24.dp))

                                CustomMultilineTextField(
                                    value = modalInstruction.value,
                                    onValueChange = { modalInstruction.value = it },
                                    label = "Instruction",
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    borderColor = colors.DarkOrange,
                                    focusedBorderColor = colors.Orange100,
                                    isError = modalError.value != "",
                                )

                                Spacer(Modifier.height(14.dp))

                                if (modalError.value != "") {
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
                                            if (modalInstruction.value == "") {
                                                modalError.value = "Please fill out all the fields"
                                            } else {
                                                instructions = instructions + modalInstruction.value
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
                                            text = "Save instruction",
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
                Icon(
                    painter = painterResource(id = R.drawable.plus_icon),
                    contentDescription = "Add",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(24.dp),
                    tint = colors.FontColor
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Add instruction",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

            Section(title = "Photos") {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .size(160.dp)
                            .border(1.dp, colors.LightOutline, shape = RoundedCornerShape(10.dp))
                            .clickable {
                                imagePickerLauncher.launch("image/*")
                            },
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus_icon),
                            contentDescription = "Add photo",
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterHorizontally),
                            tint = colors.FontColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add photos",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val context = LocalContext.current

                    Column(
                        modifier = Modifier
                            .size(160.dp)
                            .border(1.dp, colors.LightOutline, shape = RoundedCornerShape(10.dp))
                            .clickable {
                                val permission = Manifest.permission.CAMERA
                                when {
                                    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                                        // Permission already granted
                                        val newUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                                        )
                                        cameraImageUri.value = newUri
                                        takePhotoLauncher.launch(newUri)
                                    }
                                    else -> {
                                        // Ask for permission
                                        cameraPermissionLauncher.launch(permission)
                                    }
                                }
                            },
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus_icon),
                            contentDescription = "Take a photo",
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterHorizontally),
                            tint = colors.FontColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Take a photo",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    imageUris.forEach { uri ->
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                            )

                            // Trash icon (top right)
                            Image(
                                painter = painterResource(id = R.drawable.logout_icon),
                                contentDescription = "Delete photo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.TopEnd)
                                    .clickable {
                                        imageUris.remove(uri)
                                        if (coverPhotoUri == uri) {
                                            coverPhotoUri = imageUris.firstOrNull()
                                        }
                                    }
                                    .padding(6.dp)
                            )

                            // Cover selector (top left)
                            Image(
                                painter = painterResource(
                                    id = if (coverPhotoUri == uri)
                                        R.drawable.icon_bolt
                                    else
                                        R.drawable.plus_circle_icon
                                ),
                                contentDescription = "Select cover photo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.TopStart)
                                    .clickable {
                                        coverPhotoUri = uri
                                    }
                                    .padding(6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Section(
                title = "Publicity"
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .fillMaxWidth()
                        .background(color = colors.LightGray)
                        .padding(2.dp),
                ) {
                    Button(
                        onClick = { isPublic = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPublic) colors.Background else colors.LightGray,
                            contentColor = colors.FontColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_public),
                            contentDescription = "Public Recipe",
                            modifier = Modifier.size(26.dp),
                            tint = colors.FontColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Public Recipe",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Button(
                        onClick = { isPublic = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isPublic) colors.Background else colors.LightGray,
                            contentColor = colors.FontColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 10.dp, horizontal = 8.dp), // Adjusted horizontal padding
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_private),
                            contentDescription = "Private Recipe",
                            modifier = Modifier.size(26.dp),
                            tint = colors.FontColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Private Recipe",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }

            val context = LocalContext.current

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != "") {
                Text(
                    text = errorMessage,
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

                Spacer(modifier = Modifier.height(6.dp))
            }

            fun createAndSendRecipe(country: String?) {
                val recipe = Recipe(
                    recipeId = id,
                    title = title,
                    description = description,
                    ingredients = ingredients,
                    instructions = instructions,
                    isPublic = isPublic,
                    coverPhoto = coverPhotoUri ?: Uri.EMPTY,
                    photos = imageUris,
                    prepTime = details?.prepTime ?: 0f,
                    difficulty = details?.difficulty.toString(),
                    servings = details?.servings ?: 0,
                    calories = details?.calories ?: 0,
                    tags = tags.map { it.name },
                    country = country
                )

                Log.d("Recipe", "Recipe: $recipe")

                viewModel.createRecipe(recipe, context) { response, error ->
                    if (error != null) {
                        errorMessage = error
                    } else {
                        navController.popBackStack()
                    }
                }
            }

            Button(
                onClick = {
                    if (title == "") {
                        errorMessage = "Please fill out the title"
                        return@Button
                    }
                    if (tags.isEmpty()) {
                        errorMessage = "Please select at least one tag"
                        return@Button
                    }
                    if (ingredients.isEmpty()) {
                        errorMessage = "Please add at least one ingredient"
                        return@Button
                    }
                    if (instructions.isEmpty()) {
                        errorMessage = "Please add at least one instruction"
                        return@Button
                    }

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
                                    geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                                        override fun onGeocode(addresses: MutableList<Address>) {
                                            val country = addresses.firstOrNull()?.countryName
                                            Log.d("Geocoder", "Country: $country")
                                            createAndSendRecipe(country)
                                        }

                                        override fun onError(errorMessageGeocoder: String?) {
                                            Log.e("Geocoder", "Geocoding error: $errorMessageGeocoder")
                                            errorMessage = "Failed to detect country"
                                        }
                                    })
                                } else {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val addresses = geocoder.getFromLocation(lat, lon, 1)
                                            val country = addresses?.firstOrNull()?.countryName

                                            withContext(Dispatchers.Main) {
                                                createAndSendRecipe(country)
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
                    } else {
                        createAndSendRecipe(country)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.Orange100,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = if (id != null) "Save recipe" else "Create recipe",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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
            .then(if (!isSelected) Modifier.border(1.dp, textColor, shape = MaterialTheme.shapes.small.copy(all = CornerSize(50.dp))) else Modifier)
            .background(backgroundColor, shape = MaterialTheme.shapes.small.copy(all = CornerSize(50.dp)))
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

@Composable
fun InstructionItem(instruction: String, number: Number, onClick: () -> Unit = {}) {
    val colors = LocalCooklyColors.current

    Row(
        modifier = Modifier
            .then(if (onClick != {}) Modifier.clickable { onClick() } else Modifier)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$number",
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            ),
            color = colors.FontColor
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .background(colors.Orange100, shape = RoundedCornerShape(50.dp))
                .width(2.dp)
                .height(28.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = instruction,
            style = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            ),
            color = colors.FontColor
        )
    }
}