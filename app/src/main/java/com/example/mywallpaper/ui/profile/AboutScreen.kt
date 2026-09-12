package com.example.mywallpaper.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywallpaper.R
import com.example.mywallpaper.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var aboutText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val loaded = try {
            context.assets.open("About.txt").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            try {
                context.assets.open("about.txt").bufferedReader().use { it.readText() }
            } catch (_: Exception) {
                ""
            }
        }
        aboutText = loaded.trim()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // App Icon
            Image(
                painter = painterResource(id = R.drawable.mywallpapericon),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(22.dp))
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "MyWallpaper Studio",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Version 1.0.0",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(24.dp))

            if (aboutText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainer)
                        .padding(20.dp)
                ) {
                    Text(
                        text = aboutText,
                        color = OnSurface,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainer)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "About information will appear here once provided in About.txt.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
