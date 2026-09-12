package com.example.mywallpaper.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mywallpaper.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSignOut: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val profile by viewModel.profile.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState())) {
        // Header gradient
        Box(
            modifier = Modifier.fillMaxWidth().height(220.dp)
                .background(Brush.verticalGradient(listOf(GradientViolet.copy(alpha = 0.4f), Background)))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                // Avatar
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(SurfaceContainerHigh)) {
                    if (profile.photoUrl.isNotEmpty()) {
                        AsyncImage(model = profile.photoUrl, contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize())
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(
                            Brush.radialGradient(listOf(GradientViolet, GradientCyan))),
                            contentAlignment = Alignment.Center) {
                            Text(profile.name.firstOrNull()?.toString()?.uppercase() ?: "U",
                                color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(profile.name.ifBlank { "Guest User" }, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(profile.email, color = TextSecondary, fontSize = 13.sp)
            }
        }

        // Stats row
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Favorites", profile.favoritesCount.toString(), modifier = Modifier.weight(1f),
                onClick = onNavigateToFavorites)
            StatCard("AI Created", profile.aiCreationsCount.toString(), modifier = Modifier.weight(1f),
                onClick = onNavigateToHistory)
            StatCard("Downloads", profile.downloadsCount.toString(), modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        // Menu sections
        ProfileMenuSection(title = "My Content") {
            ProfileMenuItem("My Favorites", Icons.Outlined.Favorite, onClick = onNavigateToFavorites)
            ProfileMenuItem("AI Creations", Icons.Outlined.AutoAwesome, onClick = onNavigateToHistory)
        }
        Spacer(Modifier.height(8.dp))
        ProfileMenuSection(title = "Preferences") {
            ProfileMenuItem("Settings", Icons.Outlined.Settings, onClick = onNavigateToSettings)
            ProfileMenuItem("Appearance", Icons.Outlined.Palette, onClick = onNavigateToAppearance)
            ProfileMenuItem("Notifications", Icons.Outlined.Notifications, onClick = onNavigateToNotificationSettings)
        }
        Spacer(Modifier.height(8.dp))
        ProfileMenuSection(title = "More") {
            ProfileMenuItem("Privacy Policy", Icons.Outlined.PrivacyTip, onClick = onNavigateToPrivacyPolicy)
            ProfileMenuItem("About", Icons.Outlined.Info, onClick = onNavigateToAbout)
        }
        Spacer(Modifier.height(16.dp))

        // Logout
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorColor.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor)
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(100.dp))
    }

    val scope = rememberCoroutineScope()

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = SurfaceContainerHigh,
            title = { Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    scope.launch {
                        viewModel.signOut()
                        onSignOut()
                    }
                }) {
                    Text("Sign Out", color = ErrorColor, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(modifier = modifier.clickable { onClick() }, shape = RoundedCornerShape(16.dp), color = SurfaceContainerHigh) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ProfileMenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = SurfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Column { content() }
        }
    }
}

@Composable
private fun ProfileMenuItem(title: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
    }
}
