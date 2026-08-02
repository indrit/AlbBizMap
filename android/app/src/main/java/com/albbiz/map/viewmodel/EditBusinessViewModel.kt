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

    // newPhotoUris: freshly picked local images still needing upload.
    // keptPhotoUrls: existing (already-uploaded) photo URLs the user did NOT
    // delete from the gallery manager — passed in explicitly rather than reading
    // business.photos directly, since the caller may have removed some of the
    // business's original photos in this same edit session and that removal
    // needs to persist even if no new photos were added at all.
    fun updateBusiness(
        business: Business,
        newPhotoUris: List<Uri>,
        keptPhotoUrls: List<String>
    ) {
        // Same double-submit gap as AddBusinessViewModel: set/check Loading
        // synchronously, before launch, so a second fast tap on Save is rejected
        // here rather than racing a real second update through.
        if (_uiState.value == EditBusinessUiState.Loading) return
        _uiState.value = EditBusinessUiState.Loading

        viewModelScope.launch {
            try {
                // Upload any newly picked photos, then combine with whatever
                // existing photos survived the edit — never a blind replace, so
                // photos the user didn't touch (or newly kept after deleting
                // others) aren't lost.
                val finalBusiness = if (newPhotoUris.isNotEmpty()) {
                    val uploadResult = repository.uploadBusinessImages(business.id, newPhotoUris)
                    if (uploadResult.isSuccess) {
                        business.copy(photos = keptPhotoUrls + uploadResult.getOrThrow())
                    } else {
                        _uiState.value = EditBusinessUiState.Error("Failed to upload photos")
                        return@launch
                    }
                } else {
                    business.copy(photos = keptPhotoUrls)
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