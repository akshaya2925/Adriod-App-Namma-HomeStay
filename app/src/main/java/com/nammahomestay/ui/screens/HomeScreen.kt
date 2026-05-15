package com.nammahomestay.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nammahomestay.R
import com.nammahomestay.ui.components.ShimmerBox
import com.nammahomestay.ui.home.HomeViewModel
import com.nammahomestay.ui.navigation.BottomNavItem
import com.nammahomestay.utils.AppResult

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel()
    val profileResult by viewModel.profileResult.collectAsStateWithLifecycle()
    val inquiriesResult by viewModel.inquiriesResult.collectAsStateWithLifecycle()
    val menuResult by viewModel.menuResult.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            if (profileResult is AppResult.Loading) {
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(80.dp))
            } else if (profileResult is AppResult.Success) {
                val profile = (profileResult as AppResult.Success).data
                val name = profile.homestayName.ifEmpty { "Host" }
                Text(
                    text = stringResource(R.string.good_morning, name),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            if (inquiriesResult is AppResult.Loading || profileResult is AppResult.Loading) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(modifier = Modifier.weight(1f).height(100.dp))
                    ShimmerBox(modifier = Modifier.weight(1f).height(100.dp))
                    ShimmerBox(modifier = Modifier.weight(1f).height(100.dp))
                }
            } else {
                val profile = (profileResult as? AppResult.Success)?.data
                val inqs = (inquiriesResult as? AppResult.Success)?.data ?: emptyList()
                val unread = inqs.count { !it.isRead }
                val week = inqs.size // Simplified: total
                val completePct = if (profile?.isProfileComplete == true) 100 else 40

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(stringResource(R.string.inquiries_this_week), week.toString(), Modifier.weight(1f))
                    StatCard(stringResource(R.string.unread_messages), unread.toString(), Modifier.weight(1f))
                    StatCard(stringResource(R.string.profile_completeness), "${completePct}%", Modifier.weight(1f))
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.availability), fontWeight = FontWeight.Bold)
                    // Simplified toggle logic using a local state assuming acceptingGuests exists in Firestore, defaults to true
                    var isAvailable by remember { mutableStateOf(true) }
                    Switch(checked = isAvailable, onCheckedChange = { 
                        isAvailable = it
                        viewModel.toggleAvailability(it)
                    })
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.today_menu), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (menuResult is AppResult.Loading) {
                        ShimmerBox(modifier = Modifier.fillMaxWidth().height(40.dp))
                    } else if (menuResult is AppResult.Success) {
                        val menu = (menuResult as AppResult.Success).data
                        Text(if (menu.description.isNotEmpty()) menu.description else stringResource(R.string.no_menu_yet))
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.quick_actions), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionBtn("Profile", Icons.Default.Edit, Modifier.weight(1f)) { navController.navigate(BottomNavItem.Profile.route) }
                QuickActionBtn("Menu", Icons.Default.Menu, Modifier.weight(1f)) { navController.navigate(BottomNavItem.Menu.route) }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionBtn("Guide", Icons.Default.LocationOn, Modifier.weight(1f)) { navController.navigate(BottomNavItem.LocalGuide.route) }
                QuickActionBtn("Logout", Icons.Default.ExitToApp, Modifier.weight(1f), isDestructive = true) {
                    viewModel.logout { navController.navigate("auth") { popUpTo(0) } }
                }
            }
        }

        item {
            Text(stringResource(R.string.recent_inquiries), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
        }

        if (inquiriesResult is AppResult.Success) {
            val recent = (inquiriesResult as AppResult.Success).data.take(3)
            items(recent) { inquiry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(inquiry.travelerName, fontWeight = FontWeight.Bold)
                            Text(inquiry.message, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                        }
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:${inquiry.travelerPhone}")
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(title, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 12.sp)
        }
    }
}

@Composable
fun QuickActionBtn(text: String, icon: ImageVector, modifier: Modifier = Modifier, isDestructive: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
    ) {
        Icon(icon, contentDescription = text, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp)
    }
}
