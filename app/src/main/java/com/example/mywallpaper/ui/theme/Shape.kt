package com.example.mywallpaper.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val WallpaperStudioShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Named tokens for easy reference
val ShapeCard = RoundedCornerShape(20.dp)
val ShapeHero = RoundedCornerShape(24.dp)
val ShapeBottomSheet = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
val ShapeChip = RoundedCornerShape(50)
val ShapeButton = RoundedCornerShape(50)
val ShapeInput = RoundedCornerShape(16.dp)
