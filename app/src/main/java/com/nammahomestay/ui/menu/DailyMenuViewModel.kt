package com.nammahomestay.ui.menu

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.nammahomestay.data.DailyMenu
import com.nammahomestay.firebase.FirebaseManager
import com.nammahomestay.repository.MenuRepository
import com.nammahomestay.utils.AppResult
import com.nammahomestay.utils.ImageUploadHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyMenuViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MenuRepository()

    private val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val menuResult: StateFlow<AppResult<DailyMenu>> = repository.getTodayMenuFlow(dateStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppResult.Loading)

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val lastUpdatedText: StateFlow<String> = menuResult.map { result ->
        if (result is AppResult.Success && result.data.lastUpdated != null) {
            val format = SimpleDateFormat("h:mm a", Locale.getDefault())
            "Last updated: Today at " + format.format(result.data.lastUpdated.toDate())
        } else "Last updated: -"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Last updated: -")

    fun saveMenu(description: String, meals: List<String>, rateOverride: Int?, photoUri: Uri?, onSpeedySave: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val uid = FirebaseManager.auth.currentUser?.uid ?: return@launch
                var photoUrl = (menuResult.value as? AppResult.Success)?.data?.photoUrl

                if (photoUri != null) {
                    val bytes = ImageUploadHelper.compressImage(getApplication(), photoUri)
                    if (bytes != null) {
                        val downloadUrl = com.nammahomestay.utils.CloudinaryHelper.uploadImage(bytes)
                        if (downloadUrl != null) {
                            photoUrl = downloadUrl
                        }
                    }
                }

                val menu = DailyMenu(
                    date = dateStr,
                    description = description,
                    photoUrl = photoUrl,
                    mealsAvailable = meals,
                    dailyRateOverride = rateOverride,
                    lastUpdated = Timestamp.now()
                )

                repository.saveMenu(menu, dateStr)
                onSpeedySave()
            } catch (e: Exception) {
                _message.value = "Failed to update menu"
            } finally {
                _loading.value = false
            }
        }
    }

    fun showMessage(msg: String) { _message.value = msg }
    fun clearMessage() { _message.value = null }
}
