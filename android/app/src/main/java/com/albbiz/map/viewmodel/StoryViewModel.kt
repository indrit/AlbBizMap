// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albbiz.map.data.Story
import com.albbiz.map.data.StoriesRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class AddStoryUiState {
    object Idle : AddStoryUiState()
    object Loading : AddStoryUiState()
    object Success : AddStoryUiState()
    data class Error(val message: String) : AddStoryUiState()
}

class StoriesViewModel(
    private val repository: StoriesRepository = StoriesRepository()
) : ViewModel() {

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _addStoryState = MutableStateFlow<AddStoryUiState>(AddStoryUiState.Idle)
    val addStoryState: StateFlow<AddStoryUiState> = _addStoryState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadStories()
        cleanExpiredStories()
    }

    // ── LOAD ACTIVE STORIES ───────────────────────────────────────────
    private fun loadStories() {
        _isLoading.value = true
        repository.getActiveStories()
            .onEach { list ->
                _stories.value = list
                _isLoading.value = false
            }
            .catch { e ->
                _error.value = e.message
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    // ── ADD STORY ─────────────────────────────────────────────────────
    fun addStory(
        text: String,
        type: String,
        category: String,
        location: String,
        photoUris: List<Uri>,
        businessId: String? = null,
        businessName: String? = null,
        isSponsored: Boolean = false
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: run {
            _addStoryState.value = AddStoryUiState.Error("You must be logged in to post a story")
            return
        }

        if (photoUris.isEmpty()) {
            _addStoryState.value = AddStoryUiState.Error("Please add at least one photo")
            return
        }

        if (photoUris.size > 10) {
            _addStoryState.value = AddStoryUiState.Error("Maximum 10 photos per story")
            return
        }

        viewModelScope.launch {
            _addStoryState.value = AddStoryUiState.Loading

            val story = Story(
                userId = currentUser.uid,
                userName = currentUser.displayName?.takeIf { it.isNotBlank() }
                    ?: currentUser.email?.substringBefore("@") ?: "User",
                businessId = businessId,
                businessName = businessName,
                type = type,
                category = category,
                location = location,
                text = text,
                isSponsored = isSponsored
            )

            repository.addStory(story, photoUris)
                .onSuccess {
                    _addStoryState.value = AddStoryUiState.Success
                }
                .onFailure { e ->
                    _addStoryState.value = AddStoryUiState.Error(
                        e.message ?: "Failed to post story"
                    )
                }
        }
    }

    // ── MARK STORY AS VIEWED ──────────────────────────────────────────
    fun markStoryViewed(storyId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            repository.markStoryViewed(storyId, userId)
        }
    }

    // ── DELETE STORY ──────────────────────────────────────────────────
    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            repository.deleteStory(storyId)
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    // ── CLEAN EXPIRED STORIES ─────────────────────────────────────────
    private fun cleanExpiredStories() {
        viewModelScope.launch {
            repository.deleteExpiredStories()
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────

    // Group stories by user/business for the stories bar
    fun getGroupedStories(): Map<String, List<Story>> {
        return _stories.value.groupBy { story ->
            story.businessId ?: story.userId
        }
    }

    // Check if current user has viewed a story group
    fun hasViewedAllStories(stories: List<Story>): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        return stories.all { userId in it.viewedBy }
    }

    fun resetAddStoryState() {
        _addStoryState.value = AddStoryUiState.Idle
    }

    fun clearError() {
        _error.value = null
    }
}