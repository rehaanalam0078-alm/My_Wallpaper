package com.example.mywallpaper.ui.favorites

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.mywallpaper.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.StateFlow

class FavoritesViewModel(context: Context) : ViewModel() {
    private val repo = FavoritesRepository(context)
    val favorites: StateFlow<List<com.example.mywallpaper.Wallpaper>> = repo.favorites
    fun toggleFavorite(wallpaper: com.example.mywallpaper.Wallpaper) = repo.toggleFavorite(wallpaper)
}
