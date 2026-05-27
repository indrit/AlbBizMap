// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albbiz.map.data.Business
import com.albbiz.map.data.BusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EditBusinessUiState {
    object Initial : EditBusinessUiState()
    object Loading : EditBusinessUiState()
    object Success : EditBusinessUiState()
    data class Error(val message: String) : EditBusinessUiState()
}

class EditBusinessViewModel(
    private val repository: BusinessRepository = BusinessRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditBusinessUiState>(EditBusinessUiState.Initial)
    val uiState: StateFlow<EditBusinessUiState> = _uiState.asStateFlow()

    fun updateBusiness(
        business: Business,
        newPhotoUri: Uri?
    ) {
        viewModelScope.launch {
            _uiState.value = EditBusinessUiState.Loading

            try {
                // Upload new photo if user picked one
                val finalBusiness = if (newPhotoUri != null) {
                    val photoResult = repository.updateBusinessPhoto(business.id, newPhotoUri)
                    if (photoResult.isSuccess) {
                        business.copy(photos = listOf(photoResult.getOrThrow()))
                    } else {
                        _uiState.value = EditBusinessUiState.Error("Failed to upload photo")
                        return@launch
                    }
                } else {
                    business
                }

                // Update business in Firestore
                val updateResult = repository.updateBusiness(finalBusiness)
                if (updateResult.isSuccess) {
                    _uiState.value = EditBusinessUiState.Success
                } else {
                    _uiState.value = EditBusinessUiState.Error(
                        updateResult.exceptionOrNull()?.message ?: "Failed to update business"
                    )
                }

            } catch (e: Exception) {
                _uiState.value = EditBusinessUiState.Error(
                    e.message ?: "Unexpected error"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = EditBusinessUiState.Initial
    }
}