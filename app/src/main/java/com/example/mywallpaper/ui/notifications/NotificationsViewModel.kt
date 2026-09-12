package com.example.mywallpaper.ui.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywallpaper.data.model.NotificationItem
import com.example.mywallpaper.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<NotificationItem> = emptyList(),
    val groupedNotifications: Map<String, List<NotificationItem>> = emptyMap(),
    val unreadCount: Int = 0,
    val isNewWallpapersEnabled: Boolean = true,
    val error: String? = null
)

class NotificationsViewModel(
    private val repo: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val TAG = "NotificationsVM"

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    val unreadCount: StateFlow<Int> = repo.getUnreadCountFlow()
        .catch { emit(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadSettings()
        observeNotifications()
        registerToken()
    }

    fun registerToken() {
        viewModelScope.launch {
            try {
                repo.registerDeviceToken()
            } catch (e: Exception) {
                Log.w(TAG, "Token registration error: ${e.message}")
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val enabled = repo.isNewWallpapersEnabled()
                _uiState.value = _uiState.value.copy(isNewWallpapersEnabled = enabled)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load notification settings: ${e.message}")
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.getNotificationsFlow()
                .catch { e ->
                    Log.e(TAG, "Error collecting notifications: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Couldn't load notifications."
                    )
                }
                .collect { list ->
                    val groups = groupNotifications(list)
                    val unread = list.count { !it.read }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notifications = list,
                        groupedNotifications = groups,
                        unreadCount = unread,
                        error = null
                    )
                }
        }
    }

    private fun groupNotifications(list: List<NotificationItem>): Map<String, List<NotificationItem>> {
        val groups = linkedMapOf<String, MutableList<NotificationItem>>()
        for (item in list) {
            val groupKey = item.dateGroup()
            groups.getOrPut(groupKey) { mutableListOf() }.add(item)
        }
        return groups
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repo.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repo.markAllAsRead()
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            repo.deleteNotification(notificationId)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repo.clearAllNotifications()
        }
    }

    fun setNewWallpapersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                repo.setNewWallpapersEnabled(enabled)
                _uiState.value = _uiState.value.copy(isNewWallpapersEnabled = enabled)
                if (enabled) {
                    repo.registerDeviceToken()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update new wallpapers preference: ${e.message}", e)
            }
        }
    }

    fun retry() {
        observeNotifications()
        loadSettings()
    }
}
