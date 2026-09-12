package com.example.mywallpaper.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywallpaper.Wallpaper
import com.example.mywallpaper.data.repository.FavoritesRepository
import com.example.mywallpaper.data.repository.WallpaperRepository
import com.example.mywallpaper.ui.components.EmptyState
import com.example.mywallpaper.ui.components.StaggeredWallpaperGrid
import com.example.mywallpaper.ui.theme.Background
import com.example.mywallpaper.ui.theme.Primary
import com.example.mywallpaper.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryName: String,
    onWallpaperClick: (Wallpaper) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val wallpaperRepo = remember { WallpaperRepository() }
    val favRepo = remember { FavoritesRepository(context) }
    val favorites by favRepo.favorites.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var wallpapers by remember { mutableStateOf<List<Wallpaper>>(emptyList()) }

    LaunchedEffect(categoryName) {
        isLoading = true
        try {
            wallpapers = wallpaperRepo.getWallpapersByCategory(categoryName)
        } catch (_: Exception) {
            wallpapers = emptyList()
        }
        isLoading = false
    }

    val displayTitle = categoryName.replaceFirstChar { it.uppercase() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = displayTitle,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isLoading && wallpapers.isNotEmpty()) {
                            Text(
                                text = "${wallpapers.size} wallpapers",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                wallpapers.isEmpty() -> {
                    EmptyState(
                        title = "No wallpapers found",
                        subtitle = "We couldn't find any wallpapers in $displayTitle yet.",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    StaggeredWallpaperGrid(
                        wallpapers = wallpapers,
                        favorites = favorites,
                        onWallpaperClick = onWallpaperClick,
                        onFavoriteClick = { favRepo.toggleFavorite(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
