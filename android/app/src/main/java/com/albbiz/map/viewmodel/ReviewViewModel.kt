// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albbiz.map.data.Review
import com.albbiz.map.data.ReviewRepository
import com.albbiz.map.ui.CurrentLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import com.albbiz.map.data.Reply

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

    // Guards addReview/addReply against double-submit: the UI's "enabled = !isLoading"
    // only takes effect once Compose recomposes, which isn't guaranteed to happen
    // before a second very-fast tap is already dispatched. This makes the ViewModel
    // itself refuse a second submit while one is still in flight, instead of relying
    // solely on UI lag.
    private val submitMutex = Mutex()

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
        photoUris: List<Uri> = emptyList(),
        onSuccess: () -> Unit
    ) {
        val review = Review(
            businessId = businessId,
            userId = userId,
            userName = userName,
            rating = rating,
            comment = comment
        )

        // tryLock (not withLock/launch-then-wait) so a second rapid tap is dropped
        // immediately instead of queueing up to run right after the first — we want
        // "ignore the duplicate", not "submit it too, just slightly later".
        if (!submitMutex.tryLock()) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                repo.addReview(businessId, review, photoUris)
                    .onSuccess { onSuccess() }
                    .onFailure { e -> _error.value = e.message }
                _isLoading.value = false
            } finally {
                submitMutex.unlock()
            }
        }
    }

    // EDIT A REVIEW (author only — the calling UI is expected to only show this
    // to the review's own author, and Firestore rules enforce it server-side too)
    private val updateReviewMutex = Mutex()

    fun updateReview(
        businessId: String,
        reviewId: String,
        rating: Int,
        comment: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit = {}
    ) {
        if (!updateReviewMutex.tryLock()) return
        viewModelScope.launch {
            try {
                repo.updateReview(businessId, reviewId, rating, comment)
                    .onSuccess { onSuccess() }
                    .onFailure { onFailure() }
            } finally {
                updateReviewMutex.unlock()
            }
        }
    }

    // DELETE A REVIEW (author only, same enforcement note as above)
    private val deleteReviewMutex = Mutex()

    fun deleteReview(
        businessId: String,
        reviewId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit = {}
    ) {
        if (!deleteReviewMutex.tryLock()) return
        viewModelScope.launch {
            try {
                repo.deleteReview(businessId, reviewId)
                    .onSuccess { onSuccess() }
                    .onFailure { onFailure() }
            } finally {
                deleteReviewMutex.unlock()
            }
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
                    _reportMessage.value = CurrentLanguage.strings().reviewReportedSuccess
                }
                .onFailure { e ->
                    _reportMessage.value = e.message // shows "already reported" if applicable
                }
        }
    }

    // TOGGLE LIKE ON A REVIEW
    fun toggleLike(
        businessId: String,
        reviewId: String,
        userId: String
    ) {
        viewModelScope.launch {
            repo.toggleLike(businessId, reviewId, userId)
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    fun clearReportMessage() {
        _reportMessage.value = null
    }

    // GET REPLIES FOR A REVIEW
    private val _replies = MutableStateFlow<Map<String, List<Reply>>>(emptyMap())
    val replies: StateFlow<Map<String, List<Reply>>> = _replies

    // One live Firestore listener job per reviewId. Without this, calling
    // loadReplies() again for a reviewId that's already being listened to (e.g. the
    // caller's LaunchedEffect re-firing) would attach a second concurrent listener
    // on the same query instead of replacing the first — both then race to write the
    // same _replies map entry indefinitely.
    private val replyJobs = mutableMapOf<String, Job>()

    fun loadReplies(businessId: String, reviewId: String) {
        replyJobs[reviewId]?.cancel()
        replyJobs[reviewId] = viewModelScope.launch {
            repo.getReplies(businessId, reviewId)
                .catch { e -> _error.value = e.message }
                .collect { replyList ->
                    _replies.value = _replies.value.toMutableMap().also {
                        it[reviewId] = replyList
                    }
                }
        }
    }

    // ADD A REPLY
    private val replyMutex = Mutex()

    fun addReply(
        businessId: String,
        reviewId: String,
        comment: String,
        userId: String,
        userName: String,
        onSuccess: () -> Unit
    ) {
        if (!replyMutex.tryLock()) return

        val reply = Reply(
            reviewId = reviewId,
            userId = userId,
            userName = userName,
            comment = comment
        )
        viewModelScope.launch {
            try {
                repo.addReply(businessId, reviewId, reply)
                    .onSuccess { onSuccess() }
                    .onFailure { e -> _error.value = e.message }
            } finally {
                replyMutex.unlock()
            }
        }
    }

    // EDIT A REPLY (author only)
    private val updateReplyMutex = Mutex()

    fun updateReply(
        businessId: String,
        reviewId: String,
        replyId: String,
        comment: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit = {}
    ) {
        if (!updateReplyMutex.tryLock()) return
        viewModelScope.launch {
            try {
                repo.updateReply(businessId, reviewId, replyId, comment)
                    .onSuccess { onSuccess() }
                    .onFailure { onFailure() }
            } finally {
                updateReplyMutex.unlock()
            }
        }
    }

    // DELETE A REPLY (author only)
    private val deleteReplyMutex = Mutex()

    fun deleteReply(
        businessId: String,
        reviewId: String,
        replyId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit = {}
    ) {
        if (!deleteReplyMutex.tryLock()) return
        viewModelScope.launch {
            try {
                repo.deleteReply(businessId, reviewId, replyId)
                    .onSuccess { onSuccess() }
                    .onFailure { onFailure() }
            } finally {
                deleteReplyMutex.unlock()
            }
        }
    }

    // TOGGLE LIKE ON A REPLY
    fun toggleReplyLike(
        businessId: String,
        reviewId: String,
        replyId: String,
        userId: String
    ) {
        viewModelScope.launch {
            repo.toggleReplyLike(businessId, reviewId, replyId, userId)
                .onFailure { e -> _error.value = e.message }
        }
    }
}