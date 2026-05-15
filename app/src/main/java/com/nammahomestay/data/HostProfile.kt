package com.nammahomestay.data

data class HostProfile(
    val homestayName: String = "",
    val village: String = "",
    val numRooms: Int = 0,
    val maxGuests: Int = 0,
    val roomType: String = "",
    val description: String = "",
    val photoUrls: List<String> = emptyList(),
    val verificationChecklist: Map<String, Boolean> = emptyMap(),
    val whatsapp: String = "",
    val languages: List<String> = emptyList(),
    val isProfileComplete: Boolean = false
)
