package com.example.mywallpaper.data.model

data class AIWallpaper(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val prompt: String = "",
    val enhancedPrompt: String = "",
    val style: String = "",
    val mood: String = "",
    val color: String = "",
    val format: String = "Phone",
    val quality: String = "Standard",
    val createdAt: Long = System.currentTimeMillis(),
    var isFavorite: Boolean = false
)
