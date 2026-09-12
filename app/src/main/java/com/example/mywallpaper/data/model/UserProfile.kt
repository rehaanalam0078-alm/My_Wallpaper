package com.example.mywallpaper.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val favoritesCount: Int = 0,
    val aiCreationsCount: Int = 0,
    val downloadsCount: Int = 0
)
