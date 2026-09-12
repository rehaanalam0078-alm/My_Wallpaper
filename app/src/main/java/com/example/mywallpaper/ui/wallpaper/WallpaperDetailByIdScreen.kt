package com.example.mywallpaper.ui.wallpaper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywallpaper.Wallpaper
import com.example.mywallpaper.data.repository.WallpaperRepository
import com.example.mywallpaper.ui.components.GradientButton
import com.example.mywallpaper.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetailByIdScreen(
    wallpaperId: String,
    viewModel: WallpaperDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val repo = remember { WallpaperRepository() }
    var isLoading by remember { mutableStateOf(true) }
    var loadedWallpaper by remember { mutableStateOf<Wallpaper?>(null) }

    LaunchedEffect(wallpaperId) {
        isLoading = true
        loadedWallpaper = repo.getWallpaperById(wallpaperId)
        isLoading = false
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        }
        loadedWallpaper != null -> {
            WallpaperDetailScreen(
                wallpaper = loadedWallpaper!!,
                viewModel = viewModel,
                onNavigateBack = onNavigateBack
            )
        }
        else -> {
            // Friendly Unavailable Screen (Requirement 50)
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
                    )
                },
                containerColor = Background
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BrokenImage,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = "This wallpaper is no longer available.",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "It might have been removed or updated by the studio. Explore our vast collection of fresh 4K wallpapers.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(32.dp))

                        GradientButton(
                            text = "Explore Wallpapers",
                            onClick = onNavigateToExplore,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onNavigateToHome,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Outlined.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Go Home", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
