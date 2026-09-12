package com.example.mywallpaper

import com.google.firebase.firestore.PropertyName

data class Wallpaper(
    val id: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val title: String = "",
    @get:PropertyName("isFeatured")
    @set:PropertyName("isFeatured")
    var isFeatured: Boolean = false,
    var isFavorite: Boolean = false
)
