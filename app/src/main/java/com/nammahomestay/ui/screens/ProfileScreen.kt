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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nammahomestay.data.HostProfile
import com.nammahomestay.ui.profile.ProfileViewModel

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
    var numRooms by remember(profileData.numRooms) { mutableStateOf(if (profileData.numRooms > 0) profileData.numRooms.toString() else "") }
    var maxGuests by remember(profileData.maxGuests) { mutableStateOf(if (profileData.maxGuests > 0) profileData.maxGuests.toString() else "") }
    var roomType by remember(profileData.roomType) { mutableStateOf(profileData.roomType) }
    var description by remember(profileData.description) { mutableStateOf(profileData.description) }
    var whatsapp by remember(profileData.whatsapp) { mutableStateOf(profileData.whatsapp) }

    val checklistLabels = listOf(
        "roomsClean" to "Rooms are clean and swept",
        "toiletsClean" to "Toilets are clean",
        "freshLinen" to "Fresh bed linen provided",
        "drinkingWater" to "Drinking water available",
        "mosquitoNets" to "Mosquito nets provided",
        "noDrains" to "No open drains near entrance"
    )

    var checklistState by remember(profileData.verificationChecklist) { 
        mutableStateOf(checklistLabels.associate { it.first to (profileData.verificationChecklist[it.first] == true) }) 
    }

    val availableLanguages = listOf("Kannada", "Tulu", "English", "Hindi")
    var selectedLanguages by remember(profileData.languages) { mutableStateOf(profileData.languages.toSet()) }

    var expandedRoomType by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadPhoto(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Button(
                onClick = {
                    val updatedProfile = HostProfile(
                        homestayName = homestayName,
                        village = village,
                        numRooms = numRooms.toIntOrNull() ?: 0,
                        maxGuests = maxGuests.toIntOrNull() ?: 0,
                        roomType = roomType,
                        description = description,
                        photoUrls = profileData.photoUrls,
                        verificationChecklist = checklistState,
                        whatsapp = whatsapp,
                        languages = selectedLanguages.toList()
                    )
                    viewModel.saveProfile(updatedProfile)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                enabled = !loading
            ) {
                Text("Save Profile")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                if (loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                }
            }

            // Photos Section
            item {
                SectionHeader("PHOTOS")
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable {
                                    if (profileData.photoUrls.size < 6) {
                                        photoLauncher.launch("image/*")
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "Add Photo")
                            }
                        }
                    }
                    items(profileData.photoUrls) { url ->
                        Box(modifier = Modifier.size(100.dp)) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            IconButton(
                                onClick = { viewModel.deletePhoto(url) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Property Details
            item {
                SectionHeader("PROPERTY DETAILS")
                OutlinedTextField(
                    value = homestayName,
                    onValueChange = { homestayName = it },
                    label = { Text("Homestay Name") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    label = { Text("Village/Location") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = numRooms,
                        onValueChange = { numRooms = it },
                        label = { Text("Num Rooms") },
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )
                    OutlinedTextField(
                        value = maxGuests,
                        onValueChange = { maxGuests = it },
                        label = { Text("Max Guests") },
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                }
                ExposedDropdownMenuBox(
                    expanded = expandedRoomType,
                    onExpandedChange = { expandedRoomType = !expandedRoomType },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = roomType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Room Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoomType) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRoomType,
                        onDismissRequest = { expandedRoomType = false }
                    ) {
                        listOf("Private Room", "Shared Room", "Entire Home").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    roomType = option
                                    expandedRoomType = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 200) description = it },
                    label = { Text("Short Description") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    minLines = 3,
                    supportingText = {
                        Text(
                            text = "${description.length}/200",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                )
            }

            // Checklist
            item {
                SectionHeader("CLEANLINESS CHECKLIST")
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        checklistLabels.forEach { (key, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        checklistState = checklistState.toMutableMap().apply {
                                            put(key, !(this[key] ?: false))
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = checklistState[key] == true,
                                    onCheckedChange = { isChecked ->
                                        checklistState = checklistState.toMutableMap().apply { put(key, isChecked) }
                                    }
                                )
                                Text(text = label, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }

            // Contact
            item {
                SectionHeader("CONTACT")
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp Number") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                Text(
                    text = "Languages Spoken",
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableLanguages.forEach { lang ->
                        FilterChip(
                            selected = selectedLanguages.contains(lang),
                            onClick = {
                                val current = selectedLanguages.toMutableSet()
                                if (current.contains(lang)) current.remove(lang) else current.add(lang)
                                selectedLanguages = current
                            },
                            label = { Text(lang) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
    )
}
