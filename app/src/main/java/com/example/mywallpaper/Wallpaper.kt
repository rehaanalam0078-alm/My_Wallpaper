package com.example.mywallpaper

import android.media.Image

data class Wallpaper(
    val imageUrl: String= "",
    val category: String= "",
    var isFavorite: Boolean = false
)
