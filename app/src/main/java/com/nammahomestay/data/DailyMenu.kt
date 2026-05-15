package com.nammahomestay.data

import com.google.firebase.Timestamp

data class DailyMenu(
    val date: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val mealsAvailable: List<String> = emptyList(),
    val dailyRateOverride: Int? = null,
    val lastUpdated: Timestamp? = null
)
