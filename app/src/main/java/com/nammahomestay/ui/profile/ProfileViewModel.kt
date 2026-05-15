package com.nammahomestay.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nammahomestay.data.HostProfile
import com.nammahomestay.firebase.FirebaseManager
import com.nammahomestay.repository.HostRepository
import com.nammahomestay.utils.AppResult
import com.nammahomestay.utils.ImageUploadHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HostRepository()

    val profileResult: StateFlow<AppResult<HostProfile>> = repository.getProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppResult.Loading)

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun saveProfile(profile: HostProfile) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.saveProfile(profile)
                _message.value = "Profile saved!"
            } catch (e: Exception) {
                _message.value = "Failed to save profile: " + e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun uploadPhoto(uri: Uri) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val uid = FirebaseManager.auth.currentUser?.uid ?: return@launch
                val bytes = ImageUploadHelper.compressImage(getApplication(), uri) ?: return@launch
                
                val downloadUrl = com.nammahomestay.utils.CloudinaryHelper.uploadImage(bytes)
                if (downloadUrl == null) {
                    _message.value = "Failed to upload to Cloudinary"
                    return@launch
                }

                val currentProfile = (profileResult.value as? AppResult.Success)?.data ?: return@launch
                val currentUrls = currentProfile.photoUrls.toMutableList()
                currentUrls.add(downloadUrl)

                FirebaseManager.firestore.collection("hosts").document(uid).update("photoUrls", currentUrls).await()
            } catch (e: Exception) {
                _message.value = "Failed to upload photo"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deletePhoto(url: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val uid = FirebaseManager.auth.currentUser?.uid ?: return@launch
                // Cloudinary images uploaded via unsigned preset are not deleted via client SDK.
                // We just remove the reference from Firestore.

                val currentProfile = (profileResult.value as? AppResult.Success)?.data ?: return@launch
                val currentUrls = currentProfile.photoUrls.toMutableList()
                currentUrls.remove(url)

                FirebaseManager.firestore.collection("hosts").document(uid).update("photoUrls", currentUrls).await()
            } catch (e: Exception) {
                _message.value = "Failed to delete photo"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearMessage() { _message.value = null }
}
