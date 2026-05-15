package com.nammahomestay.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nammahomestay.ui.menu.DailyMenuViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen() {
    val viewModel: DailyMenuViewModel = viewModel()
    val menuResult by viewModel.menuResult.collectAsState()
    val todayMenu = (menuResult as? com.nammahomestay.utils.AppResult.Success)?.data ?: com.nammahomestay.data.DailyMenu()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()
    val lastUpdatedText by viewModel.lastUpdatedText.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Capture start time for 60-second speedy save logic
    val startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    var description by remember(todayMenu.description) { mutableStateOf(todayMenu.description) }
    var rateOverride by remember(todayMenu.dailyRateOverride) { mutableStateOf(todayMenu.dailyRateOverride?.toString() ?: "") }
    
    val mealOptions = listOf("Breakfast", "Lunch", "Dinner")
    var selectedMeals by remember(todayMenu.mealsAvailable) { mutableStateOf(todayMenu.mealsAvailable.toSet()) }

    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { pendingPhotoUri = it }
    }

    val todayHeader = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }

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
            // Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = todayHeader,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("TODAY'S SPECIAL", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 300) description = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 4,
                label = { Text("What's for breakfast, lunch, dinner today?") },
                supportingText = {
                    Text(
                        text = "${description.length}/300",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("FOOD PHOTO", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 8.dp)
                    .clickable { photoLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (pendingPhotoUri != null) {
                        AsyncImage(
                            model = pendingPhotoUri,
                            contentDescription = "Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (!todayMenu.photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = todayMenu.photoUrl,
                            contentDescription = "Current Food Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Tap to Add Image",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("MEAL AVAILABILITY", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mealOptions.forEach { meal ->
                    FilterChip(
                        selected = selectedMeals.contains(meal),
                        onClick = {
                            val current = selectedMeals.toMutableSet()
                            if (current.contains(meal)) current.remove(meal) else current.add(meal)
                            selectedMeals = current
                        },
                        label = { Text(meal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = rateOverride,
                onValueChange = { rateOverride = it },
                label = { Text("Daily Rate Override (Optional)") },
                leadingIcon = { Text("â‚¹", modifier = Modifier.padding(start = 12.dp, end = 4.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val rate = rateOverride.toIntOrNull()
                    viewModel.saveMenu(
                        description = description,
                        meals = selectedMeals.toList(),
                        rateOverride = rate,
                        photoUri = pendingPhotoUri,
                        onSpeedySave = {
                            val elapsed = (System.currentTimeMillis() - startTime) / 1000
                            if (elapsed < 60) {
                                viewModel.showMessage("Menu updated in $elapsed seconds âš¡")
                            } else {
                                viewModel.showMessage("Menu updated successfully")
                            }
                            pendingPhotoUri = null
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Update Menu")
                }
            }

            Text(
                text = lastUpdatedText,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
    }
}
