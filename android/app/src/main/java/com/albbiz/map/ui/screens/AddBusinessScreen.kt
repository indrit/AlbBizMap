// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.albbiz.map.data.Business
import com.albbiz.map.data.BusinessCategory
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
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
    val strings = LocalAppStrings.current

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
    ) { uris -> uris?.let { selectedImageUris = (selectedImageUris + it).take(1) } }

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraImageUri.value?.let { uri ->
            selectedImageUris = (selectedImageUris + uri).take(1)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraImageUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, strings.cameraPermissionRequired, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AddBusinessUiState.Success -> {
                Toast.makeText(context, strings.businessRegistered, Toast.LENGTH_LONG).show()
                onBusinessAdded()
                viewModel.resetState()
            }
            is AddBusinessUiState.Error -> {
                Toast.makeText(context, (uiState as AddBusinessUiState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.registerBusiness,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MeTontRed)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                //.background(Color(0xFFFFF8F0))
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── REQUIRED INFO ─────────────────────────────────────
            SectionCard(title = strings.requiredInformation) {
                RedOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = strings.businessName,
                    icon = Icons.Default.Store
                )

                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.displayName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.category) },
                        leadingIcon = { Icon(Icons.Default.Category, null, tint = MeTontRed) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            focusedLabelColor = MeTontRed,
                            cursorColor = MeTontRed
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        BusinessCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(category.icon, null, modifier = Modifier.size(24.dp))
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
                    label = { Text(strings.eventDescription) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    supportingText = { Text("${description.length}/100", color = MeTontGrey) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MeTontRed,
                        focusedLabelColor = MeTontRed,
                        cursorColor = MeTontRed
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }

            // ── LOCATION ──────────────────────────────────────────
            SectionCard(title = strings.locationSection) {
                RedOutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = strings.fullAddress,
                    icon = Icons.Default.LocationOn,
                    singleLine = false
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text(strings.latitude) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text(strings.longitude) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                OutlinedButton(
                    onClick = { showMapPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MeTontRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeTontRed)
                ) {
                    Icon(Icons.Default.Map, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.pickLocationFromMap)
                }
            }

            // ── CONTACT ───────────────────────────────────────────
            SectionCard(title = strings.contactInformation) {
                RedOutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = strings.phoneNumber,
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )
                RedOutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = strings.emailOptional,
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )
                RedOutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = strings.websiteOptional,
                    icon = Icons.Default.Language,
                    keyboardType = KeyboardType.Uri
                )
            }

            // ── WORKING HOURS ─────────────────────────────────────
            SectionCard(title = strings.workingHoursSection) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.open247, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isOpen24Hours,
                        onCheckedChange = { isOpen24Hours = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MeTontRed
                        )
                    )
                }
                if (!isOpen24Hours) {
                    WorkingHoursEditor(
                        hours = workingHours,
                        onHoursChanged = { workingHours = it }
                    )
                }
            }

            // ── PHOTO ─────────────────────────────────────────────
            SectionCard(title = strings.photoOptional) {
                if (selectedImageUris.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUris.first()),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImageUris = emptyList() },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, null, tint = MeTontRed)
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showImageSourceDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedImageUris.isEmpty(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MeTontRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeTontRed)
                ) {
                    Icon(Icons.Default.AddAPhoto, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedImageUris.isEmpty()) strings.addPhoto else strings.photoAdded)
                }
            }

            // ── SUBMIT ────────────────────────────────────────────
            Button(
                onClick = {
                    val lat = latitude.toDoubleOrNull()
                    val lng = longitude.toDoubleOrNull()
                    when {
                        name.isBlank() -> Toast.makeText(context, strings.businessNameRequired, Toast.LENGTH_SHORT).show()
                        selectedCategory == null -> Toast.makeText(context, strings.selectCategory, Toast.LENGTH_SHORT).show()
                        description.isBlank() -> Toast.makeText(context, strings.descriptionRequired, Toast.LENGTH_SHORT).show()
                        address.isBlank() -> Toast.makeText(context, strings.addressRequired, Toast.LENGTH_SHORT).show()
                        phone.isBlank() -> Toast.makeText(context, strings.phoneRequired, Toast.LENGTH_SHORT).show()
                        lat == null || lng == null -> Toast.makeText(context, strings.validCoordinates, Toast.LENGTH_SHORT).show()
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
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeTontRed,
                    contentColor = Color.White
                ),
                enabled = uiState !is AddBusinessUiState.Loading
            ) {
                if (uiState is AddBusinessUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.registering)
                } else {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.registerBusinessButton, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ── IMAGE SOURCE DIALOG ───────────────────────────────────────────
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text(strings.addPhoto, fontWeight = FontWeight.Bold) },
            text = { Text(strings.choosePhotoSource) },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text(strings.gallery, color = MeTontRed)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                            val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            cameraImageUri.value = uri
                            cameraLauncher.launch(uri)
                        }
                        else -> permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Text(strings.camera, color = MeTontRed)
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

// ── REUSABLE COMPONENTS ───────────────────────────────────────────────

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                color = MeTontRed,
                fontSize = 14.sp
            )
            content()
        }
    }
}

@Composable
fun RedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = MeTontRed) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MeTontRed,
            focusedLabelColor = MeTontRed,
            cursorColor = MeTontRed
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}