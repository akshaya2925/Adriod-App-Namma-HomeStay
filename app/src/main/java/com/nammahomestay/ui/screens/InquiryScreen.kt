package com.nammahomestay.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nammahomestay.ui.inquiry.InquiryViewModel
import com.nammahomestay.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun InquiryScreen(navController: NavController, viewModel: InquiryViewModel) {
    val inquiriesResult by viewModel.inquiriesResult.collectAsState()
    val inquiries = (inquiriesResult as? com.nammahomestay.utils.AppResult.Success)?.data ?: emptyList()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(Cream)) {
        // Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Inquiry Box", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BrownMedium)
            Text(text = "${inquiries.size} messages from travelers", fontSize = 14.sp, color = TextLight)
        }

        if (inquiries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No inquiries yet.\nTravelers will reach out once your profile is live.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = TextMedium,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(inquiries) { inquiry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectInquiry(inquiry)
                                navController.navigate("inquiryDetail")
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(TerraXL, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = inquiry.travelerName.take(1).uppercase(),
                                        color = TerraDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = inquiry.travelerName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = inquiry.travelerPhone, fontSize = 11.sp, color = TextLight)
                                }

                                Surface(
                                    color = Sand,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                                    val timeStr = timeFormat.format(inquiry.createdAt.toDate())
                                    Text(
                                        text = "Today · $timeStr",
                                        fontSize = 11.sp,
                                        color = TextLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Message box
                            Surface(
                                color = Sand,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                    // Simulation of border-left
                                    Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(TerraXL))
                                    Text(
                                        text = inquiry.message,
                                        fontSize = 13.sp,
                                        color = TextMedium,
                                        modifier = Modifier.padding(10.dp),
                                        lineHeight = 20.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("smsto:${inquiry.travelerPhone}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SMS", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL)
                                        intent.data = Uri.parse("tel:${inquiry.travelerPhone}")
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Terra)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Call", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
