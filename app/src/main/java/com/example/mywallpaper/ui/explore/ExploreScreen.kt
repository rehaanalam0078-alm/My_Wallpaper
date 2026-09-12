package com.example.mywallpaper.ui.explore

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywallpaper.Wallpaper
import com.example.mywallpaper.data.repository.FavoritesRepository
import com.example.mywallpaper.data.util.CategoryNormalizer
import com.example.mywallpaper.ui.components.*
import com.example.mywallpaper.ui.theme.*

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onWallpaperClick: (Wallpaper) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val favRepo = remember { FavoritesRepository(context) }
    val favorites by favRepo.favorites.collectAsState()

    val gridState = rememberLazyStaggeredGridState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val isScrolled by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 24
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        // Sticky / Collapsing Header container with glassmorphism when scrolled
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isScrolled) {
                        Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xF20F131C),
                                        Color(0xEA121622),
                                        Color(0xD9121622)
                                    )
                                )
                            )
                            .padding(bottom = 10.dp)
                    } else {
                        Modifier.background(Color.Transparent)
                    }
                )
                .statusBarsPadding()
        ) {
            // Title and Subtitle - collapses when scrolled up
            AnimatedVisibility(
                visible = !isScrolled,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 4.dp)
                ) {
                    Text("Explore", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Discover wallpapers by category", color = TextSecondary, fontSize = 14.sp)
                }
            }

            // Search Bar - ALWAYS present at top, gains glassmorphism on scroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = if (isScrolled) 8.dp else 12.dp)
            ) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.updateSearch(it) },
                    placeholder = "Search wallpapers, categories...",
                    onSearch = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    glassmorphic = isScrolled
                )
            }

            // Category chips - collapses when scrolled up, reappears on scroll down to top
            AnimatedVisibility(
                visible = !isScrolled,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val isAllSelected = state.selectedCategory.isEmpty() && state.searchQuery.isEmpty()
                            CategoryChip(
                                label = "All",
                                selected = isAllSelected,
                                onClick = { viewModel.selectCategory("") }
                            )
                        }
                        items(state.categories) { category ->
                            val isSelected = CategoryNormalizer.matches(category, state.selectedCategory) && state.searchQuery.isEmpty()
                            CategoryChip(
                                label = CategoryNormalizer.toDisplayName(category),
                                selected = isSelected,
                                onClick = { viewModel.selectCategory(category) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // Results area
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
                state.error != null -> ErrorState(
                    message = state.error!!,
                    onRetry = { viewModel.loadAll(forceRefresh = true) },
                    modifier = Modifier.fillMaxSize()
                )
                state.wallpapers.isEmpty() -> EmptyState(
                    title = "No wallpapers found",
                    subtitle = if (state.searchQuery.isNotEmpty()) "Try a different search term" else "No wallpapers available in this category",
                    modifier = Modifier.fillMaxSize()
                )
                else -> StaggeredWallpaperGrid(
                    wallpapers = state.wallpapers,
                    favorites = favorites,
                    onWallpaperClick = onWallpaperClick,
                    onFavoriteClick = { favRepo.toggleFavorite(it) },
                    state = gridState,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
