package com.nammahomestay.ui.inquiry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammahomestay.data.Inquiry
import com.nammahomestay.repository.InquiryRepository
import com.nammahomestay.utils.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InquiryViewModel : ViewModel() {
    private val repository = InquiryRepository()

    val inquiriesResult: StateFlow<AppResult<List<Inquiry>>> = repository.getInquiriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppResult.Loading)

    val unreadCount: StateFlow<Int> = inquiriesResult.map { result ->
        if (result is AppResult.Success) result.data.count { !it.isRead } else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedInquiry = MutableStateFlow<Inquiry?>(null)
    val selectedInquiry: StateFlow<Inquiry?> = _selectedInquiry.asStateFlow()

    fun selectInquiry(inquiry: Inquiry) {
        _selectedInquiry.value = inquiry
        if (!inquiry.isRead) {
            viewModelScope.launch {
                repository.markAsRead(inquiry.id)
            }
        }
    }

    fun sendReply(inquiryId: String, message: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.sendReply(inquiryId, message)
                onSuccess()
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
