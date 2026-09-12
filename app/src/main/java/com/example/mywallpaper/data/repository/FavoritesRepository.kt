package com.example.mywallpaper.data.repository

import android.content.Context
import android.util.Log
import com.example.mywallpaper.FavoriteStorage
import com.example.mywallpaper.Wallpaper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

private data class WallpaperItemWithTime(
    val wallpaper: Wallpaper,
    val time: Long
)

class FavoritesRepository(private val context: Context) {

    companion object {
        private const val TAG = "FavoritesRepository"
        private val db = FirebaseFirestore.getInstance()
        private val auth = FirebaseAuth.getInstance()
        private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        // Shared StateFlow across all repository instances for immediate UI synchronization
        private val _sharedFavorites = MutableStateFlow<List<Wallpaper>>(emptyList())
        val sharedFavorites: StateFlow<List<Wallpaper>> = _sharedFavorites.asStateFlow()

        private var activeListenerRegistration: ListenerRegistration? = null
        private var lastObservedUid: String? = null
        private var isInitialized = false

        private fun hashString(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }.take(32)
        }

        fun getFavoriteDocId(wallpaper: Wallpaper): String {
            if (wallpaper.id.isNotBlank()) {
                return wallpaper.id.replace("/", "_").replace(".", "_")
            }
            return hashString(wallpaper.imageUrl)
        }
    }

    val favorites: StateFlow<List<Wallpaper>> get() = sharedFavorites

    init {
        synchronized(FavoritesRepository::class.java) {
            if (!isInitialized) {
                // Attach real-time auth listener to auto-sync favorites when user logs in or out
                auth.addAuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        if (lastObservedUid != user.uid) {
                            lastObservedUid = user.uid
                            setupFirestoreListener(user.uid)
                        }
                    } else {
                        lastObservedUid = null
                        activeListenerRegistration?.remove()
                        activeListenerRegistration = null
                        // Fall back to local storage for guest
                        _sharedFavorites.value = FavoriteStorage.loadFavorites(context)
                    }
                }

                // Initial load
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    lastObservedUid = currentUser.uid
                    setupFirestoreListener(currentUser.uid)
                } else {
                    _sharedFavorites.value = FavoriteStorage.loadFavorites(context)
                }

                isInitialized = true
            }
        }
    }

    private fun setupFirestoreListener(uid: String) {
        activeListenerRegistration?.remove()
        activeListenerRegistration = null

        Log.d(TAG, "Setting up Firestore favorites listener for user: $uid")
        activeListenerRegistration = db.collection("users")
            .document(uid)
            .collection("favorites")
            .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for favorites: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        val imageUrl = doc.getString("imageUrl") ?: return@mapNotNull null
                        val wallpaperId = doc.getString("wallpaperId") ?: doc.id
                        val category = doc.getString("category") ?: ""
                        val title = doc.getString("title") ?: ""
                        val timestamp = doc.getTimestamp("createdAt")

                        WallpaperItemWithTime(
                            wallpaper = Wallpaper(
                                id = wallpaperId,
                                imageUrl = imageUrl,
                                category = category,
                                title = title,
                                isFavorite = true
                            ),
                            time = timestamp?.toDate()?.time ?: 0L
                        )
                    }
                    val sorted = items.sortedByDescending { it.time }.map { it.wallpaper }
                    _sharedFavorites.value = sorted
                    Log.d(TAG, "Loaded ${sorted.size} favorites from Firestore for user $uid")
                }
            }

        // Migrate any legacy local favorites on first sync
        migrateLegacyFavoritesIfPresent(uid)
    }

    private fun migrateLegacyFavoritesIfPresent(uid: String) {
        repositoryScope.launch {
            try {
                val localList = FavoriteStorage.loadFavorites(context)
                if (localList.isNotEmpty()) {
                    Log.d(TAG, "Migrating ${localList.size} legacy local favorites to Firestore for user: $uid")
                    val batch = db.batch()
                    for (item in localList) {
                        if (item.imageUrl.isBlank()) continue
                        val docId = getFavoriteDocId(item)
                        val docRef = db.collection("users")
                            .document(uid)
                            .collection("favorites")
                            .document(docId)

                        val data = hashMapOf(
                            "wallpaperId" to item.id,
                            "imageUrl" to item.imageUrl,
                            "category" to item.category,
                            "title" to item.title,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                        batch.set(docRef, data)
                    }
                    batch.commit().await()
                    // Clear local preferences once migrated to avoid redundant uploads
                    FavoriteStorage.saveFavorites(context, ArrayList())
                    Log.d(TAG, "Legacy favorites migration completed successfully")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Legacy favorites migration encountered an error: ${e.message}")
            }
        }
    }

    fun isFavorite(wallpaper: Wallpaper): Boolean {
        val currentList = _sharedFavorites.value
        return currentList.any { fav ->
            (wallpaper.id.isNotBlank() && fav.id == wallpaper.id) ||
            (wallpaper.imageUrl.isNotBlank() && fav.imageUrl == wallpaper.imageUrl)
        }
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        val isCurrentlyFav = isFavorite(wallpaper)
        val current = _sharedFavorites.value.toMutableList()
        val docId = getFavoriteDocId(wallpaper)

        if (isCurrentlyFav) {
            current.removeAll { fav ->
                (wallpaper.id.isNotBlank() && fav.id == wallpaper.id) ||
                (wallpaper.imageUrl.isNotBlank() && fav.imageUrl == wallpaper.imageUrl)
            }
        } else {
            current.add(0, wallpaper.copy(isFavorite = true))
        }
        _sharedFavorites.value = current

        val uid = auth.currentUser?.uid
        if (uid != null) {
            // Persist change to Firestore asynchronously
            repositoryScope.launch {
                try {
                    val docRef = db.collection("users")
                        .document(uid)
                        .collection("favorites")
                        .document(docId)

                    if (isCurrentlyFav) {
                        docRef.delete().await()
                        Log.d(TAG, "Removed favorite from Firestore: $docId")
                    } else {
                        val data = hashMapOf(
                            "wallpaperId" to wallpaper.id,
                            "imageUrl" to wallpaper.imageUrl,
                            "category" to wallpaper.category,
                            "title" to wallpaper.title,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                        docRef.set(data).await()
                        Log.d(TAG, "Saved favorite to Firestore: $docId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update favorite in Firestore: ${e.message}", e)
                }
            }
        } else {
            // Guest mode fallback to SharedPreferences
            FavoriteStorage.saveFavorites(context, ArrayList(current))
        }
    }

    fun getFavorites(): List<Wallpaper> = _sharedFavorites.value
}
