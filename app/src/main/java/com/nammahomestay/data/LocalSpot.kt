package com.nammahomestay.data

import com.google.firebase.Timestamp

data class LocalSpot(
    var id: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val distance: String = "",
    val bestTime: String = "",
    val createdAt: Timestamp = Timestamp.now()
)
