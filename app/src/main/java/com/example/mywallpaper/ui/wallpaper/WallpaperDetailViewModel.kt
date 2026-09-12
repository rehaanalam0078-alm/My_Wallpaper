package com.example.mywallpaper.ui.wallpaper

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywallpaper.Wallpaper
import com.example.mywallpaper.data.repository.FavoritesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

data class WallpaperDetailUiState(
    val wallpaper: Wallpaper? = null,
    val isFavorite: Boolean = false,
    val isDownloading: Boolean = false,
    val isSetting: Boolean = false,
    val message: String? = null
)

class WallpaperDetailViewModel(private val context: Context) : ViewModel() {

    private val favoritesRepo = FavoritesRepository(context)
    private val TAG = "WallpaperDetailVM"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow(WallpaperDetailUiState())
    val state: StateFlow<WallpaperDetailUiState> = _state.asStateFlow()

    fun setWallpaper(wallpaper: Wallpaper) {
        val isFav = favoritesRepo.isFavorite(wallpaper)
        _state.value = WallpaperDetailUiState(wallpaper = wallpaper, isFavorite = isFav)
    }

    fun toggleFavorite() {
        val wallpaper = _state.value.wallpaper ?: return
        favoritesRepo.toggleFavorite(wallpaper)
        _state.value = _state.value.copy(isFavorite = favoritesRepo.isFavorite(wallpaper))
    }

    /**
     * Downloads wallpaper using modern MediaStore scoped storage (Android 10+) or legacy storage (< API 29).
     * Streams network bytes directly to disk without loading full resolution bitmaps into RAM.
     */
    fun downloadWallpaper(imageUrl: String) {
        if (_state.value.isDownloading) return // Prevent duplicate taps

        viewModelScope.launch {
            _state.value = _state.value.copy(isDownloading = true)
            try {
                withContext(Dispatchers.IO) {
                    val filename = "wallpaper_${System.currentTimeMillis()}.jpg"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Modern Android 10+ MediaStore scoped storage
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyWallpaper")
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }

                        val resolver = context.contentResolver
                        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                            ?: throw Exception("Failed to create MediaStore entry")

                        try {
                            resolver.openOutputStream(uri)?.use { outputStream ->
                                streamUrlToOutputStream(imageUrl, outputStream)
                            } ?: throw Exception("Failed to open output stream")

                            // Release pending status
                            contentValues.clear()
                            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            resolver.update(uri, contentValues, null, null)
                        } catch (e: Exception) {
                            resolver.delete(uri, null, null)
                            throw e
                        }
                    } else {
                        // Legacy Android (< API 29)
                        val folder = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "MyWallpaper"
                        )
                        if (!folder.exists()) folder.mkdirs()
                        val file = File(folder, filename)
                        FileOutputStream(file).use { outputStream ->
                            streamUrlToOutputStream(imageUrl, outputStream)
                        }
                    }
                }
                _state.value = _state.value.copy(isDownloading = false, message = "Saved to gallery!")
            } catch (e: Exception) {
                Log.e(TAG, "Download error: ${e.message}", e)
                _state.value = _state.value.copy(isDownloading = false, message = "Download failed: ${e.message}")
            }
        }
    }

    private fun streamUrlToOutputStream(url: String, outputStream: OutputStream) {
        if (url.startsWith("data:")) {
            // Base64 data URI
            val base64 = if (url.contains(",")) url.substringAfter(",") else url
            val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            outputStream.write(bytes)
            outputStream.flush()
            return
        }

        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Server returned HTTP ${response.code}")
        }
        val body = response.body ?: throw Exception("Empty response body")
        body.byteStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
        }
    }

    /**
     * Sets wallpaper safely using WallpaperManager with specific mode flags on Dispatchers.IO.
     */
    fun setAsWallpaper(bitmap: Bitmap, mode: Int) {
        if (_state.value.isSetting) return // Prevent duplicate taps

        viewModelScope.launch {
            _state.value = _state.value.copy(isSetting = true)
            try {
                withContext(Dispatchers.IO) {
                    val wm = WallpaperManager.getInstance(context)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wm.setBitmap(bitmap, null, true, mode)
                    } else {
                        wm.setBitmap(bitmap)
                    }
                }
                val modeLabel = when (mode) {
                    WallpaperManager.FLAG_LOCK -> "Lock screen"
                    WallpaperManager.FLAG_SYSTEM -> "Home screen"
                    else -> "Home and Lock screen"
                }
                _state.value = _state.value.copy(isSetting = false, message = "$modeLabel wallpaper updated!")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set wallpaper: ${e.message}", e)
                _state.value = _state.value.copy(isSetting = false, message = "Failed to set wallpaper: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
