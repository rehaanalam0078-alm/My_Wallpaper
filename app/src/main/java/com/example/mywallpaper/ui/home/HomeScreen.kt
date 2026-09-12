package com.example.mywallpaper.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mywallpaper.Wallpaper
import com.example.mywallpaper.data.repository.FavoritesRepository
import androidx.compose.material.icons.outlined.Notifications
import com.example.mywallpaper.data.util.CategoryNormalizer
import com.example.mywallpaper.ui.components.*
import com.example.mywallpaper.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    unreadCount: Int = 0,
    onNotificationsClick: () -> Unit = {},
    onWallpaperClick: (Wallpaper) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onAICreateClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val favRepo = remember { FavoritesRepository(context) }
    val favorites by favRepo.favorites.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val greeting = when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
    val displayName = user?.displayName?.split(" ")?.firstOrNull() ?: "there"
    var homeSearchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            state.error != null -> ErrorState(
                message = state.error!!,
                onRetry = { viewModel.loadData() },
                modifier = Modifier.fillMaxSize()
            )
            else -> {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    // Header with Greeting and Notification Bell
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 56.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("$greeting, $displayName \uD83D\uDC4B", color = TextSecondary, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Discover Wallpapers", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }

                        // Notification bell button with unread count badge
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                                .clickable(onClick = onNotificationsClick),
                            contentAlignment = Alignment.Center
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = PrimaryContainer,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Live Interactive Search bar accepting keyboard input
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                        SearchBar(
                            query = homeSearchQuery,
                            onQueryChange = { homeSearchQuery = it },
                            placeholder = "Search wallpapers...",
                            onSearch = {
                                if (homeSearchQuery.isNotBlank()) {
                                    val queryToSend = homeSearchQuery.trim()
                                    homeSearchQuery = "" // Clear after navigation
                                    onSearchSubmit(queryToSend)
                                }
                            }
                        )
                    }

                    // Featured Hero (marked isFeatured or first available wallpaper)
                    val hero = state.featured ?: state.trending.firstOrNull()
                    if (hero != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(260.dp)
                                .clip(ShapeHero)
                                .clickable { onWallpaperClick(hero) }
                        ) {
                            AsyncImage(
                                model = hero.imageUrl,
                                contentDescription = "Featured wallpaper",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                            )
                            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                                Surface(shape = RoundedCornerShape(20.dp), color = Primary.copy(alpha = 0.9f)) {
                                    Text(
                                        "Featured", color = Color.White, fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    CategoryNormalizer.toDisplayName(hero.category),
                                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                    }

                    // Categories
                    Text(
                        "Browse Categories", color = Color.White, fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.categories) { category ->
                            CategoryChip(
                                label = CategoryNormalizer.toDisplayName(category),
                                selected = false,
                                onClick = { onCategoryClick(category) }
                            )
                        }
                    }
                    Spacer(Modifier.height(28.dp))

                    // Trending wallpapers row
                    Text(
                        "Trending Now", color = Color.White, fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.trending.take(10), key = { it.imageUrl }) { wallpaper ->
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(200.dp)
                                    .clip(ShapeCard)
                                    .clickable { onWallpaperClick(wallpaper) }
                            ) {
                                AsyncImage(
                                    model = wallpaper.imageUrl,
                                    contentDescription = wallpaper.category,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Heart overlay
                                val isFav = favorites.any { it.imageUrl == wallpaper.imageUrl }
                                Box(
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                                        .size(32.dp).clip(RoundedCornerShape(50))
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .clickable { favRepo.toggleFavorite(wallpaper) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFav) HeartRed else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(28.dp))

                    // AI Promo Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(ShapeCard)
                            .background(Brush.horizontalGradient(colors = listOf(GradientViolet, GradientCyan)))
                            .clickable { onAICreateClick() }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("AI Wallpaper Studio", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("Create wallpapers from your imagination", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                                Spacer(Modifier.height(12.dp))
                                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                                    Text(
                                        "Try Now ✦", color = Color.White, fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(48.dp))
                        }
                    }
                    Spacer(Modifier.height(28.dp))

                    // Latest section
                    if (state.latest.isNotEmpty()) {
                        Text(
                            "Latest Wallpapers", color = Color.White, fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        // 2-column grid
                        val chunked = state.latest.chunked(2)
                        chunked.forEach { pair ->
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { wallpaper ->
                                    WallpaperCard(
                                        wallpaper = wallpaper,
                                        isFavorite = favorites.any { it.imageUrl == wallpaper.imageUrl },
                                        onClick = { onWallpaperClick(wallpaper) },
                                        onFavoriteClick = { favRepo.toggleFavorite(wallpaper) },
                                        modifier = Modifier.weight(1f).aspectRatio(9f / 14f)
                                    )
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // Bottom padding for nav bar
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}
