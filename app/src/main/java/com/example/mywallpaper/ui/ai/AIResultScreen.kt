package com.example.mywallpaper.ui.ai

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mywallpaper.ui.components.GradientButton
import com.example.mywallpaper.ui.components.GlassButton
import com.example.mywallpaper.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun decodeBase64ToBitmap(dataUri: String): Bitmap? {
    return try {
        val base64 = if (dataUri.contains(",")) dataUri.substringAfter(",") else dataUri
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) { null }
}

@Composable
fun AIResultScreen(
    viewModel: AICreateViewModel,
    onGenerateAgain: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val generatedWallpaper = (state.generationState as? AIGenerationState.Success)?.wallpaper
    val error = (state.generationState as? AIGenerationState.Error)?.message
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var isSetting by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            kotlinx.coroutines.delay(3000)
            snackbarMessage = null
        }
    }

    if (error != null) {
        Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Text("\uD83D\uDE22", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text("Generation Failed", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(error, color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(24.dp))
                GradientButton("Try Again", onClick = { viewModel.resetState(); onGenerateAgain() }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                GlassButton("Go Back", onClick = { viewModel.resetState(); onNavigateBack() }, modifier = Modifier.fillMaxWidth())
            }
        }
        return
    }

    if (generatedWallpaper == null) {
        onNavigateBack()
        return
    }

    // Build the image model - handle data URIs (base64) and regular URLs
    val imageModel = remember(generatedWallpaper.imageUrl) {
        val url = generatedWallpaper.imageUrl
        if (url.startsWith("data:")) {
            // Decode base64 to bitmap for Coil
            decodeBase64ToBitmap(url)
        } else {
            url
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageModel)
                .crossfade(true)
                .build(),
            contentDescription = "Generated wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlays
        Box(modifier = Modifier.fillMaxSize().background(
            androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(Color.Black.copy(0.3f), Color.Transparent), endY = 200f)))
        Box(modifier = Modifier.fillMaxSize().background(
            androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(Color.Transparent, Color.Black.copy(0.85f)), startY = 800f)))

        // Back button
        IconButton(onClick = onNavigateBack,
            modifier = Modifier.padding(top = 48.dp, start = 16.dp).size(44.dp)
                .clip(CircleShape).background(Color.Black.copy(0.4f))) {
            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
        }

        // Made with AI badge
        Surface(modifier = Modifier.align(Alignment.TopEnd).padding(top = 56.dp, end = 20.dp),
            shape = RoundedCornerShape(20.dp), color = GradientViolet.copy(0.85f)) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Made with AI", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Snackbar
        if (snackbarMessage != null) {
            Surface(modifier = Modifier.align(Alignment.TopCenter)
                    .padding(start = 20.dp, end = 20.dp, top = 56.dp),
                shape = RoundedCornerShape(12.dp), color = SurfaceContainerHigh) {
                Text(snackbarMessage!!, color = Color.White, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
        }

        // Bottom actions
        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 20.dp, vertical = 32.dp)) {
            Text("Generated Wallpaper", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            if (generatedWallpaper.prompt.isNotEmpty()) {
                Text("\"${generatedWallpaper.prompt.take(80)}${if (generatedWallpaper.prompt.length > 80) "..." else ""}\"",
                    color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))

            // Share button
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(0.12f))
                    .clickable {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Created with MyWallpaper Studio AI! Prompt: ${generatedWallpaper.prompt}")
                        }
                        context.startActivity(Intent.createChooser(intent, "Share"))
                    }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Share, "Share", tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.height(12.dp))

            // Set wallpaper button
            GradientButton(
                text = if (isSetting) "Setting..." else "Set as Wallpaper",
                isLoading = isSetting,
                onClick = {
                    scope.launch {
                        isSetting = true
                        withContext(Dispatchers.IO) {
                            try {
                                val bitmap = decodeBase64ToBitmap(generatedWallpaper.imageUrl)
                                if (bitmap != null) {
                                    WallpaperManager.getInstance(context).setBitmap(bitmap)
                                    snackbarMessage = "✓ Wallpaper set successfully!"
                                } else {
                                    snackbarMessage = "Failed to decode image"
                                }
                            } catch (e: Exception) {
                                snackbarMessage = "Failed: ${e.message}"
                            }
                        }
                        isSetting = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            GlassButton("Generate Another", onClick = { viewModel.resetState(); onGenerateAgain() }, modifier = Modifier.fillMaxWidth())
        }
    }
}