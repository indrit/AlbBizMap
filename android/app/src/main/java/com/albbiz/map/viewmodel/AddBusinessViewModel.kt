// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albbiz.map.data.Business
import com.albbiz.map.data.BusinessRepository
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AddBusinessUiState {
    object Initial : AddBusinessUiState()
    object Loading : AddBusinessUiState()
    object Success : AddBusinessUiState()
    data class Error(val message: String) : AddBusinessUiState()
}

class AddBusinessViewModel(
    private val repository: BusinessRepository = BusinessRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddBusinessUiState>(AddBusinessUiState.Initial)
    val uiState: StateFlow<AddBusinessUiState> = _uiState.asStateFlow()

    fun addBusiness(business: Business, imageUris: List<Uri>) {
        viewModelScope.launch {
            _uiState.value = AddBusinessUiState.Loading

            try {
                val imageUrls = if (imageUris.isNotEmpty()) {
                    repository.uploadBusinessImages(business.id, imageUris)
                } else {
                    Result.success(emptyList())
                }

                imageUrls.fold(
                    onSuccess = { urls ->
                        val businessWithImages = business.copy(photos = urls)
                        val result = repository.addBusiness(businessWithImages)

                        result.fold(
                            onSuccess = {
                                _uiState.value = AddBusinessUiState.Success
                            },
                            onFailure = { e ->
                                _uiState.value = AddBusinessUiState.Error(
                                    "Failed to add business: ${e.message}"
                                )
                            }
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = AddBusinessUiState.Error(
                            "Failed to upload images: ${e.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = AddBusinessUiState.Error(
                    "Unexpected error: ${e.message}"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = AddBusinessUiState.Initial
    }
}