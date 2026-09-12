package com.example.mywallpaper.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywallpaper.ui.theme.Background
import com.example.mywallpaper.ui.theme.GradientCyan
import com.example.mywallpaper.ui.theme.GradientViolet
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isLoggedIn: Boolean,
    onNavigateToHome: () -> Unit,
    onNavigateToWelcome: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoAlpha"
    )

    LaunchedEffect(Unit) {
        delay(1800)
        if (isLoggedIn) onNavigateToHome() else onNavigateToWelcome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo gradient text
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.radialGradient(colors = listOf(GradientViolet, GradientCyan)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("W", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "MyWallpaper Studio",
                color = Color.White.copy(alpha = alpha),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Discover. Create. Set.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}
