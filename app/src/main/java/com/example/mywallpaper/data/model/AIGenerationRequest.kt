package com.example.mywallpaper.data.model

data class AIGenerationRequest(
    val prompt: String,
    val style: String = "Photorealistic",
    val mood: String = "Calm",
    val color: String = "Auto",
    val format: String = "Phone",
    val quality: String = "Standard",
    val idToken: String = ""
)

data class AIGenerationResponse(
    val imageUrl: String = "",
    val generationId: String = "",
    val enhancedPrompt: String = ""
)

data class EnhancePromptRequest(
    val prompt: String,
    val idToken: String = ""
)

data class EnhancePromptResponse(
    val enhancedPrompt: String = ""
)
