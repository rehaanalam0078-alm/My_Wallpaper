package com.example.mywallpaper.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywallpaper.Wallpaper
import com.example.mywallpaper.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val featured: Wallpaper? = null,
    val trending: List<Wallpaper> = emptyList(),
    val latest: List<Wallpaper> = emptyList(),
    val categories: List<String> = emptyList(),
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val repo = WallpaperRepository()

    private val _state = MutableStateFlow(HomeUiState(isLoading = true))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val trending = repo.getTrending(20)
                val featured = repo.getFeaturedWallpaper()
                val categories = repo.getCategories()
                _state.value = HomeUiState(
                    isLoading = false,
                    featured = featured,
                    trending = trending,
                    latest = trending.shuffled().take(12),
                    categories = categories
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load wallpapers"
                )
            }
        }
    }
}
