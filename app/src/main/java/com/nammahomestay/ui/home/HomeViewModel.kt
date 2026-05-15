package com.nammahomestay.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammahomestay.data.DailyMenu
import com.nammahomestay.data.HostProfile
import com.nammahomestay.data.Inquiry
import com.nammahomestay.firebase.FirebaseManager
import com.nammahomestay.repository.HostRepository
import com.nammahomestay.repository.InquiryRepository
import com.nammahomestay.repository.MenuRepository
import com.nammahomestay.utils.AppResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel : ViewModel() {
    private val hostRepository = HostRepository()
    private val inquiryRepository = InquiryRepository()
    private val menuRepository = MenuRepository()

    val profileResult: StateFlow<AppResult<HostProfile>> = hostRepository.getProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppResult.Loading)

    val inquiriesResult: StateFlow<AppResult<List<Inquiry>>> = inquiryRepository.getInquiriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppResult.Loading)

    private val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val menuResult: StateFlow<AppResult<DailyMenu>> = menuRepository.getTodayMenuFlow(dateStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppResult.Loading)

    fun toggleAvailability(isAvailable: Boolean) {
        viewModelScope.launch {
            val uid = FirebaseManager.auth.currentUser?.uid ?: return@launch
            FirebaseManager.firestore.collection("hosts").document(uid).update("acceptingGuests", isAvailable).await()
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        FirebaseManager.auth.signOut()
        onLogoutComplete()
    }
}
