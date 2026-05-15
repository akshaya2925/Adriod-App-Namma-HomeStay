package com.nammahomestay.ui.guide

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.nammahomestay.data.LocalSpot
import com.nammahomestay.firebase.FirebaseManager
import com.nammahomestay.repository.SpotRepository
import com.nammahomestay.utils.AppResult
import com.nammahomestay.utils.ImageUploadHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocalGuideViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SpotRepository()

    val spotsResult: StateFlow<AppResult<List<LocalSpot>>> = repository.getSpotsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppResult.Loading)

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun addSpot(name: String, category: String, description: String, distance: String, bestTime: String, photoUri: Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val uid = FirebaseManager.auth.currentUser?.uid ?: return@launch
                val spotId = FirebaseManager.firestore.collection("hosts").document(uid).collection("localSpots").document().id
                
                var photoUrl: String? = null
                if (photoUri != null) {
                    val bytes = ImageUploadHelper.compressImage(getApplication(), photoUri)
                    if (bytes != null) {
                        photoUrl = com.nammahomestay.utils.CloudinaryHelper.uploadImage(bytes)
                    }
                }

                val spot = LocalSpot(
                    id = spotId,
                    name = name,
                    category = category,
                    description = description,
                    photoUrl = photoUrl,
                    distance = distance,
                    bestTime = bestTime,
                    createdAt = Timestamp.now()
                )

                repository.addSpot(spot)
                _message.value = "Spot added successfully!"
                onSuccess()
            } catch (e: Exception) {
                _message.value = "Failed to add spot"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteSpot(spotId: String) {
        viewModelScope.launch {
            try {
                repository.deleteSpot(spotId)
                _message.value = "Spot deleted"
            } catch (e: Exception) {
                _message.value = "Failed to delete spot"
            }
        }
    }

    fun clearMessage() { _message.value = null }
}
