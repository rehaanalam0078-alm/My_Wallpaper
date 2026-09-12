package com.example.mywallpaper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.mywallpaper.notifications.NotificationChannelHelper
import com.example.mywallpaper.ui.navigation.AppNavGraph
import com.example.mywallpaper.ui.theme.WallpaperStudioTheme

class MainActivity : ComponentActivity() {

    private var pendingWallpaperId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationChannelHelper.createNotificationChannel(this)

        handleIncomingIntent(intent)

        enableEdgeToEdge()
        setContent {
            WallpaperStudioTheme {
                AppNavGraph(
                    pendingWallpaperId = pendingWallpaperId,
                    onClearPendingWallpaperId = { pendingWallpaperId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        // 1. Check direct intent extras (from FCM notification payload or local notification)
        val wallpaperIdExtra = intent.getStringExtra("wallpaperId")
        if (!wallpaperIdExtra.isNullOrBlank()) {
            pendingWallpaperId = wallpaperIdExtra
            return
        }

        // 2. Check deep-link URI: mywallpaper://wallpaper/{wallpaperId}
        val data = intent.data
        if (data != null && data.scheme == "mywallpaper" && data.host == "wallpaper") {
            val pathId = data.lastPathSegment
            if (!pathId.isNullOrBlank()) {
                pendingWallpaperId = pathId
            }
        }
    }
}
