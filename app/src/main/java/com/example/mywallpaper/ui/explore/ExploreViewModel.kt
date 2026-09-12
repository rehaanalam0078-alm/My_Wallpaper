package com.example.mywallpaper.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywallpaper.Wallpaper
import com.example.mywallpaper.data.repository.WallpaperRepository
import com.example.mywallpaper.data.util.CategoryNormalizer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExploreUiState(
    val isLoading: Boolean = false,
    val wallpapers: List<Wallpaper> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "",
    val categories: List<String> = emptyList(),
    val error: String? = null
)

class ExploreViewModel : ViewModel() {

    private val repo = WallpaperRepository()
    private var allWallpapersCache = listOf<Wallpaper>()
    private var filterJob: Job? = null

    private val _state = MutableStateFlow(ExploreUiState(isLoading = true))
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val wallpapers = repo.getAllWallpapers(forceRefresh)
                allWallpapersCache = wallpapers
                val categories = repo.getCategories()
                
                // Re-apply any active filter or search
                val displayed = applyCurrentFilter(
                    wallpapers,
                    _state.value.selectedCategory,
                    _state.value.searchQuery
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    wallpapers = displayed,
                    categories = categories
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load wallpapers. Please check your connection."
                )
            }
        }
    }

    fun updateSearch(query: String) {
        val trimmed = query.trim()
        _state.value = _state.value.copy(
            searchQuery = query,
            // When user types a non-empty search query, clear the selected category chip
            selectedCategory = if (trimmed.isNotEmpty()) "" else _state.value.selectedCategory
        )

        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            if (trimmed.isNotEmpty()) {
                delay(200) // Debounce keystrokes to prevent race conditions and flickering
            }
            val filtered = applyCurrentFilter(
                allWallpapersCache,
                _state.value.selectedCategory,
                query
            )
            _state.value = _state.value.copy(wallpapers = filtered)
        }
    }

    fun selectCategory(category: String) {
        val normalizedTarget = CategoryNormalizer.normalize(category)
        val currentNormalized = CategoryNormalizer.normalize(_state.value.selectedCategory)

        // Toggle category off if clicking the same one, or if clicking "All" / ""
        val newCategory = if (normalizedTarget == currentNormalized || category.isBlank() || category.equals("all", ignoreCase = true)) {
            ""
        } else {
            category
        }

        filterJob?.cancel()
        _state.value = _state.value.copy(
            selectedCategory = newCategory,
            searchQuery = "" // Selecting a category clears the search query
        )

        val filtered = applyCurrentFilter(allWallpapersCache, newCategory, "")
        _state.value = _state.value.copy(wallpapers = filtered)
    }

    private fun applyCurrentFilter(
        source: List<Wallpaper>,
        category: String,
        query: String
    ): List<Wallpaper> {
        val cleanQuery = query.trim()
        if (cleanQuery.isNotEmpty()) {
            val normalizedQuery = CategoryNormalizer.normalize(cleanQuery)
            return source.filter { wallpaper ->
                wallpaper.title.contains(cleanQuery, ignoreCase = true) ||
                CategoryNormalizer.matches(wallpaper.category, cleanQuery) ||
                CategoryNormalizer.normalize(wallpaper.category).contains(normalizedQuery, ignoreCase = true)
            }
        }

        if (category.isNotEmpty()) {
            return source.filter { CategoryNormalizer.matches(it.category, category) }
        }

        return source
    }
}
