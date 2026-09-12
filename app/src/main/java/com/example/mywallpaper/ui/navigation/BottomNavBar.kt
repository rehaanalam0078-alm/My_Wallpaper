package com.example.mywallpaper.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mywallpaper.ui.theme.*

data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    NavItem(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(Routes.EXPLORE, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
    NavItem(Routes.AI_CREATE, "Create", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    NavItem(Routes.FAVORITES, "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    NavItem(Routes.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun BottomNavBar(navController: NavHostController, modifier: Modifier = Modifier) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xCC0A0C10))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                val iconTint by animateColorAsState(
                    targetValue = if (selected) Primary else TextSecondary,
                    label = "navIconTint"
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) Primary else TextSecondary,
                    label = "navTextColor"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.HOME) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // AI Create gets special gradient treatment
                    if (item.route == Routes.AI_CREATE) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(GradientViolet, GradientCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        color = textColor,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
