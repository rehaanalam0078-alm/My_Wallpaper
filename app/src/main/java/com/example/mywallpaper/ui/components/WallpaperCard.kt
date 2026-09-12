package com.example.mywallpaper.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mywallpaper.Wallpaper
import com.example.mywallpaper.ui.theme.HeartRed
import com.example.mywallpaper.ui.theme.ShapeCard

@Composable
fun WallpaperCard(
    wallpaper: Wallpaper,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heartColor by animateColorAsState(
        targetValue = if (isFavorite) HeartRed else Color.White.copy(alpha = 0.85f),
        animationSpec = spring(),
        label = "heartColor"
    )

    Box(
        modifier = modifier
            .clip(ShapeCard)
            .background(Color(0xFF1E2024))
            .clickable { onClick() }
    ) {
        // Wallpaper image
        AsyncImage(
            model = wallpaper.imageUrl,
            contentDescription = wallpaper.title.ifBlank { wallpaper.category },
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom gradient overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                    )
                )
        )

        // Top gradient overlay (for icons)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )

        // Favorite button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = heartColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
