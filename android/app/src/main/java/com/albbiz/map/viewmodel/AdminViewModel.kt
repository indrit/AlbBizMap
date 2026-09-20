// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albbiz.map.data.Business
import com.albbiz.map.data.BusinessRepository
import com.albbiz.map.data.ClaimRequest
import com.albbiz.map.ui.CurrentLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class AdminViewModel(
    private val repository: BusinessRepository = BusinessRepository()
) : ViewModel() {

    // Single source of truth for both pending-claims lists below — split by
    // ClaimRequest.type rather than queried separately, since they come off the
    // same "claim_requests" Firestore listener anyway.
    private val _claimRequests = MutableStateFlow<List<ClaimRequest>>(emptyList())
    val claimRequests: StateFlow<List<ClaimRequest>> = _claimRequests

    private val _businessClaims = MutableStateFlow<List<ClaimRequest>>(emptyList())
    val businessClaims: StateFlow<List<ClaimRequest>> = _businessClaims

    private val _verificationRequests = MutableStateFlow<List<ClaimRequest>>(emptyList())
    val verificationRequests: StateFlow<List<ClaimRequest>> = _verificationRequests

    // Businesses currently on a paid tier (Premium/Featured/Sponsored), for the
    // Businesses & Plans table. Loaded from the same getActiveBusinesses() feed
    // the map screen uses, filtered client-side — there's no separate "plans"
    // collection, tier flags just live on the Business doc itself.
    private val _businessesWithPlans = MutableStateFlow<List<Business>>(emptyList())
    val businessesWithPlans: StateFlow<List<Business>> = _businessesWithPlans

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    // checkAdminStatus can be called more than once for the same ViewModel instance
    // (e.g. AdminScreen's LaunchedEffect(currentUserId) re-running because
    // currentUserId briefly flips through "" while auth state settles). Without
    // tracking the claims listener's Job, each call would attach a brand-new
    // addSnapshotListener on top of any still-active one, and all of them would
    // keep racing to write _claimRequests.
    private var claimRequestsJob: Job? = null
    private var businessesWithPlansJob: Job? = null

    fun checkAdminStatus(userId: String) {
        viewModelScope.launch {
            _isAdmin.value = repository.isUserAdmin(userId)
            if (_isAdmin.value) {
                loadClaimRequests()
                loadBusinessesWithPlans()
            }
        }
    }

    private fun loadClaimRequests() {
        claimRequestsJob?.cancel()
        _isLoading.value = true
        claimRequestsJob = repository.getClaimRequests()
            .onEach { claims ->
                val sorted = claims.sortedByDescending { it.createdAt }
                _claimRequests.value = sorted
                _businessClaims.value = sorted.filter { it.type != "verification" }
                _verificationRequests.value = sorted.filter { it.type == "verification" }
                _isLoading.value = false
            }
            .catch { e ->
                _message.value = "${CurrentLanguage.strings().errorLoadingClaimsPrefix}: ${e.message}"
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    private fun loadBusinessesWithPlans() {
        businessesWithPlansJob?.cancel()
        businessesWithPlansJob = repository.getActiveBusinesses()
            .onEach { businesses ->
                _businessesWithPlans.value = businesses
                    .filter { it.isPremium || it.isFeatured || it.isSponsored }
                    .sortedWith(
                        compareByDescending<Business> { it.isSponsored }
                            .thenByDescending { it.isFeatured }
                            .thenByDescending { it.isPremium }
                    )
            }
            .launchIn(viewModelScope)
    }

    fun approveClaim(claim: ClaimRequest) {
        viewModelScope.launch {
            repository.approveClaim(claim)
                .onSuccess {
                    _message.value = String.format(
                        CurrentLanguage.strings().claimApprovedTemplate,
                        claim.businessName,
                        claim.userEmail
                    )
                }
                .onFailure { e ->
                    _message.value = "${CurrentLanguage.strings().failedToApprovePrefix}: ${e.message}"
                }
        }
    }

    fun rejectClaim(claimId: String) {
        viewModelScope.launch {
            repository.rejectClaim(claimId)
                .onSuccess {
                    _message.value = CurrentLanguage.strings().claimRejectedMsg
                }
                .onFailure { e ->
                    _message.value = "${CurrentLanguage.strings().failedToRejectPrefix}: ${e.message}"
                }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    // Same double-tap guard pattern as seedMutex below — this writes one update
    // per stale business, so a second concurrent tap could double up on writes
    // mid-flight. _businessesWithPlans refreshes on its own afterward since it's
    // a live listener (loadBusinessesWithPlans), not something this needs to
    // manually re-fetch.
    private val clearExpiredMutex = Mutex()

    fun clearExpiredPlans() {
        if (!clearExpiredMutex.tryLock()) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.clearExpiredPlans(_businessesWithPlans.value)
                    .onSuccess { count ->
                        _message.value = if (count == 0) {
                            CurrentLanguage.strings().clearExpiredPlansNoneFound
                        } else {
                            String.format(CurrentLanguage.strings().clearExpiredPlansSuccessTemplate, count)
                        }
                    }
                    .onFailure { e ->
                        _message.value = "${CurrentLanguage.strings().clearExpiredPlansFailedPrefix}: ${e.message}"
                    }
                _isLoading.value = false
            } finally {
                clearExpiredMutex.unlock()
            }
        }
    }

    // "Import Sample Businesses" had no disabled state at all in the UI (unlike the
    // other admin actions), so a double-tap while the import is running — which can
    // take a while, it writes many documents — would kick off two full imports
    // concurrently and duplicate every sample business. tryLock rejects the second
    // tap outright instead of queueing it.
    private val seedMutex = Mutex()

    fun seedBusinesses(context: android.content.Context) {
        if (!seedMutex.tryLock()) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.seedBusinessesFromJson(context)
                    .onSuccess { count ->
                        _message.value = String.format(CurrentLanguage.strings().importSuccessTemplate, count)
                    }
                    .onFailure { e ->
                        _message.value = "${CurrentLanguage.strings().importFailedPrefix}: ${e.message}"
                    }
                _isLoading.value = false
            } finally {
                seedMutex.unlock()
            }
        }
    }
}