package com.nammahomestay.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Menu : BottomNavItem("menu", "Menu", Icons.AutoMirrored.Filled.List)
    object Inquiries : BottomNavItem("inquiries", "Inquiries", Icons.Default.Info)
    object LocalGuide : BottomNavItem("guide", "Local Guide", Icons.Default.LocationOn)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}
