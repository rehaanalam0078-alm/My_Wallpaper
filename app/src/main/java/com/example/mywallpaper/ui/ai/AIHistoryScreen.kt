package com.example.mywallpaper.ui.ai

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mywallpaper.ui.components.EmptyState
import com.example.mywallpaper.ui.theme.*

@Composable
fun SmartImage(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val model = remember(url) {
        if (url.startsWith("data:")) {
            try {
                val base64 = url.substringAfter(",")
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) { null }
        } else url
    }
    AsyncImage(
        model = ImageRequest.Builder(context).data(model).crossfade(true).build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@Composable
fun AIHistoryScreen(viewModel: AICreateViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadHistory() }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 48.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Column {
                Text("My Creations", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${state.history.size} wallpapers generated", color = TextSecondary, fontSize = 13.sp)
            }
        }
        when {
            state.isLoadingHistory -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            state.history.isEmpty() -> EmptyState(
                title = "No creations yet",
                subtitle = "Your AI generated wallpapers will appear here",
                modifier = Modifier.fillMaxSize()
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.history, key = { it.id }) { item ->
                    Box(modifier = Modifier.aspectRatio(9f / 16f).clip(RoundedCornerShape(16.dp)).background(SurfaceContainerHigh)) {
                        SmartImage(url = item.imageUrl, modifier = Modifier.fillMaxSize())
                        Box(modifier = Modifier.fillMaxSize().background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
                        Text(item.prompt.take(40), color = Color.White, fontSize = 11.sp,
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
                    }
                }
            }
        }
    }
}