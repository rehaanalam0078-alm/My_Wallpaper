package com.example.mywallpaper.data.repository

import android.util.Log
import com.example.mywallpaper.Wallpaper
import com.example.mywallpaper.data.util.CategoryNormalizer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class WallpaperRepository {

    private val db = FirebaseFirestore.getInstance()
    private val wallpapersCollection = db.collection("wallpapers")
    private val TAG = "WallpaperRepository"

    // Cache wallpapers in memory to prevent repeated full-collection reads
    private var cachedWallpapers: List<Wallpaper>? = null

    /**
     * Retrieves all available wallpapers without artificial limit.
     * Throws an exception on network/permission failure so callers can show ErrorState.
     */
    suspend fun getAllWallpapers(forceRefresh: Boolean = false): List<Wallpaper> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedWallpapers != null) {
            return@withContext cachedWallpapers!!
        }
        try {
            val snapshot = wallpapersCollection.get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Wallpaper::class.java)?.copy(id = doc.id)
            }
            cachedWallpapers = list
            list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load wallpapers: ${e.message}", e)
            throw e
        }
    }

    /**
     * Retrieves trending wallpapers.
     */
    suspend fun getTrending(limit: Long = 20): List<Wallpaper> = withContext(Dispatchers.IO) {
        try {
            val all = getAllWallpapers()
            all.take(limit.toInt())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load trending wallpapers: ${e.message}", e)
            throw e
        }
    }

    /**
     * Retrieves the featured hero wallpaper.
     * Prioritizes the wallpaper marked with isFeatured == true.
     * Falls back to the first trending wallpaper if none is explicitly marked.
     */
    suspend fun getFeaturedWallpaper(): Wallpaper? = withContext(Dispatchers.IO) {
        try {
            val all = getAllWallpapers()
            all.firstOrNull { it.isFeatured } ?: all.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load featured wallpaper: ${e.message}", e)
            null
        }
    }

    /**
     * Filters wallpapers by category, using canonical normalization (e.g., 'ainme' maps to 'anime').
     */
    suspend fun getWallpapersByCategory(category: String): List<Wallpaper> = withContext(Dispatchers.IO) {
        val all = getAllWallpapers()
        if (category.isBlank() || category.equals("all", ignoreCase = true)) {
            all
        } else {
            all.filter { CategoryNormalizer.matches(it.category, category) }
        }
    }

    /**
     * Performs a case-insensitive search across wallpaper title and category (including category aliases).
     */
    suspend fun search(query: String): List<Wallpaper> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return@withContext getAllWallpapers()
        }
        val all = getAllWallpapers()
        val normalizedQuery = CategoryNormalizer.normalize(cleanQuery)
        all.filter { wallpaper ->
            wallpaper.title.contains(cleanQuery, ignoreCase = true) ||
            CategoryNormalizer.matches(wallpaper.category, cleanQuery) ||
            CategoryNormalizer.normalize(wallpaper.category).contains(normalizedQuery, ignoreCase = true)
        }
    }

    /**
     * Dynamically derives available categories from actual Firestore wallpaper data.
     * Newly added categories appear automatically; categories with 0 wallpapers are excluded.
     */
    suspend fun getCategories(): List<String> = withContext(Dispatchers.IO) {
        try {
            val all = getAllWallpapers()
            val categories = CategoryNormalizer.extractCategories(all.map { it.category })
            if (categories.isNotEmpty()) {
                categories
            } else {
                // Fallback default list if no wallpapers exist yet
                listOf("anime", "cars", "nature", "islamic", "hinduism", "kitty", "minimal", "dark")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load categories: ${e.message}", e)
            throw e
        }
    }

    /**
     * Retrieves a single wallpaper by its Firestore document ID.
     * Returns null if not found or if an error occurs.
     */
    suspend fun getWallpaperById(id: String): Wallpaper? = withContext(Dispatchers.IO) {
        if (id.isBlank()) return@withContext null
        try {
            val doc = wallpapersCollection.document(id).get().await()
            if (doc.exists()) {
                doc.toObject(Wallpaper::class.java)?.copy(id = doc.id)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get wallpaper by id '$id': ${e.message}", e)
            null
        }
    }
}

