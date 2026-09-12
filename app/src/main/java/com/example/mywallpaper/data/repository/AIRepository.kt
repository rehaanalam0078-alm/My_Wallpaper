package com.example.mywallpaper.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.mywallpaper.data.model.AIGenerationRequest
import com.example.mywallpaper.data.model.AIGenerationResponse
import com.example.mywallpaper.data.model.AIWallpaper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.TimeUnit

class AIRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "AIRepository"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Gemini API base URL
    private val GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta"

    // Fetch dynamic configuration from Firebase Remote Config (NO hardcoded secret in client code)
    private suspend fun getApiKey(): String = withContext(Dispatchers.IO) {
        try {
            val rc = Firebase.remoteConfig
            val settings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0 // Instant fetch for live updates
            }
            rc.setConfigSettingsAsync(settings).await()
            rc.fetchAndActivate().await()
            val key = rc.getString("ai_api_key").trim()
            if (key.isNotBlank()) {
                Log.d(TAG, "Remote Config AI key retrieved successfully")
                key
            } else {
                Log.w(TAG, "Remote Config 'ai_api_key' is not configured")
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve configuration from Remote Config: ${e.message}")
            ""
        }
    }

    // Build a detailed prompt with style parameters
    private fun buildFullPrompt(req: AIGenerationRequest): String {
        val parts = mutableListOf(req.prompt)
        if (req.style.isNotBlank() && req.style != "Auto") parts.add("${req.style} style")
        if (req.mood.isNotBlank() && req.mood != "Auto") parts.add("${req.mood} atmosphere")
        if (req.color.isNotBlank() && req.color != "Auto") parts.add("${req.color} dominant colors")
        when (req.format) {
            "Phone" -> parts.add("portrait orientation, 9:16 mobile wallpaper")
            "Tablet" -> parts.add("4:3 tablet wallpaper")
            "Desktop" -> parts.add("16:9 widescreen wallpaper")
            else -> parts.add("phone wallpaper portrait")
        }
        when (req.quality) {
            "Ultra" -> parts.add("masterpiece, ultra high quality, 4K, 8K resolution, sharp focus, hyperdetailed")
            "High" -> parts.add("high quality, highly detailed, crisp")
            else -> parts.add("high quality")
        }
        parts.add("cinematic lighting, wallpaper aesthetics")
        return parts.joinToString(", ")
    }

    suspend fun generateWallpaper(request: AIGenerationRequest): Result<AIGenerationResponse> = withContext(Dispatchers.IO) {
        try {
            val fullPrompt = buildFullPrompt(request)
            Log.d(TAG, "Generating with prompt: $fullPrompt")

            val (width, height) = when (request.format) {
                "Tablet" -> Pair(1200, 900)
                "Desktop" -> Pair(1920, 1080)
                else -> Pair(720, 1280) // 9:16 portrait
            }

            val encodedPrompt = URLEncoder.encode(fullPrompt, "UTF-8")
            val seed = (System.currentTimeMillis() % 1000000).toInt()
            val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=$width&height=$height&nologo=true&seed=$seed"
            Log.d(TAG, "Generating image via: $imageUrl")

            val imageHttpRequest = Request.Builder()
                .url(imageUrl)
                .get()
                .addHeader("User-Agent", "MyWallpaperStudio/1.0")
                .build()

            val response = httpClient.newCall(imageHttpRequest).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Generation server returned status ${response.code}"))
            }

            val imageBytes = response.body?.bytes()
                ?: return@withContext Result.failure(Exception("No image received from generator"))

            if (imageBytes.isEmpty()) {
                return@withContext Result.failure(Exception("Received empty image data"))
            }

            val imageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val mimeType = "image/jpeg"
            val imageDataUri = "data:$mimeType;base64,$imageBase64"
            val generationId = UUID.randomUUID().toString()
            val userId = auth.currentUser?.uid

            // Cache to Firestore if user is authenticated
            if (!userId.isNullOrEmpty()) {
                try {
                    val wallpaperData = hashMapOf(
                        "id" to generationId,
                        "userId" to userId,
                        "imageUrl" to imageDataUri,
                        "prompt" to request.prompt,
                        "enhancedPrompt" to fullPrompt,
                        "style" to request.style,
                        "mood" to request.mood,
                        "color" to request.color,
                        "format" to request.format,
                        "quality" to request.quality,
                        "createdAt" to System.currentTimeMillis(),
                        "isFavorite" to false
                    )
                    db.collection("users").document(userId)
                        .collection("aiCreations").document(generationId)
                        .set(wallpaperData)
                        .await()
                    Log.d(TAG, "Saved generation $generationId to Firestore for user $userId")
                } catch (fsErr: Exception) {
                    Log.w(TAG, "Firestore save skipped/failed: ${fsErr.message}")
                }
            }

            Result.success(
                AIGenerationResponse(
                    imageUrl = imageDataUri,
                    generationId = generationId,
                    enhancedPrompt = fullPrompt
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Generate error: ${e.message}", e)
            Result.failure(Exception(e.message ?: "Generation failed. Please check your network and try again."))
        }
    }

    suspend fun enhancePrompt(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("AI prompt enhancement is not configured. Please try generating directly."))
            }

            val systemText = "You are a creative AI art director specializing in mobile phone wallpapers. " +
                "Transform the user's description into a rich, cinematic, visually stunning wallpaper prompt. " +
                "Include lighting, atmosphere, textures, colors, depth, and mood. " +
                "Return ONLY the enhanced prompt in a single paragraph. Do not include quotes, markdown formatting, or explanations. Max 60 words."

            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemText\n\nUser description: $prompt")
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.8)
                    put("maxOutputTokens", 120)
                })
            }.toString()

            val httpRequest = Request.Builder()
                .url("$GEMINI_BASE/models/gemini-flash-latest:generateContent?key=$apiKey")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()

            val response = httpClient.newCall(httpRequest).execute()
            val responseBody = response.body?.string() ?: ""
            Log.d(TAG, "Gemini enhance response code: ${response.code}")

            if (!response.isSuccessful) {
                Log.e(TAG, "Enhance API returned ${response.code}: $responseBody")
                return@withContext Result.failure(Exception("Enhancement error (${response.code})"))
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val enhancedText = parts?.optJSONObject(0)?.optString("text")?.trim()

            if (!enhancedText.isNullOrBlank()) {
                Log.d(TAG, "Enhanced prompt: $enhancedText")
                Result.success(enhancedText)
            } else {
                Result.success(prompt)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Enhance error: ${e.message}", e)
            Result.failure(Exception(e.message ?: "Failed to enhance prompt"))
        }
    }

    suspend fun getHistory(userId: String): List<AIWallpaper> = withContext(Dispatchers.IO) {
        try {
            val result = db.collection("users").document(userId)
                .collection("aiCreations")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            result.documents.mapNotNull { doc ->
                try {
                    AIWallpaper(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        prompt = doc.getString("prompt") ?: "",
                        enhancedPrompt = doc.getString("enhancedPrompt") ?: "",
                        style = doc.getString("style") ?: "",
                        mood = doc.getString("mood") ?: "",
                        color = doc.getString("color") ?: "",
                        format = doc.getString("format") ?: "Phone",
                        quality = doc.getString("quality") ?: "Standard",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        isFavorite = doc.getBoolean("isFavorite") ?: false
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveGeneration(userId: String, wallpaper: AIWallpaper): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.collection("users").document(userId)
                .collection("aiCreations")
                .document(wallpaper.id.ifEmpty { UUID.randomUUID().toString() })
                .set(wallpaper)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}