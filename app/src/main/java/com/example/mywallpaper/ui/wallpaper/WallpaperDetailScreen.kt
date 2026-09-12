package com.example.mywallpaper.ui.wallpaper

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import com.example.mywallpaper.Wallpaper
import com.example.mywallpaper.data.util.CategoryNormalizer
import com.example.mywallpaper.ui.components.GradientButton
import com.example.mywallpaper.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetailScreen(
    wallpaper: Wallpaper,
    viewModel: WallpaperDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(wallpaper) {
        viewModel.setWallpaper(wallpaper)
    }

    LaunchedEffect(state.message) {
        if (state.message != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    /**
     * Memory-safe bitmap loading downsampled to screen resolution to prevent OutOfMemoryError.
     */
    suspend fun loadSafeBitmap(): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val displayMetrics = context.resources.displayMetrics
            val request = ImageRequest.Builder(context)
                .data(wallpaper.imageUrl)
                .size(displayMetrics.widthPixels, displayMetrics.heightPixels)
                .precision(Precision.INEXACT)
                .allowHardware(false)
                .build()

            val result = context.imageLoader.execute(request)
            if (result !is SuccessResult) return@withContext null
            val drawable = result.drawable
            val w = drawable.intrinsicWidth.coerceIn(1, displayMetrics.widthPixels)
            val h = drawable.intrinsicHeight.coerceIn(1, displayMetrics.heightPixels)
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp
        } catch (oom: OutOfMemoryError) {
            System.gc()
            null
        } catch (e: Exception) {
            null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        // Full-screen wallpaper preview
        AsyncImage(
            model = wallpaper.imageUrl,
            contentDescription = wallpaper.title.ifBlank { wallpaper.category },
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradient overlays
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent), endY = 200f))
        )
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)), startY = 800f))
        )

        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(top = 48.dp, start = 16.dp).size(44.dp)
                .clip(CircleShape).background(Color.Black.copy(alpha = 0.4f))
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        // Message banner
        if (state.message != null) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 56.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainerHigh
            ) {
                Text(state.message!!, color = Color.White, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
        }

        // Bottom actions
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = CategoryNormalizer.toDisplayName(wallpaper.category),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
            Text(
                text = wallpaper.title.ifBlank { "Beautiful Wallpaper" },
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))

            // Action row: Favorite, Share, Download
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Favorite
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { viewModel.toggleFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (state.isFavorite) HeartRed else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Share safely
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable {
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Check out this wallpaper on MyWallpaper Studio:\n${wallpaper.imageUrl}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Wallpaper"))
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, "No sharing app found", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to share wallpaper", Toast.LENGTH_SHORT).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(22.dp))
                }

                // Download safely
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable(enabled = !state.isDownloading) {
                            viewModel.downloadWallpaper(wallpaper.imageUrl)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isDownloading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }

            // Set wallpaper button
            GradientButton(
                text = if (state.isSetting) "Applying Wallpaper..." else "Set as Wallpaper",
                onClick = { if (!state.isSetting) showSetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                isLoading = state.isSetting,
                enabled = !state.isSetting
            )
        }
    }

    // Set wallpaper dialog (Home, Lock, Both)
    if (showSetDialog) {
        AlertDialog(
            onDismissRequest = { showSetDialog = false },
            containerColor = SurfaceContainerHigh,
            title = { Text("Set Wallpaper", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Where would you like to set this wallpaper?", color = TextSecondary) },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            showSetDialog = false
                            scope.launch {
                                val bitmap = loadSafeBitmap()
                                if (bitmap != null) {
                                    viewModel.setAsWallpaper(bitmap, WallpaperManager.FLAG_SYSTEM)
                                } else {
                                    Toast.makeText(context, "Failed to prepare image", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Home Screen", color = Primary)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        TextButton(
                            onClick = {
                                showSetDialog = false
                                scope.launch {
                                    val bitmap = loadSafeBitmap()
                                    if (bitmap != null) {
                                        viewModel.setAsWallpaper(bitmap, WallpaperManager.FLAG_LOCK)
                                    } else {
                                        Toast.makeText(context, "Failed to prepare image", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Lock Screen", color = Primary)
                        }

                        TextButton(
                            onClick = {
                                showSetDialog = false
                                scope.launch {
                                    val bitmap = loadSafeBitmap()
                                    if (bitmap != null) {
                                        viewModel.setAsWallpaper(
                                            bitmap,
                                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                                        )
                                    } else {
                                        Toast.makeText(context, "Failed to prepare image", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Both Screens", color = Primary)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
