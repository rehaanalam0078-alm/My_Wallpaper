package com.example.mywallpaper.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mywallpaper.MainActivity
import com.example.mywallpaper.R
import com.example.mywallpaper.data.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val notifRepo = NotificationRepository()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Token: $token")
        scope.launch {
            try {
                notifRepo.registerDeviceToken()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register new token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notif = remoteMessage.notification

        val type = data["type"] ?: "NEW_WALLPAPER"
        val wallpaperId = data["wallpaperId"] ?: ""
        val category = data["category"] ?: ""
        val imageUrl = data["imageUrl"] ?: notif?.imageUrl?.toString() ?: ""
        val title = data["title"] ?: notif?.title ?: "New wallpaper"
        val body = data["body"] ?: notif?.body ?: "A new wallpaper is available."

        Log.d(TAG, "Message received - type: $type, wallpaperId: $wallpaperId, title: $title")

        scope.launch {
            showNotification(
                type = type,
                wallpaperId = wallpaperId,
                category = category,
                imageUrl = imageUrl,
                title = title,
                body = body
            )
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun showNotification(
        type: String,
        wallpaperId: String,
        category: String,
        imageUrl: String,
        title: String,
        body: String
    ) {
        NotificationChannelHelper.createNotificationChannel(applicationContext)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("type", type)
            putExtra("wallpaperId", wallpaperId)
            putExtra("category", category)
            putExtra("imageUrl", imageUrl)
        }

        val notifId = if (wallpaperId.isNotBlank()) wallpaperId.hashCode() else System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bitmap = if (imageUrl.isNotBlank()) loadBitmap(imageUrl) else null

        val builder = NotificationCompat.Builder(applicationContext, NotificationChannelHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (bitmap != null) {
            builder.setLargeIcon(bitmap)
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .setSummaryText(body)
            )
        }

        try {
            val notificationManager = NotificationManagerCompat.from(applicationContext)
            notificationManager.notify(notifId, builder.build())
        } catch (e: SecurityException) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying notification: ${e.message}", e)
        }
    }

    private suspend fun loadBitmap(urlStr: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load notification big picture: ${e.message}")
            null
        }
    }
}
