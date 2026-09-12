package com.example.mywallpaper.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywallpaper.R
import com.example.mywallpaper.data.model.UserProfile
import com.example.mywallpaper.data.repository.AuthRepository
import com.example.mywallpaper.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val context: Context) : ViewModel() {

    private val authRepo = AuthRepository(
        context = context,
        webClientId = try { context.getString(R.string.default_web_client_id) } catch (e: Exception) { "" }
    )
    private val favRepo = FavoritesRepository(context)

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val profile = authRepo.getCurrentUserProfile()
        _profile.value = profile.copy(favoritesCount = favRepo.getFavorites().size)
    }

    suspend fun signOut() {
        authRepo.signOut()
    }
}
