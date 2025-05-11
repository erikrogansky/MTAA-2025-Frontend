package com.roganskyerik.cookly.ui

import android.util.Log
import android.widget.Space
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.roganskyerik.cookly.MainActivity
import com.roganskyerik.cookly.MainViewModel
import com.roganskyerik.cookly.R
import com.roganskyerik.cookly.network.FullRecipe
import com.roganskyerik.cookly.network.RecipeOverview
import com.roganskyerik.cookly.network.Review
import com.roganskyerik.cookly.network.isOnline
import com.roganskyerik.cookly.ui.modals.ModalType
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors
import com.roganskyerik.cookly.ui.theme.Nunito
import com.roganskyerik.cookly.utils.LocalRecipeManager
import kotlin.math.roundToInt
import kotlin.math.max
import kotlin.collections.maxOrNull
import kotlin.math.roundToLong

@Composable
fun RecipeScreen(recipeId: String, navController: NavController, showModal: (ModalType) -> Unit, viewModel: MainViewModel = hiltViewModel(), isOffline: Boolean = false) {
    val activity = LocalActivity.current as MainActivity
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val recipe by viewModel.recipe.collectAsState()
    val colors = LocalCooklyColors.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val isOnline = remember { mutableStateOf(isOnline(context)) }

    LaunchedEffect(recipeId) {
        val message = """{ "type": "subscribe_recipe", "recipeId": "$recipeId" }"""
        viewModel.sendMessageToServer(message)
    }

    DisposableEffect(Unit) {
        onDispose {
            val message = """{ "type": "unsubscribe_recipe", "recipeId": "$recipeId" }"""
            viewModel.sendMessageToServer(message)
            activity.tts.stop()
            viewModel.removeRecipe()
        }
    }

    LaunchedEffect(Unit) {
        isOnline.value = isOnline(context)
    }

    LaunchedEffect(Unit) {
        viewModel.setContext(context)
        viewModel.loadRecipe(recipeId, isOffline, context)
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val imageHeight = screenHeight / 3

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = recipe?.coverPhotoUrl,
            contentDescription = "Recipe image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
        )

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(top = imageHeight - 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(colors.Background)
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe?.title ?: "Unknown",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    ),
                    color = colors.FontColor
                )

                Spacer(modifier = Modifier.weight(1f))

                var isDownloaded by remember { mutableStateOf(false) }
                // var isLiked by remember { mutableStateOf(false) }

                LaunchedEffect(recipeId) {
                    isDownloaded = LocalRecipeManager.isRecipeSaved(context, recipeId)
                }

                if (recipe?.isOwn == true && isOnline.value) {
                    Icon(
                        painter = painterResource(R.drawable.edit_icon),
                        contentDescription = "Edit",
                        modifier = Modifier
                            .size(34.dp)
                            .clickable {
                                navController.navigate("create/${recipe?.id}")
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))
                }

                    Image(
                        painter = painterResource(
                            id = if (isDownloaded) R.drawable.icon_download_full else R.drawable.icon_download_empty
                        ),
                        contentDescription = "Download",
                        modifier = Modifier
                            .size(34.dp)
                            .clickable {
                                recipe?.let {
                                    if (isDownloaded) {
                                        LocalRecipeManager.deleteRecipe(context, it.id.toString())
                                    } else {
                                        LocalRecipeManager.saveRecipe(context, it)
                                    }
                                    isDownloaded = !isDownloaded
                                }
                            }
                    )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val tagScrollState = rememberScrollState()

            Row (
                modifier = Modifier.horizontalScroll(tagScrollState)
            ) {
                for (tag in recipe?.tags ?: emptyList()) {
                    TagItem(
                        tag = tag,
                        isClickable = false,
                        isSelected = true,
                        onToggle = {  }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row {
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

                    val prepTime = recipe?.prepTime ?: 0f
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

                VerticalDivider(
                    modifier = Modifier
                        .height(54.dp)
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
                        text = recipe?.difficulty?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = colors.FontColor.copy(alpha = 0.75f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(54.dp)
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
                        text = "${recipe?.servings} por.",
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = colors.FontColor.copy(alpha = 0.75f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(54.dp)
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
                        text = "${recipe?.calories} kcal",
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

            Spacer(modifier = Modifier.height(24.dp))

            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
                Text(
                    text = "About",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    ),
                    color = colors.FontColor
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    painter = painterResource(id = R.drawable.read_icon),
                    contentDescription = "Heart",
                    modifier = Modifier.size(24.dp)
                        .clickable {
                            recipe?.let {
                                val fullText = "Here is the recipes description: ${it.description}"

                                activity.speak(fullText)
                            }
                        },
                    tint = colors.FontColor,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = recipe?.description ?: "Unknown",
                style = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = colors.FontColor,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
                Text(
                    text = "Ingredients (${recipe?.ingredients?.size ?: 0})",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    ),
                    color = colors.FontColor
                )

                Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        painter = painterResource(id = R.drawable.read_icon),
                        contentDescription = "Volume",
                        modifier = Modifier.size(24.dp)
                            .clickable {
                                recipe?.let {
                                    val ingredients = it.ingredients.joinToString(separator = ". ") { ing ->
                                        "${ing.quantity} of ${ing.name}"
                                    }

                                    val fullText = "For the recipe you will need: $ingredients"

                                    activity.speak(fullText)
                                }
                            },
                        tint = colors.FontColor
                    )

            }

            Spacer(modifier = Modifier.height(10.dp))

            for (ingredient in recipe?.ingredients ?: emptyList()) {
                IngredientItem(
                    ingredient = ingredient
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
                Text(
                    text = "Preparation",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    ),
                    color = colors.FontColor
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    painter = painterResource(id = R.drawable.read_icon),
                    contentDescription = "Volume",
                    modifier = Modifier.size(24.dp)
                        .clickable {
                            recipe?.let {
                                val instructions = it.instructions.withIndex().joinToString(separator = ". ") { (index, step) ->
                                    "Step ${index + 1}: $step"
                                }

                                val fullText = "To prepare the recipe, follow these steps: $instructions."

                                activity.speak(fullText)
                            }
                        },
                    tint = colors.FontColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            for ((index, instruction) in recipe?.instructions?.withIndex() ?: emptyList<String>().withIndex()) {
                InstructionItem(
                    instruction = instruction,
                    number = index + 1
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Images",
                style = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                ),
                color = colors.FontColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (image in recipe?.images ?: emptyList()) {
                    AsyncImage(
                        model = image,
                        contentDescription = "Recipe image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(274.dp, 174.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }

            if(isOnline.value) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Rating",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    ),
                    color = colors.FontColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                val reviewCounts = (1..5).associateWith { rating ->
                    recipe?.reviews?.count { it.rating == rating } ?: 0
                }
                val maxCount = reviewCounts.values.maxOrNull()?.coerceAtLeast(1) ?: 1

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        (5 downTo 1).forEach { stars ->
                            val count = reviewCounts[stars] ?: 0
                            val fillFraction = count.toFloat() / maxCount.toFloat()

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = stars.toString(),
                                    style = TextStyle(
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = colors.FontColor
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Icon(
                                    painter = painterResource(id = R.drawable.icon_star_full),
                                    contentDescription = "Star",
                                    modifier = Modifier.size(24.dp),
                                    tint = colors.Orange100
                                )

                                Spacer(modifier = Modifier.width(7.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fillFraction)
                                        .height(6.dp)
                                        .background(colors.Positive, RoundedCornerShape(5.dp))
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.75f)
                            .padding(bottom = 10.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        val averageRating = recipe?.reviews
                            ?.takeIf { it.isNotEmpty() }
                            ?.map { it.rating }
                            ?.average()
                            ?.takeIf { !it.isNaN() }
                            ?.let { String.format("%.2f", it).toFloat() }
                            ?: 0f

                        Text(
                            text = "$averageRating / 5",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Black,
                                fontSize = 40.sp
                            ),
                            color = colors.FontColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val fullStars = averageRating.roundToInt().coerceIn(0, 5)
                        val emptyStars = 5 - fullStars

                        Row {
                            repeat(fullStars) {
                                Icon(
                                    painter = painterResource(R.drawable.icon_star_full),
                                    contentDescription = "Full star",
                                    modifier = Modifier.size(30.dp),
                                    tint = colors.Orange100
                                )
                            }
                            repeat(emptyStars) {
                                Icon(
                                    painter = painterResource(R.drawable.icon_star_empty),
                                    contentDescription = "Empty star",
                                    modifier = Modifier.size(30.dp),
                                    tint = colors.Orange100
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${recipe?.reviews?.size} review${if (recipe?.reviews?.size == 1) " " else "s"}",
                            style = TextStyle(
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.FontColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        showModal(ModalType.Custom { onDismiss ->
                            Text(
                                text = "What is your rating?",
                                style = TextStyle(
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                ),
                                color = colors.FontColor
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            var rating by remember { mutableStateOf(0) }

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (i in 1..5) {
                                    val iconRes =
                                        if (i <= rating) R.drawable.icon_star_full else R.drawable.icon_star_empty

                                    Icon(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = "Star $i",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clickable {
                                                rating = i
                                            },
                                        tint = colors.Orange100
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(30.dp))

                            Text(
                                text = "Please share your thoughts",
                                style = TextStyle(
                                    fontFamily = Nunito,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                ),
                                color = colors.FontColor
                            )

                            Spacer(modifier = Modifier.height(25.dp))

                            var reviewText by remember { mutableStateOf("") }
                            var errorMessage by remember { mutableStateOf<String?>(null) }

                            CustomMultilineTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                label = "Write your review here...",
                                borderColor = colors.Orange100,
                                focusedBorderColor = colors.DarkOrange
                            )

                            Spacer(modifier = Modifier.height(38.dp))

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage ?: "",
                                    style = TextStyle(
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = colors.Error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Button(
                                onClick = {
                                    if (rating == 0) {
                                        errorMessage = "Please select a rating"
                                        return@Button
                                    }

                                    viewModel.postReview(
                                        recipeId = recipeId,
                                        rating = rating,
                                        comment = reviewText
                                    ) { response, error ->
                                        if (error != null) {
                                            errorMessage = error
                                            Log.d("RecipeScreen", "Error: $error")
                                        }
                                        if (response != null) {
                                            Log.d("RecipeScreen", "Response: $response")
                                            onDismiss()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(50.dp),
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
                                    text = "Publish review",
                                    style = TextStyle(
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    ),
                                )
                            }
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    border = BorderStroke(1.dp, colors.Orange100),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.ModalBackground,
                        contentColor = colors.FontColor
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 12.dp
                    ),
                ) {
                    Text(
                        text = "Leave a review",
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = colors.FontColor
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                for (review in recipe?.reviews ?: emptyList()) {
                    ReviewItem(
                        review = review
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}


@Composable
fun ReviewItem(review: Review) {
    val colors = LocalCooklyColors.current

    Column {
        Row {
            AsyncImage(
                model = review.user.profilePictureUrl?.let { "$it?ts=${System.currentTimeMillis()}" },
                contentDescription = "User profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(50.dp))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = review.user.name,
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = colors.FontColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    repeat(review.rating) {
                        Icon(
                            painter = painterResource(R.drawable.icon_star_full),
                            contentDescription = "Full star",
                            modifier = Modifier.size(20.dp),
                            tint = colors.Orange100
                        )
                    }
                    repeat(5 - review.rating) {
                        Icon(
                            painter = painterResource(R.drawable.icon_star_empty),
                            contentDescription = "Empty star",
                            modifier = Modifier.size(20.dp),
                            tint = colors.Orange100
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = review.createdAt,
                style = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = colors.FontColor.copy(alpha = 0.75f),
                modifier = Modifier.align(Alignment.Top)
            )
        }

        if (review.text != null) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = review.text,
                style = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = colors.FontColor
            )
        }
    }
}