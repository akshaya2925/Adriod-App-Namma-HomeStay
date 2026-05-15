package com.nammahomestay.data

import com.google.firebase.Timestamp

data class Inquiry(
    var id: String = "",
    val travelerName: String = "",
    val travelerPhone: String = "",
    val message: String = "",
    val checkIn: String = "",
    val checkOut: String = "",
    val guests: Int = 0,
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)

data class Reply(
    val message: String = "",
    val sentAt: Timestamp = Timestamp.now()
)
