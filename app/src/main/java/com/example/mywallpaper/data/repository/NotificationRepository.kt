package com.example.mywallpaper.data.repository

import android.os.Build
import android.util.Log
import com.example.mywallpaper.data.model.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class NotificationRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "NotificationRepo"

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Flow of user's notifications, kept in sync via Firestore real-time snapshots.
     */
    fun getNotificationsFlow(): Flow<List<NotificationItem>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection("users")
            .document(uid)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for notifications: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(NotificationItem::class.java)?.copy(notificationId = doc.id)
                    }
                    trySend(items)
                }
            }

        awaitClose { registration.remove() }
    }

    /**
     * Flow of unread notifications count for the top bell badge.
     */
    fun getUnreadCountFlow(): Flow<Int> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection("users")
            .document(uid)
            .collection("notifications")
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for unread count: ${error.message}")
                    return@addSnapshotListener
                }
                val count = snapshot?.size() ?: 0
                trySend(count)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Marks a single notification as read in Firestore.
     */
    suspend fun markAsRead(notificationId: String) = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        try {
            db.collection("users")
                .document(uid)
                .collection("notifications")
                .document(notificationId)
                .update("read", true)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark notification $notificationId as read: ${e.message}", e)
        }
    }

    /**
     * Marks all unread notifications as read via batch write.
     */
    suspend fun markAllAsRead() = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        try {
            val unreadDocs = db.collection("users")
                .document(uid)
                .collection("notifications")
                .whereEqualTo("read", false)
                .get()
                .await()

            if (unreadDocs.isEmpty) return@withContext

            val batch = db.batch()
            for (doc in unreadDocs.documents) {
                batch.update(doc.reference, "read", true)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark all notifications as read: ${e.message}", e)
        }
    }

    /**
     * Deletes a single notification record.
     */
    suspend fun deleteNotification(notificationId: String) = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        try {
            db.collection("users")
                .document(uid)
                .collection("notifications")
                .document(notificationId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete notification $notificationId: ${e.message}", e)
        }
    }

    /**
     * Clears all notification records for this user.
     */
    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        try {
            val allDocs = db.collection("users")
                .document(uid)
                .collection("notifications")
                .get()
                .await()

            if (allDocs.isEmpty) return@withContext

            val batch = db.batch()
            for (doc in allDocs.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all notifications: ${e.message}", e)
        }
    }

    /**
     * Retrieves the user's notification preference (default: true).
     */
    suspend fun isNewWallpapersEnabled(): Boolean = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext true
        try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val settings = doc.get("notificationSettings") as? Map<*, *>
                val newWallpapers = settings?.get("newWallpapers") as? Boolean
                return@withContext newWallpapers ?: true
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get notification settings: ${e.message}")
            true
        }
    }

    /**
     * Updates the user's notification preference in Firestore.
     */
    suspend fun setNewWallpapersEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        try {
            val update = mapOf(
                "notificationSettings" to mapOf(
                    "newWallpapers" to enabled
                )
            )
            db.collection("users").document(uid).set(update, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification settings: ${e.message}", e)
            throw e
        }
    }

    /**
     * Registers or updates this device's FCM token under users/{userId}/fcmTokens/{tokenId}.
     */
    suspend fun registerDeviceToken() = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            if (token.isNullOrBlank()) return@withContext

            val tokenDocId = hashToken(token)
            val tokenData = hashMapOf(
                "token" to token,
                "platform" to "android",
                "deviceModel" to "${Build.MANUFACTURER} ${Build.MODEL}",
                "appVersion" to "1.0",
                "updatedAt" to FieldValue.serverTimestamp()
            )

            // Use merge to preserve original createdAt if it already exists
            db.collection("users")
                .document(uid)
                .collection("fcmTokens")
                .document(tokenDocId)
                .set(tokenData, SetOptions.merge())
                .await()

            Log.d(TAG, "FCM device token registered successfully for user: $uid")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register FCM token: ${e.message}")
        }
    }

    /**
     * Removes this device's FCM token on user logout.
     */
    suspend fun unregisterDeviceToken() = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            if (token.isNullOrBlank()) return@withContext

            val tokenDocId = hashToken(token)
            db.collection("users")
                .document(uid)
                .collection("fcmTokens")
                .document(tokenDocId)
                .delete()
                .await()

            Log.d(TAG, "FCM device token unregistered for user: $uid")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unregister FCM token: ${e.message}")
        }
    }

    /**
     * Produces a clean alphanumeric ID from the token for safe Firestore document naming.
     */
    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }.take(32)
    }
}
