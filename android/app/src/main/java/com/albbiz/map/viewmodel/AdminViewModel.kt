// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albbiz.map.data.BusinessRepository
import com.albbiz.map.data.ClaimRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: BusinessRepository = BusinessRepository()
) : ViewModel() {

    private val _claimRequests = MutableStateFlow<List<ClaimRequest>>(emptyList())
    val claimRequests: StateFlow<List<ClaimRequest>> = _claimRequests

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    fun checkAdminStatus(userId: String) {
        viewModelScope.launch {
            _isAdmin.value = repository.isUserAdmin(userId)
            if (_isAdmin.value) loadClaimRequests()
        }
    }

    private fun loadClaimRequests() {
        _isLoading.value = true
        repository.getClaimRequests()
            .onEach { claims ->
                _claimRequests.value = claims.sortedByDescending { it.createdAt }
                _isLoading.value = false
            }
            .catch { e ->
                _message.value = "Error loading claims: ${e.message}"
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun approveClaim(claim: ClaimRequest) {
        viewModelScope.launch {
            repository.approveClaim(claim)
                .onSuccess {
                    _message.value = "Claim approved — ${claim.businessName} is now owned by ${claim.userEmail}"
                }
                .onFailure { e ->
                    _message.value = "Failed to approve: ${e.message}"
                }
        }
    }

    fun rejectClaim(claimId: String) {
        viewModelScope.launch {
            repository.rejectClaim(claimId)
                .onSuccess {
                    _message.value = "Claim rejected"
                }
                .onFailure { e ->
                    _message.value = "Failed to reject: ${e.message}"
                }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun seedBusinesses(context: android.content.Context) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.seedBusinessesFromJson(context)
                .onSuccess { count ->
                    _message.value = "Successfully imported $count businesses!"
                }
                .onFailure { e ->
                    _message.value = "Import failed: ${e.message}"
                }
            _isLoading.value = false
        }
    }
}