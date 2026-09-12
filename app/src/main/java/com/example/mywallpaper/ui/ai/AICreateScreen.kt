package com.example.mywallpaper.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywallpaper.ui.components.CategoryChip
import com.example.mywallpaper.ui.components.GradientButton
import com.example.mywallpaper.ui.components.GlassButton
import com.example.mywallpaper.ui.theme.*

@Composable
fun AICreateScreen(
    viewModel: AICreateViewModel,
    onGenerationStarted: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.generationState) {
        when (state.generationState) {
            is AIGenerationState.Generating -> onGenerationStarted()
            else -> {}
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState())) {
        // Header
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 56.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("AI Studio", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("Describe the wallpaper you imagine", color = TextSecondary, fontSize = 13.sp)
                }
                TextButton(onClick = onNavigateToHistory) {
                    Text("History", color = Primary, fontSize = 13.sp)
                }
            }
        }

        // Prompt input box
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerHigh)
                    .padding(16.dp)
            ) {
                Column {
                    BasicTextField(
                        value = state.prompt,
                        onValueChange = { viewModel.updatePrompt(it) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                        cursorBrush = SolidColor(Primary),
                        decorationBox = { innerTextField ->
                            if (state.prompt.isEmpty()) {
                                Text(
                                    "Example: futuristic Tokyo at night, neon rain, cinematic lighting...",
                                    color = TextTertiary, fontSize = 15.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassButton(
                            text = "✦ Enhance",
                            onClick = { viewModel.enhancePrompt() },
                            modifier = Modifier.weight(1f),
                            enabled = state.generationState !is AIGenerationState.EnhancingPrompt
                        )
                        if (state.enhancedPrompt.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(8.dp), color = Primary.copy(alpha = 0.15f)) {
                                Text("Enhanced ✓", color = Primary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                            }
                        }
                    }
                    if (state.enhancedPrompt.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Enhanced: ${state.enhancedPrompt}", color = TextSecondary, fontSize = 12.sp, maxLines = 2)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        // Error state
        if (state.generationState is AIGenerationState.Error) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp), color = ErrorContainer.copy(alpha = 0.3f)
            ) {
                Text((state.generationState as AIGenerationState.Error).message,
                    color = ErrorColor, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))
        }

        // Enhancing prompt indicator
        if (state.generationState is AIGenerationState.EnhancingPrompt) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Enhancing your prompt...", color = TextSecondary, fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))
        }

        // Style selector
        SelectorSection(title = "Style", options = viewModel.styles, selected = state.selectedStyle, onSelect = { viewModel.updateStyle(it) })
        Spacer(Modifier.height(20.dp))

        // Mood selector
        SelectorSection(title = "Mood", options = viewModel.moods, selected = state.selectedMood, onSelect = { viewModel.updateMood(it) })
        Spacer(Modifier.height(20.dp))

        // Color selector
        SelectorSection(title = "Color", options = viewModel.colors, selected = state.selectedColor, onSelect = { viewModel.updateColor(it) })
        Spacer(Modifier.height(20.dp))

        // Format selector
        SelectorSection(title = "Format", options = viewModel.formats, selected = state.selectedFormat, onSelect = { viewModel.updateFormat(it) })
        Spacer(Modifier.height(20.dp))

        // Quality selector
        SelectorSection(title = "Quality", options = viewModel.qualities, selected = state.selectedQuality, onSelect = { viewModel.updateQuality(it) })
        Spacer(Modifier.height(32.dp))

        // Generate button
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            GradientButton(
                text = "Generate Wallpaper",
                onClick = { viewModel.generate() },
                modifier = Modifier.fillMaxWidth(),
                isLoading = state.generationState is AIGenerationState.Generating,
                enabled = state.generationState !is AIGenerationState.Generating &&
                          state.generationState !is AIGenerationState.EnhancingPrompt
            )
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun SelectorSection(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
        Spacer(Modifier.height(8.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option ->
                CategoryChip(label = option, selected = selected == option, onClick = { onSelect(option) })
            }
        }
    }
}
