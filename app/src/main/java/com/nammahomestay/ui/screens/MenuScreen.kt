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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nammahomestay.ui.menu.DailyMenuViewModel
import com.nammahomestay.ui.theme.*
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

    var description by remember(todayMenu.description) { mutableStateOf(todayMenu.description) }
    var rateOverride by remember(todayMenu.dailyRateOverride) { mutableStateOf(todayMenu.dailyRateOverride?.toString() ?: "") }
    
    val mealOptions = listOf("Breakfast", "Lunch", "Dinner")
    var selectedMeals by remember(todayMenu.mealsAvailable) { mutableStateOf(todayMenu.mealsAvailable.toSet()) }

    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { pendingPhotoUri = it }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Today's Menu", fontWeight = FontWeight.Bold, color = TerraDark) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Banner
            Surface(
                color = TerraXL,
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Update what you're serving today. Travelers love seeing local specialties! This should take less than 1 minute.",
                        fontSize = 13.sp,
                        color = TerraDark,
                        lineHeight = 18.sp
                    )
                }
            }

            // Live Preview Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "🍽️ Today's Menu — Live Preview", fontWeight = FontWeight.Bold, color = BrownMedium)
                Surface(color = LeafLight, shape = RoundedCornerShape(8.dp)) {
                    Text("Live", color = Leaf, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            // Preview Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MealPreviewCard("🌅 Breakfast", description, "Breakfast" in selectedMeals, Modifier.weight(1f))
                MealPreviewCard("☀️ Lunch", description, "Lunch" in selectedMeals, Modifier.weight(1f))
                MealPreviewCard("🌙 Dinner", description, "Dinner" in selectedMeals, Modifier.weight(1f))
            }

            // Edit Form
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("✏️ Update Meal Plan", fontWeight = FontWeight.Bold, color = BrownMedium)
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.length <= 300) description = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        label = { Text("What's on the menu today?") },
                        supportingText = {
                            Text(text = "${description.length}/300", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                        }
                    )

                    Text("MEAL AVAILABILITY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        mealOptions.forEach { meal ->
                            FilterChip(
                                selected = selectedMeals.contains(meal),
                                onClick = {
                                    val current = selectedMeals.toMutableSet()
                                    if (current.contains(meal)) current.remove(meal) else current.add(meal)
                                    selectedMeals = current
                                },
                                label = { Text(meal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Terra,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text("FOOD PHOTO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMedium)
                    Card(
                        modifier = Modifier.fillMaxWidth().height(150.dp).clickable { photoLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Sand)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (pendingPhotoUri != null || !todayMenu.photoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = pendingPhotoUri ?: todayMenu.photoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📷", fontSize = 32.sp)
                                    Text("Tap to add food photo", fontSize = 14.sp, color = TextMedium)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.saveMenu(description, selectedMeals.toList(), rateOverride.toIntOrNull(), pendingPhotoUri) {
                                pendingPhotoUri = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Terra),
                        enabled = !loading
                    ) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        else Text("🍛 Update Menu", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = lastUpdatedText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = TextLight
            )
        }
    }
}

@Composable
fun MealPreviewCard(title: String, description: String, isAvailable: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Sand),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Terra, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (isAvailable && description.isNotBlank()) {
                val items = description.split(",").take(2)
                items.forEach { item ->
                    Surface(
                        color = TerraXL,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(item.trim(), fontSize = 11.sp, color = TerraDark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            } else {
                Text("Not set", fontSize = 12.sp, color = TextLight)
            }
        }
    }
}
