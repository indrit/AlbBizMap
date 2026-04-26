// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albbiz.map.data.Review
import com.albbiz.map.data.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val repo: ReviewRepository = ReviewRepository()
) : ViewModel() {

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _reportMessage = MutableStateFlow<String?>(null)
    val reportMessage: StateFlow<String?> = _reportMessage

    // LOAD REVIEWS FOR A BUSINESS
    fun loadReviews(businessId: String) {
        _isLoading.value = true

        repo.getReviews(businessId)
            .onEach { list ->
                _reviews.value = list.sortedByDescending { it.createdAt }
                _isLoading.value = false
            }
            .catch { e ->
                _error.value = e.message
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    // ADD A NEW REVIEW
    fun addReview(
        businessId: String,
        rating: Int,
        comment: String,
        userId: String,
        userName: String,
        onSuccess: () -> Unit
    ) {
        val review = Review(
            businessId = businessId,
            userId = userId,
            userName = userName,
            rating = rating,
            comment = comment
        )

        viewModelScope.launch {
            _isLoading.value = true
            repo.addReview(businessId, review)
                .onSuccess { onSuccess() }
                .onFailure { e -> _error.value = e.message }
            _isLoading.value = false
        }
    }

    // REPORT A REVIEW
    fun reportReview(
        businessId: String,
        reviewId: String,
        userId: String
    ) {
        viewModelScope.launch {
            repo.reportReview(businessId, reviewId, userId)
                .onSuccess {
                    _reportMessage.value = "Review reported successfully"
                }
                .onFailure { e ->
                    _reportMessage.value = e.message // shows "already reported" if applicable
                }
        }
    }

    fun clearReportMessage() {
        _reportMessage.value = null
    }
}