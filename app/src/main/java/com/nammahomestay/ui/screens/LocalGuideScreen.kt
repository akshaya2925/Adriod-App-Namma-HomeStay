package com.nammahomestay.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nammahomestay.data.LocalSpot
import com.nammahomestay.ui.guide.LocalGuideViewModel
import com.nammahomestay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LocalGuideScreen() {
    val viewModel: LocalGuideViewModel = viewModel()
    val spotsResult by viewModel.spotsResult.collectAsState()
    val spots = (spotsResult as? com.nammahomestay.utils.AppResult.Success)?.data ?: emptyList()
    val message by viewModel.message.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var spotToDelete by remember { mutableStateOf<LocalSpot?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (spotToDelete != null) {
        AlertDialog(
            onDismissRequest = { spotToDelete = null },
            title = { Text("Delete Spot?") },
            text = { Text("Are you sure you want to delete '${spotToDelete?.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    spotToDelete?.let { viewModel.deleteSpot(it.id) }
                    spotToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { spotToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Local Secret Spots 🗺️", fontWeight = FontWeight.Bold, color = BrownMedium, fontSize = 20.sp)
                        Text("Nearby attractions to share with your guests", fontSize = 12.sp, color = TextLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = Terra,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Spot")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().background(Cream).padding(innerPadding)) {
            if (spots.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Share your local secrets!\nAdd waterfalls, viewpoints, or local markets to help travelers explore.",
                        textAlign = TextAlign.Center,
                        color = TextMedium
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1), // Single column like the HTML layout in mobile-ish view
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(spots) { spot ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { /* Details */ },
                                    onLongClick = { spotToDelete = spot }
                                ),
                            colors = CardDefaults.cardColors(containerColor = Sand),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                Text(getCategoryIcon(spot.category), fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = spot.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                        if (spot.locationUrl.isNotBlank()) {
                                            Text(
                                                text = "View on Map ↗",
                                                fontSize = 11.sp,
                                                color = Terra,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spot.locationUrl))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        // Handle error
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    Text(
                                        text = "📍 ${spot.distance} · ${spot.bestTime}",
                                        fontSize = 11.sp,
                                        color = TextLight,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    if (spot.entryFee.isNotBlank() || spot.timings.isNotBlank()) {
                                        Text(
                                            text = "${if(spot.entryFee.isNotBlank()) "🎟️ " + spot.entryFee else ""} ${if(spot.timings.isNotBlank()) "⏰ " + spot.timings else ""}",
                                            fontSize = 11.sp,
                                            color = BrownMedium,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = spot.description,
                                        fontSize = 13.sp,
                                        color = TextMedium,
                                        modifier = Modifier.padding(top = 4.dp),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = Color.White
            ) {
                AddSpotForm(
                    loading = loading,
                    onSave = { name, category, desc, dist, bestTime, locUrl, fee, time, uri ->
                        viewModel.addSpot(name, category, desc, dist, bestTime, locUrl, fee, time, uri) {
                            showBottomSheet = false
                        }
                    }
                )
            }
        }
    }
}

fun getCategoryIcon(category: String): String {
    return when (category) {
        "Waterfall" -> "💧"
        "Viewpoint" -> "⛰️"
        "Beach" -> "🌊"
        "Market" -> "🛍️"
        "Temple" -> "🛕"
        "Farm" -> "🌾"
        "Experience" -> "🌾"
        else -> "📍"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpotForm(
    loading: Boolean,
    onSave: (String, String, String, String, String, String, String, String, Uri?) -> Unit
) {
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var bestTime by remember { mutableStateOf("") }
    var locationUrl by remember { mutableStateOf("") }
    var entryFee by remember { mutableStateOf("") }
    var timings by remember { mutableStateOf("") }

    var expandedCategory by remember { mutableStateOf(false) }
    val categories = listOf("Waterfall", "Viewpoint", "Beach", "Market", "Temple", "Farm", "Experience", "Other")

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { pendingPhotoUri = it }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Add New Secret Spot", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrownMedium)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Sand, RoundedCornerShape(12.dp))
                .clickable { photoLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (pendingPhotoUri != null) {
                AsyncImage(model = pendingPhotoUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Text("📷 Tap to add photo (Optional)", fontSize = 14.sp, color = TextMedium)
            }
        }

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Spot Name") }, modifier = Modifier.fillMaxWidth())

        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
                categories.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { category = option; expandedCategory = false })
                }
            }
        }

        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        OutlinedTextField(value = distance, onValueChange = { distance = it }, label = { Text("Distance (e.g. 5 km)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = bestTime, onValueChange = { bestTime = it }, label = { Text("Best Time (e.g. Sunset)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = locationUrl, onValueChange = { locationUrl = it }, label = { Text("Google Maps URL (Optional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = entryFee, onValueChange = { entryFee = it }, label = { Text("Entry Fee (Optional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = timings, onValueChange = { timings = it }, label = { Text("Timings (Optional)") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = { onSave(name, category, description, distance, bestTime, locationUrl, entryFee, timings, pendingPhotoUri) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Terra),
            enabled = !loading && name.isNotBlank()
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            else Text("Add Spot", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
