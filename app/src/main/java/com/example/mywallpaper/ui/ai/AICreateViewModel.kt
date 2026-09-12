package com.example.mywallpaper.ui.ai

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywallpaper.data.model.AIGenerationRequest
import com.example.mywallpaper.data.model.AIWallpaper
import com.example.mywallpaper.data.repository.AIRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AIGenerationState {
    object Idle : AIGenerationState()
    object EnhancingPrompt : AIGenerationState()
    object Generating : AIGenerationState()
    data class Success(val wallpaper: AIWallpaper) : AIGenerationState()
    data class Error(val message: String) : AIGenerationState()
}

data class AICreateUiState(
    val prompt: String = "",
    val enhancedPrompt: String = "",
    val selectedStyle: String = "Photorealistic",
    val selectedMood: String = "Calm",
    val selectedColor: String = "Auto",
    val selectedFormat: String = "Phone",
    val selectedQuality: String = "Standard",
    val generationState: AIGenerationState = AIGenerationState.Idle,
    val history: List<AIWallpaper> = emptyList(),
    val isLoadingHistory: Boolean = false
)

class AICreateViewModel : ViewModel() {

    private val repo = AIRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(AICreateUiState())
    val state: StateFlow<AICreateUiState> = _state.asStateFlow()

    val styles = listOf("Photorealistic", "Anime", "3D", "Cinematic", "Minimal",
                        "Cyberpunk", "Fantasy", "Illustration", "Watercolor", "Islamic Art")
    val moods = listOf("Calm", "Dark", "Dreamy", "Energetic", "Mysterious",
                       "Luxury", "Futuristic")
    val colors = listOf("Auto", "Black", "Blue", "Purple", "Red", "Green",
                        "Gold", "Monochrome")
    val formats = listOf("Phone", "Tablet", "Desktop")
    val qualities = listOf("Standard", "High", "Ultra")

    fun updatePrompt(text: String) { _state.value = _state.value.copy(prompt = text) }
    fun updateStyle(style: String) { _state.value = _state.value.copy(selectedStyle = style) }
    fun updateMood(mood: String) { _state.value = _state.value.copy(selectedMood = mood) }
    fun updateColor(color: String) { _state.value = _state.value.copy(selectedColor = color) }
    fun updateFormat(format: String) { _state.value = _state.value.copy(selectedFormat = format) }
    fun updateQuality(quality: String) { _state.value = _state.value.copy(selectedQuality = quality) }

    fun enhancePrompt() {
        val prompt = _state.value.prompt.trim()
        if (prompt.isBlank()) {
            _state.value = _state.value.copy(generationState = AIGenerationState.Error("Please enter a prompt first"))
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(generationState = AIGenerationState.EnhancingPrompt)
            val result = repo.enhancePrompt(prompt)
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    enhancedPrompt = result.getOrDefault(prompt),
                    generationState = AIGenerationState.Idle
                )
            } else {
                _state.value = _state.value.copy(
                    generationState = AIGenerationState.Error(result.exceptionOrNull()?.message ?: "Enhancement failed")
                )
            }
        }
    }

    fun generate() {
        val state = _state.value
        val prompt = state.enhancedPrompt.ifBlank { state.prompt }.trim()
        if (prompt.isBlank()) {
            _state.value = _state.value.copy(generationState = AIGenerationState.Error("Please enter a prompt"))
            return
        }
        if (prompt.length < 5) {
            _state.value = _state.value.copy(generationState = AIGenerationState.Error("Prompt too short. Please describe your wallpaper in more detail."))
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(generationState = AIGenerationState.Generating)
            val request = AIGenerationRequest(
                prompt = prompt,
                style = state.selectedStyle,
                mood = state.selectedMood,
                color = state.selectedColor,
                format = state.selectedFormat,
                quality = state.selectedQuality
            )
            val result = repo.generateWallpaper(request)
            if (result.isSuccess) {
                val response = result.getOrNull()!!
                val userId = auth.currentUser?.uid ?: ""
                val aiWallpaper = AIWallpaper(
                    id = response.generationId,
                    userId = userId,
                    imageUrl = response.imageUrl,
                    prompt = prompt,
                    enhancedPrompt = response.enhancedPrompt,
                    style = state.selectedStyle,
                    mood = state.selectedMood,
                    color = state.selectedColor,
                    format = state.selectedFormat,
                    quality = state.selectedQuality,
                    createdAt = System.currentTimeMillis()
                )
                if (userId.isNotEmpty()) {
                    repo.saveGeneration(userId, aiWallpaper)
                }
                _state.value = _state.value.copy(generationState = AIGenerationState.Success(aiWallpaper))
            } else {
                _state.value = _state.value.copy(
                    generationState = AIGenerationState.Error(
                        result.exceptionOrNull()?.message ?: "Generation failed. Please try again."
                    )
                )
            }
        }
    }

    fun loadHistory() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingHistory = true)
            val history = repo.getHistory(userId)
            _state.value = _state.value.copy(history = history, isLoadingHistory = false)
        }
    }

    fun resetState() {
        _state.value = _state.value.copy(generationState = AIGenerationState.Idle)
    }
}
