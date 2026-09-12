package com.example.mywallpaper.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywallpaper.ui.theme.*

@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) PrimaryContainer else ChipBackground,
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else TextSecondary,
        label = "chipText"
    )

    Box(
        modifier = modifier
            .clip(ShapeChip)
            .background(bgColor)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) Color.Transparent else OutlineVariant,
                shape = ShapeChip
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.replaceFirstChar { it.uppercase() },
            color = textColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}
