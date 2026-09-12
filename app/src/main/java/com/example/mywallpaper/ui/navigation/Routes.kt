package com.example.mywallpaper.ui.navigation

import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    // Auth
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"

    // Top-Level Destinations
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val AI_CREATE = "ai_create"
    const val FAVORITES = "favorites"
    const val PROFILE = "profile"

    // Child Destinations
    const val CATEGORY = "category/{categoryName}"
    const val WALLPAPER_BY_ID = "wallpaper/{wallpaperId}"
    const val WALLPAPER_DETAIL = "wallpaper_detail/{imageUrl}/{category}"
    const val NOTIFICATIONS = "notifications"
    const val NOTIFICATION_SETTINGS = "notification_settings"
    const val SETTINGS = "settings"
    const val APPEARANCE = "appearance"
    const val PRIVACY_POLICY = "privacy_policy"
    const val ABOUT = "about"
    const val AI_GENERATING = "ai_generating"
    const val AI_RESULT = "ai_result"
    const val AI_HISTORY = "ai_history"

    fun category(categoryName: String): String {
        val encoded = try {
            URLEncoder.encode(categoryName, "UTF-8")
        } catch (_: Exception) {
            categoryName
        }
        return "category/$encoded"
    }

    fun wallpaperById(wallpaperId: String): String {
        return "wallpaper/$wallpaperId"
    }

    fun wallpaperDetail(imageUrl: String, category: String): String {
        val encodedUrl = try {
            URLEncoder.encode(imageUrl, "UTF-8")
        } catch (_: Exception) {
            imageUrl
        }
        val encodedCat = try {
            URLEncoder.encode(category, "UTF-8")
        } catch (_: Exception) {
            category
        }
        return "wallpaper_detail/$encodedUrl/$encodedCat"
    }
}
