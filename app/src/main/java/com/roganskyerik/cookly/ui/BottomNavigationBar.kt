package com.roganskyerik.cookly.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.roganskyerik.cookly.R
import com.roganskyerik.cookly.currentRoute
import com.roganskyerik.cookly.ui.theme.LocalCooklyColors

@Composable
fun BottomNavigationBar(navController: NavController) {
    val colors = LocalCooklyColors.current
    val currentRoute = currentRoute(navController)

    val items = listOf(
        BottomNavItem("home", R.drawable.home_icon, "My Recipes"),
        BottomNavItem("discover", R.drawable.global_icon, "Discover"),
        BottomNavItem("create", R.drawable.plus_circle_icon, "Add Recipe"),
        BottomNavItem("account", R.drawable.single_user_icon, "Account"),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(colors.DarkOrange)
            .padding(top = 20.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                BottomNavItemView(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    selectedColor = colors.Orange100,
                    unselectedColor = Color.White
                )
            }
        }
    }
}

@Composable
fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    unselectedColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) selectedColor.copy(alpha = 0.2f) else Color.Transparent)
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(id = item.icon),
            contentDescription = item.label,
            tint = if (isSelected) selectedColor else unselectedColor,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = item.label,
            color = if (isSelected) selectedColor else unselectedColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

data class BottomNavItem(val route: String, val icon: Int, val label: String)