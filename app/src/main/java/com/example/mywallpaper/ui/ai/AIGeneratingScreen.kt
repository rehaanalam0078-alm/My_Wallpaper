package com.example.mywallpaper.ui.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywallpaper.ui.theme.*
import kotlinx.coroutines.delay

private val messages = listOf(
    "Composing the scene...",
    "Balancing colors...",
    "Adding cinematic lighting...",
    "Applying your style...",
    "Finalizing details...",
    "Almost there..."
)

@Composable
fun AIGeneratingScreen(viewModel: AICreateViewModel, onResult: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var currentMessage by remember { mutableStateOf(messages[0]) }
    var messageIndex by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            messageIndex = (messageIndex + 1) % messages.size
            currentMessage = messages[messageIndex]
        }
    }

    LaunchedEffect(state.generationState) {
        if (state.generationState is AIGenerationState.Success || state.generationState is AIGenerationState.Error) {
            onResult()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Background, Color(0xFF0A0C10)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Animated gradient ring
            Box(
                modifier = Modifier.size(120.dp)
                    .rotate(rotation)
                    .background(
                        Brush.sweepGradient(listOf(GradientViolet, GradientCyan, Color.Transparent)),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(100.dp).background(Background, shape = RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center) {
                    Text("✦", fontSize = 36.sp, color = Primary)
                }
            }
            Spacer(Modifier.height(32.dp))
            Text("Creating your wallpaper...", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Turning your idea into something unique.", color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Text(currentMessage, color = Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
