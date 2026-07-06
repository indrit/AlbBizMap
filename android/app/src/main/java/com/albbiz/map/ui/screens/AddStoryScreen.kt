// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.albbiz.map.data.Business
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.albbiz.map.ui.theme.TierGold
import com.albbiz.map.viewmodel.AddStoryUiState
import com.albbiz.map.viewmodel.StoriesViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoryScreen(
    onBackClick: () -> Unit,
    onStoryPosted: () -> Unit,
    mapViewModel: MapViewModel = viewModel(),
    storiesViewModel: StoriesViewModel = viewModel()
) {
    val context = LocalContext.current
    val addStoryState by storiesViewModel.addStoryState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Get businesses owned by current user
    val allBusinesses by mapViewModel.businesses.collectAsState()
    val myBusinesses = remember(allBusinesses, currentUserId) {
        allBusinesses.filter { it.ownerId == currentUserId }
    }

    // Form state
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var storyText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("user") }
    var locationText by remember { mutableStateOf("") }
    var selectedBusiness by remember { mutableStateOf<Business?>(null) }
    var showBusinessDropdown by remember { mutableStateOf(false) }
    var showMaxPhotosError by remember { mutableStateOf(false) }

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val remaining = 10 - selectedPhotos.size
        if (uris.size > remaining) {
            showMaxPhotosError = true
            selectedPhotos = selectedPhotos + uris.take(remaining)
        } else {
            selectedPhotos = selectedPhotos + uris
        }
    }

    // Handle success
    LaunchedEffect(addStoryState) {
        if (addStoryState is AddStoryUiState.Success) {
            storiesViewModel.resetAddStoryState()
            onStoryPosted()
        }
    }

    val storyTypes = listOf(
        "user" to "👤 User",
        "business" to "🏢 Business",
        "community" to "📢 Community",
        "sponsored" to "⭐ Sponsored"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Story", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            storiesViewModel.addStory(
                                text = storyText,
                                type = selectedType,
                                category = selectedBusiness?.category ?: selectedType,
                                location = locationText,
                                photoUris = selectedPhotos,
                                businessId = selectedBusiness?.id,
                                businessName = selectedBusiness?.name,
                                isSponsored = selectedType == "sponsored"
                            )
                        },
                        enabled = selectedPhotos.isNotEmpty() &&
                                addStoryState !is AddStoryUiState.Loading
                    ) {
                        Text(
                            "Post",
                            color = if (selectedPhotos.isNotEmpty()) Color.White
                            else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MeTontRed)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── PHOTO SECTION ─────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Photos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            "${selectedPhotos.size}/10",
                            style = MaterialTheme.typography.labelSmall,
                            color = MeTontGrey
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Photo grid
                    if (selectedPhotos.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(selectedPhotos) { uri ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Remove photo button
                                    IconButton(
                                        onClick = {
                                            selectedPhotos = selectedPhotos - uri
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Add photos button
                    if (selectedPhotos.size < 10) {
                        OutlinedButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MeTontRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MeTontRed)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Photos")
                        }
                    }

                    if (showMaxPhotosError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Maximum 10 photos per story",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // ── STORY TYPE ────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Story Type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(storyTypes) { (type, label) ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                    if (type != "business" && type != "sponsored") {
                                        selectedBusiness = null
                                    }
                                },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (type == "sponsored") TierGold else MeTontRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── BUSINESS SELECTOR (for business/sponsored types) ──────
            if (selectedType == "business" || selectedType == "sponsored") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Select Business",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (myBusinesses.isEmpty()) {
                            Text(
                                "You don't own any businesses yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MeTontGrey
                            )
                        } else {
                            ExposedDropdownMenuBox(
                                expanded = showBusinessDropdown,
                                onExpandedChange = { showBusinessDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedBusiness?.name ?: "Select a business",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBusinessDropdown)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MeTontRed,
                                        cursorColor = MeTontRed
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = showBusinessDropdown,
                                    onDismissRequest = { showBusinessDropdown = false }
                                ) {
                                    myBusinesses.forEach { business ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(business.name, fontWeight = FontWeight.Medium)
                                                    Text(business.category, fontSize = 11.sp, color = MeTontRed)
                                                }
                                            },
                                            onClick = {
                                                selectedBusiness = business
                                                showBusinessDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── CAPTION ───────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Caption",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = storyText,
                        onValueChange = { storyText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Write a caption...", color = MeTontGrey) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── LOCATION ──────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Location",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = locationText,
                        onValueChange = { locationText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Tirana, Albania", color = MeTontGrey) },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, null, tint = MeTontRed)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── POST BUTTON ───────────────────────────────────────────
            Button(
                onClick = {
                    storiesViewModel.addStory(
                        text = storyText,
                        type = selectedType,
                        category = selectedBusiness?.category ?: selectedType,
                        location = locationText,
                        photoUris = selectedPhotos,
                        businessId = selectedBusiness?.id,
                        businessName = selectedBusiness?.name,
                        isSponsored = selectedType == "sponsored"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                enabled = selectedPhotos.isNotEmpty() &&
                        addStoryState !is AddStoryUiState.Loading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeTontRed,
                    contentColor = Color.White
                )
            ) {
                if (addStoryState is AddStoryUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Post Story", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // ── ERROR MESSAGE ─────────────────────────────────────────
            if (addStoryState is AddStoryUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    (addStoryState as AddStoryUiState.Error).message,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}