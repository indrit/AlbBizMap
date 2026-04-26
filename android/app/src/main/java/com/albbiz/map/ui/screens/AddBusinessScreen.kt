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
import com.albbiz.map.viewmodel.AddBusinessUiState
import com.albbiz.map.viewmodel.AddBusinessViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBusinessScreen(
    initialLocation: LatLng? = null,
    onBackClick: () -> Unit,
    onBusinessAdded: () -> Unit,
    viewModel: AddBusinessViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<BusinessCategory?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    var latitude by remember { mutableStateOf(initialLocation?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(initialLocation?.longitude?.toString() ?: "") }
    var showMapPicker by remember { mutableStateOf(false) }

    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    var isOpen24Hours by remember { mutableStateOf(false) }
    var workingHours by remember { mutableStateOf(mapOf<String, String>()) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris?.let { selectedImageUris = (selectedImageUris + it).take(1) }
    }

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri.value?.let { uri ->
                selectedImageUris = (selectedImageUris + uri).take(1)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            cameraImageUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AddBusinessUiState.Success -> {
                Toast.makeText(context, "Business registered successfully!", Toast.LENGTH_LONG).show()
                onBusinessAdded()
                viewModel.resetState()
            }
            is AddBusinessUiState.Error -> {
                Toast.makeText(
                    context,
                    (uiState as AddBusinessUiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Business") },
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

            // ── REQUIRED INFO ─────────────────────────────────────────
            SectionTitle("Required Information")

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

            // ── CATEGORY DROPDOWN ─────────────────────────────────────
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
                                        category.icon,
                                        null,
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
            SectionTitle("Location *")

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
            SectionTitle("Photo (Optional)")

            Button(
                onClick = { showImageSourceDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedImageUris.isEmpty()
            ) {
                Icon(Icons.Default.AddAPhoto, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedImageUris.isEmpty()) "Add Photo" else "Photo Added (1/1)")
            }

            if (selectedImageUris.isNotEmpty()) {
                Box(modifier = Modifier.size(100.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUris.first()),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { selectedImageUris = emptyList() },
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── SUBMIT ────────────────────────────────────────────────
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
                            val business = Business(
                                id = UUID.randomUUID().toString(),
                                name = name.trim(),
                                description = description.trim(),
                                category = selectedCategory!!.name,
                                address = address.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                website = website.trim(),
                                location = GeoPoint(lat, lng),
                                isOpen24Hours = isOpen24Hours,
                                workingHours = if (isOpen24Hours) emptyMap() else workingHours,
                                isActive = true,
                                rating = 0.0,
                                reviewCount = 0
                            )
                            viewModel.addBusiness(business, selectedImageUris)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = uiState !is AddBusinessUiState.Loading
            ) {
                if (uiState is AddBusinessUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registering...")
                } else {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register Business")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ── IMAGE SOURCE DIALOG ───────────────────────────────────────────
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Add Photo") },
            text = { Text("Choose photo source") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text("Gallery")
                }
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
                }) {
                    Text("Camera")
                }
            }
        )
    }

    // ── MAP PICKER DIALOG ─────────────────────────────────────────────
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