package com.nammahomestay.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nammahomestay.ui.home.HomeViewModel
import com.nammahomestay.ui.inquiry.InquiryViewModel
import com.nammahomestay.ui.navigation.BottomNavItem
import com.nammahomestay.ui.theme.*
import com.nammahomestay.utils.AppResult

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel()
    val inquiryViewModel: InquiryViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)
    
    val profileResult by viewModel.profileResult.collectAsStateWithLifecycle()
    val inquiriesResult by viewModel.inquiriesResult.collectAsStateWithLifecycle()
    val menuResult by viewModel.menuResult.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            if (profileResult is AppResult.Success) {
                val profile = (profileResult as AppResult.Success).data
                Column {
                    Text(
                        text = "Namaskara,",
                        fontSize = 16.sp,
                        color = TextMedium
                    )
                    Text(
                        text = profile.homestayName.ifEmpty { "Host" },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerraDark
                    )
                }
            }
        }

        // Stats Row 1
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Daily Rate",
                    value = "₹800",
                    label = "Per night",
                    icon = "💰",
                    color = Terra,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Inquiries",
                    value = if (inquiriesResult is AppResult.Success) (inquiriesResult as AppResult.Success).data.count { !it.isRead }.toString() else "0",
                    label = "New Today",
                    icon = "✉️",
                    color = Leaf,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Stats Row 2
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Bookings",
                    value = "4",
                    label = "This Month",
                    icon = "🗓️",
                    color = Sky,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Rating",
                    value = "4.8",
                    label = "12 reviews",
                    icon = "⭐",
                    color = Amber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "🚀 Quick Actions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BrownMedium
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionBtn("Update Menu", Icons.Default.List, Modifier.weight(1f)) {
                        navController.navigate(BottomNavItem.Menu.route)
                    }
                    QuickActionBtn("Edit Profile", Icons.Default.Person, Modifier.weight(1f)) {
                        navController.navigate(BottomNavItem.Profile.route)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionBtn("View Inquiries", Icons.Default.Email, Modifier.weight(1f)) {
                        navController.navigate(BottomNavItem.Inquiries.route)
                    }
                    QuickActionBtn("Logout", Icons.Default.ExitToApp, Modifier.weight(1f), isDestructive = true) {
                        viewModel.logout { navController.navigate("auth") { popUpTo(0) } }
                    }
                }
            }
        }

        // Today's Snapshot
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📋 Today's Snapshot",
                        fontWeight = FontWeight.Bold,
                        color = BrownMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    SnapshotRow("Menu Updated", "✓ Done", Leaf)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SandDark)
                    SnapshotRow("Rooms Available", "2 / 3", TextPrimary)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SandDark)
                    
                    if (menuResult is AppResult.Success) {
                        val menu = (menuResult as AppResult.Success).data
                        SnapshotRow("Tonight's Dinner", if (menu.description.length > 20) menu.description.take(20) + "..." else menu.description, TextMedium)
                    }
                }
            }
        }

        // Recent Inquiries
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📬 Recent Inquiries",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BrownMedium
                )
                TextButton(onClick = { navController.navigate(BottomNavItem.Inquiries.route) }) {
                    Text("View All →", color = Terra)
                }
            }
        }

        if (inquiriesResult is AppResult.Success) {
            val recent = (inquiriesResult as AppResult.Success).data.take(3)
            items(recent) { inquiry ->
                InquiryPreviewCard(inquiry) {
                    inquiryViewModel.selectInquiry(inquiry)
                    navController.navigate("inquiryDetail")
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, label: String, icon: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(icon, fontSize = 24.sp)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(title, fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Medium)
            Surface(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SnapshotRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextMedium)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun QuickActionBtn(text: String, icon: ImageVector, modifier: Modifier = Modifier, isDestructive: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDestructive) Color(0xFFF5E2C4) else Terra,
            contentColor = if (isDestructive) BrownMedium else Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InquiryPreviewCard(inquiry: com.nammahomestay.data.Inquiry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(TerraXL, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = inquiry.travelerName.take(1).uppercase(),
                    color = TerraDark,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(inquiry.travelerName, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    inquiry.message,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    color = TextMedium
                )
            }
            Text(
                "Today",
                fontSize = 11.sp,
                color = TextLight
            )
        }
    }
}
