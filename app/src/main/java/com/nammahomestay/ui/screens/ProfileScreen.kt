package com.nammahomestay.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nammahomestay.data.HostProfile
import com.nammahomestay.ui.profile.ProfileViewModel
import com.nammahomestay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = viewModel()
    val profileResult by viewModel.profileResult.collectAsState()
    val profileData = (profileResult as? com.nammahomestay.utils.AppResult.Success)?.data ?: HostProfile()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    var homestayName by remember(profileData.homestayName) { mutableStateOf(profileData.homestayName) }
    var village by remember(profileData.village) { mutableStateOf(profileData.village) }
    var description by remember(profileData.description) { mutableStateOf(profileData.description) }
    var whatsapp by remember(profileData.whatsapp) { mutableStateOf(profileData.whatsapp) }

    val checklistLabels = listOf(
        "roomsClean" to "Clean bedsheets & pillows",
        "workingFan" to "Working fan / AC",
        "toiletsClean" to "Clean toilet & bathroom",
        "mosquitoNets" to "Mosquito protection",
        "drinkingWater" to "Safe drinking water available",
        "chargingPoints" to "Phone charging points in room"
    )

    var checklistState by remember(profileData.verificationChecklist) { 
        mutableStateOf(profileData.verificationChecklist) 
    }

    val availableLanguages = listOf("Kannada", "English", "Hindi", "Tulu")
    var selectedLanguages by remember(profileData.languages) { mutableStateOf(profileData.languages.toSet()) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadPhoto(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Home Profile", fontWeight = FontWeight.Bold, color = TerraDark) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        val updatedProfile = HostProfile(
                            homestayName = homestayName,
                            village = village,
                            description = description,
                            photoUrls = profileData.photoUrls,
                            verificationChecklist = checklistState,
                            whatsapp = whatsapp,
                            languages = selectedLanguages.toList(),
                            isProfileComplete = true
                        )
                        viewModel.saveProfile(updatedProfile)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Terra),
                    enabled = !loading
                ) {
                    Text("💾 Save Profile", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Host Details
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = CardDefaults.outlinedCardBorder()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("👤 Host Details", fontWeight = FontWeight.Bold, color = BrownMedium)
                        
                        OutlinedTextField(
                            value = homestayName,
                            onValueChange = { homestayName = it },
                            label = { Text("Host Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = village,
                            onValueChange = { village = it },
                            label = { Text("Village / Location") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("About Your Home-Stay") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }

            // Contact Details
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = CardDefaults.outlinedCardBorder()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("📞 Contact Details", fontWeight = FontWeight.Bold, color = BrownMedium)
                        
                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("WhatsApp Number") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Languages Spoken", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableLanguages.forEach { lang ->
                                FilterChip(
                                    selected = selectedLanguages.contains(lang),
                                    onClick = {
                                        val current = selectedLanguages.toMutableSet()
                                        if (current.contains(lang)) current.remove(lang) else current.add(lang)
                                        selectedLanguages = current
                                    },
                                    label = { Text(lang) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Terra, selectedLabelColor = Color.White)
                                )
                            }
                        }
                    }
                }
            }

            // Photos Section
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = CardDefaults.outlinedCardBorder()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📸 Room Photos", fontWeight = FontWeight.Bold, color = BrownMedium, modifier = Modifier.padding(bottom = 12.dp))
                        
                        // Photo Upload Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Sand, RoundedCornerShape(12.dp))
                                .clickable { photoLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📷", fontSize = 32.sp)
                                Text("Tap to add room photos", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextMedium)
                            }
                        }

                        LazyRow(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(profileData.photoUrls) { url ->
                                Box(modifier = Modifier.size(100.dp)) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                                    )
                                    IconButton(
                                        onClick = { viewModel.deletePhoto(url) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Checklist
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = CardDefaults.outlinedCardBorder()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("✅ Verification Checklist", fontWeight = FontWeight.Bold, color = BrownMedium, modifier = Modifier.padding(bottom = 12.dp))
                        checklistLabels.forEach { (key, label) ->
                            val isChecked = checklistState[key] == true
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(if (isChecked) LeafLight else Sand, RoundedCornerShape(10.dp))
                                    .clickable {
                                        checklistState = checklistState.toMutableMap().apply { put(key, !isChecked) }
                                    }
                                    .padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(if (isChecked) Leaf else Color.Transparent, RoundedCornerShape(5.dp))
                                        .background(if (!isChecked) Color.White.copy(0.5f) else Color.Transparent)
                                ) {
                                    if (isChecked) Text("✓", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Center))
                                }
                                Text(text = label, modifier = Modifier.padding(start = 12.dp), fontSize = 13.sp, color = TextMedium, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
