package com.nammahomestay.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nammahomestay.ui.inquiry.InquiryViewModel

@Composable
fun InquiryDetailScreen(navController: NavController, viewModel: InquiryViewModel) {
    val selectedInquiry by viewModel.selectedInquiry.collectAsState()
    val context = LocalContext.current
    var replyText by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }

    val inquiry = selectedInquiry ?: return

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = inquiry.travelerName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = inquiry.message, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Check-in: ${inquiry.checkIn}")
                    Text("Check-out: ${inquiry.checkOut}")
                    Text("Guests: ${inquiry.guests}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:${inquiry.travelerPhone}")
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("ðŸ“ž Call Traveler")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val url = "https://wa.me/${inquiry.travelerPhone}?text=Hi ${inquiry.travelerName}, regarding your inquiry for Namma-HomeStay..."
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("WhatsApp Message")
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                label = { Text("Type a reply...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (replyText.isNotBlank()) {
                        viewModel.sendReply(inquiry.id, replyText) {
                            replyText = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Reply")
            }
        }
    }
}
