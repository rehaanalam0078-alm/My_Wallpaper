package com.example.mywallpaper.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mywallpaper.Wallpaper

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState

@Composable
fun StaggeredWallpaperGrid(
    wallpapers: List<Wallpaper>,
    favorites: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteClick: (Wallpaper) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(12.dp),
    columns: Int = 2
) {
    LazyVerticalStaggeredGrid(
        state = state,
        columns = StaggeredGridCells.Fixed(columns),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
        modifier = modifier
    ) {
        items(wallpapers, key = { it.imageUrl }) { wallpaper ->
            val aspectRatio = if (wallpapers.indexOf(wallpaper) % 3 == 0) 9f / 16f else 3f / 4f
            WallpaperCard(
                wallpaper = wallpaper,
                isFavorite = favorites.any { it.imageUrl == wallpaper.imageUrl },
                onClick = { onWallpaperClick(wallpaper) },
                onFavoriteClick = { onFavoriteClick(wallpaper) },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
            )
        }
    }
}
