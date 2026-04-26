// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.albbiz.map.data.Business
import com.albbiz.map.data.BusinessCategory
import com.albbiz.map.viewmodel.EditBusinessUiState
import com.albbiz.map.viewmodel.EditBusinessViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBusinessScreen(
    business: Business,
    onBackClick: () -> Unit,
    onBusinessUpdated: () -> Unit,
    viewModel: EditBusinessViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Pre-fill all fields with existing business data
    var name by remember { mutableStateOf(business.name) }
    var description by remember { mutableStateOf(business.description) }
    var phone by remember { mutableStateOf(business.phone) }
    var email by remember { mutableStateOf(business.email) }
    var website by remember { mutableStateOf(business.website) }
    var address by remember { mutableStateOf(business.address) }
    var selectedCategory by remember {
        mutableStateOf(
            BusinessCategory.entries.find { it.name == business.category }
        )
    }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var latitude by remember {
        mutableStateOf(business.location?.latitude?.toString() ?: "")
    }
    var longitude by remember {
        mutableStateOf(business.location?.longitude?.toString() ?: "")
    }
    var isOpen24Hours by remember { mutableStateOf(business.isOpen24Hours) }
    var workingHours by remember { mutableStateOf(business.workingHours) }

    // Photo state — start with existing photo
    var newPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { newPhotoUri = it } }

    // Camera
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraImageUri.value?.let { newPhotoUri = it }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
            )
            cameraImageUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is EditBusinessUiState.Success -> {
                Toast.makeText(context, "Business updated successfully!", Toast.LENGTH_LONG).show()
                onBusinessUpdated()
                viewModel.resetState()
            }
            is EditBusinessUiState.Error -> {
                Toast.makeText(
                    context,
                    (uiState as EditBusinessUiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Business") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── BASIC INFO ────────────────────────────────────────────
            SectionTitle("Basic Information")

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Business Name *") },
                leadingIcon = { Icon(Icons.Default.Store, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            // ── CATEGORY ──────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory?.displayName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    leadingIcon = { Icon(Icons.Default.Category, null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    BusinessCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        category.icon, null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(category.displayName)
                                }
                            },
                            onClick = {
                                selectedCategory = category
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 100) description = it },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                supportingText = { Text("${description.length}/100") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // ── LOCATION ──────────────────────────────────────────────
            SectionTitle("Location")

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Full Address *") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            OutlinedButton(
                onClick = { showMapPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Map, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick Location from Map")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // ── CONTACT ───────────────────────────────────────────────
            SectionTitle("Contact Information")

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number *") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (Optional)") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = website,
                onValueChange = { website = it },
                label = { Text("Website (Optional)") },
                leadingIcon = { Icon(Icons.Default.Language, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                )
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // ── WORKING HOURS ─────────────────────────────────────────
            SectionTitle("Working Hours")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Open 24/7")
                Switch(
                    checked = isOpen24Hours,
                    onCheckedChange = { isOpen24Hours = it }
                )
            }

            if (!isOpen24Hours) {
                WorkingHoursEditor(
                    hours = workingHours,
                    onHoursChanged = { workingHours = it }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // ── PHOTO ─────────────────────────────────────────────────
            SectionTitle("Photo")

            // Show current photo or new picked photo
            val displayPhotoUrl = newPhotoUri?.toString()
                ?: business.photos.firstOrNull()

            if (displayPhotoUrl != null) {
                Box(modifier = Modifier.size(120.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(displayPhotoUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { newPhotoUri = null },
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Default.Close, null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { showImageSourceDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AddAPhoto, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (displayPhotoUrl != null) "Change Photo" else "Add Photo")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── SAVE BUTTON ───────────────────────────────────────────
            Button(
                onClick = {
                    val lat = latitude.toDoubleOrNull()
                    val lng = longitude.toDoubleOrNull()

                    when {
                        name.isBlank() -> {
                            Toast.makeText(context, "Business name is required", Toast.LENGTH_SHORT).show()
                        }
                        selectedCategory == null -> {
                            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
                        }
                        description.isBlank() -> {
                            Toast.makeText(context, "Description is required", Toast.LENGTH_SHORT).show()
                        }
                        address.isBlank() -> {
                            Toast.makeText(context, "Address is required", Toast.LENGTH_SHORT).show()
                        }
                        phone.isBlank() -> {
                            Toast.makeText(context, "Phone number is required", Toast.LENGTH_SHORT).show()
                        }
                        lat == null || lng == null -> {
                            Toast.makeText(context, "Please enter valid coordinates", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val updatedBusiness = business.copy(
                                name = name.trim(),
                                description = description.trim(),
                                category = selectedCategory!!.name,
                                address = address.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                website = website.trim(),
                                location = GeoPoint(lat, lng),
                                isOpen24Hours = isOpen24Hours,
                                workingHours = if (isOpen24Hours) emptyMap() else workingHours
                            )
                            viewModel.updateBusiness(updatedBusiness, newPhotoUri)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = uiState !is EditBusinessUiState.Loading
            ) {
                if (uiState is EditBusinessUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ── IMAGE SOURCE DIALOG ───────────────────────────────────────────
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Change Photo") },
            text = { Text("Choose photo source") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) { Text("Gallery") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) -> {
                            val file = File(
                                context.cacheDir,
                                "camera_${System.currentTimeMillis()}.jpg"
                            )
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            cameraImageUri.value = uri
                            cameraLauncher.launch(uri)
                        }
                        else -> permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) { Text("Camera") }
            }
        )
    }

    // ── MAP PICKER ────────────────────────────────────────────────────
    if (showMapPicker) {
        LocationPickerDialog(
            initialLocation = LatLng(
                latitude.toDoubleOrNull() ?: 41.3275,
                longitude.toDoubleOrNull() ?: 19.8187
            ),
            onLocationSelected = { latLng ->
                latitude = latLng.latitude.toString()
                longitude = latLng.longitude.toString()
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    }
}