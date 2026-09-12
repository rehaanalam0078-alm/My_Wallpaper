package com.example.mywallpaper.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywallpaper.Wallpaper
import com.example.mywallpaper.data.repository.FavoritesRepository
import com.example.mywallpaper.ui.components.EmptyState
import com.example.mywallpaper.ui.components.StaggeredWallpaperGrid
import com.example.mywallpaper.ui.theme.Background
import com.example.mywallpaper.ui.theme.TextSecondary

@Composable
fun FavoritesScreen(
    onWallpaperClick: (Wallpaper) -> Unit
) {
    val context = LocalContext.current
    val favRepo = remember { FavoritesRepository(context) }
    val favorites by favRepo.favorites.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 56.dp, bottom = 20.dp)) {
            Text("Favorites", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("${favorites.size} wallpapers saved", color = TextSecondary, fontSize = 14.sp)
        }

        if (favorites.isEmpty()) {
            EmptyState(
                title = "No favorites yet",
                subtitle = "Tap the heart on any wallpaper to save it here",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            StaggeredWallpaperGrid(
                wallpapers = favorites,
                favorites = favorites,
                onWallpaperClick = onWallpaperClick,
                onFavoriteClick = { favRepo.toggleFavorite(it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
