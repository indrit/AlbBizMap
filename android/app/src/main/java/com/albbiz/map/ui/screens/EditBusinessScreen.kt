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
import com.albbiz.map.data.JobPosting
import com.albbiz.map.data.Promotion
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
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

    var name by remember { mutableStateOf(business.name) }
    var description by remember { mutableStateOf(business.description) }
    var phone by remember { mutableStateOf(business.phone) }
    var email by remember { mutableStateOf(business.email) }
    var website by remember { mutableStateOf(business.website) }
    var address by remember { mutableStateOf(business.address) }
    var selectedCategory by remember {
        mutableStateOf(BusinessCategory.entries.find { it.name == business.category })
    }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var latitude by remember { mutableStateOf(business.location?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(business.location?.longitude?.toString() ?: "") }
    var isOpen24Hours by remember { mutableStateOf(business.isOpen24Hours) }
    var workingHours by remember { mutableStateOf(business.workingHours) }
    var jobs by remember { mutableStateOf(business.jobs.toMutableList()) }
    var showAddJobDialog by remember { mutableStateOf(false) }
    var promotions by remember { mutableStateOf(business.promotions.toMutableList()) }
    var showAddPromoDialog by remember { mutableStateOf(false) }
    var newPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { newPhotoUri = it } }

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraImageUri.value?.let { newPhotoUri = it } }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraImageUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is EditBusinessUiState.Success -> {
                Toast.makeText(context, "Business updated successfully!", Toast.LENGTH_LONG).show()
                onBusinessUpdated()
                viewModel.resetState()
            }
            is EditBusinessUiState.Error -> {
                Toast.makeText(context, (uiState as EditBusinessUiState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    // ── ADD JOB DIALOG ────────────────────────────────────────────────
    if (showAddJobDialog) {
        var jobTitle by remember { mutableStateOf("") }
        var jobDescription by remember { mutableStateOf("") }
        var jobType by remember { mutableStateOf("Full-time") }
        var jobSalary by remember { mutableStateOf("") }
        var jobTypeExpanded by remember { mutableStateOf(false) }
        val jobTypes = listOf("Full-time", "Part-time", "Contract")

        AlertDialog(
            onDismissRequest = { showAddJobDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Add Job Posting", fontWeight = FontWeight.Bold, color = MeTontRed)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        label = { Text("Job Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        )
                    )
                    ExposedDropdownMenuBox(
                        expanded = jobTypeExpanded,
                        onExpandedChange = { jobTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = jobType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Job Type *") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = jobTypeExpanded)
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MeTontRed
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = jobTypeExpanded,
                            onDismissRequest = { jobTypeExpanded = false }
                        ) {
                            jobTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = { jobType = type; jobTypeExpanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = jobDescription,
                        onValueChange = { jobDescription = it },
                        label = { Text("Description *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        )
                    )
                    OutlinedTextField(
                        value = jobSalary,
                        onValueChange = { jobSalary = it },
                        label = { Text("Salary (Optional)") },
                        placeholder = { Text("e.g. \$1,500/month") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (jobTitle.isBlank() || jobDescription.isBlank()) {
                            Toast.makeText(context, "Title and description are required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        jobs = (jobs + JobPosting(
                            title = jobTitle.trim(),
                            description = jobDescription.trim(),
                            type = jobType,
                            salary = jobSalary.trim().ifBlank { null }
                        )).toMutableList()
                        showAddJobDialog = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MeTontRed)
                ) { Text("Add Job", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showAddJobDialog = false }) {
                    Text("Cancel", color = MeTontGrey)
                }
            }
        )
    }

    // ── ADD PROMOTION DIALOG ──────────────────────────────────────────
    if (showAddPromoDialog) {
        var promoTitle by remember { mutableStateOf("") }
        var promoDescription by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddPromoDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Add Promotion", fontWeight = FontWeight.Bold, color = MeTontRed)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = promoTitle,
                        onValueChange = { promoTitle = it },
                        label = { Text("Promotion Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        )
                    )
                    OutlinedTextField(
                        value = promoDescription,
                        onValueChange = { promoDescription = it },
                        label = { Text("Description *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (promoTitle.isBlank() || promoDescription.isBlank()) {
                            Toast.makeText(context, "Title and description are required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        promotions = (promotions + Promotion(
                            title = promoTitle.trim(),
                            description = promoDescription.trim()
                        )).toMutableList()
                        showAddPromoDialog = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MeTontRed)
                ) { Text("Add Promotion", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showAddPromoDialog = false }) {
                    Text("Cancel", color = MeTontGrey)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Business",
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
                //.background(Color(0xFFF5F5F5))
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── BASIC INFO ────────────────────────────────────────
            SectionCard(title = "Basic Information") {
                RedOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Business Name *",
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
                        label = { Text("Category *") },
                        leadingIcon = { Icon(Icons.Default.Category, null, tint = MeTontRed) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            focusedLabelColor = MeTontRed
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
                                onClick = { selectedCategory = category; showCategoryDropdown = false }
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
                    supportingText = { Text("${description.length}/100", color = MeTontGrey) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MeTontRed,
                        cursorColor = MeTontRed
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }

            // ── LOCATION ──────────────────────────────────────────
            SectionCard(title = "Location") {
                RedOutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Full Address *",
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
                        label = { Text("Latitude *") },
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
                        label = { Text("Longitude *") },
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
                    Text("Pick Location from Map")
                }
            }

            // ── CONTACT ───────────────────────────────────────────
            SectionCard(title = "Contact Information") {
                RedOutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number *",
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )
                RedOutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email (Optional)",
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )
                RedOutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = "Website (Optional)",
                    icon = Icons.Default.Language,
                    keyboardType = KeyboardType.Uri
                )
            }

            // ── WORKING HOURS ─────────────────────────────────────
            SectionCard(title = "Working Hours") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Open 24/7", fontWeight = FontWeight.Medium)
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
                    WorkingHoursEditor(hours = workingHours, onHoursChanged = { workingHours = it })
                }
            }

            // ── PHOTO ─────────────────────────────────────────────
            SectionCard(title = "Photo") {
                val displayPhotoUrl = newPhotoUri?.toString() ?: business.photos.firstOrNull()
                if (displayPhotoUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(displayPhotoUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { newPhotoUri = null },
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MeTontRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeTontRed)
                ) {
                    Icon(Icons.Default.AddAPhoto, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (displayPhotoUrl != null) "Change Photo" else "Add Photo")
                }
            }

            // ── JOB POSTINGS ──────────────────────────────────────
            SectionCard(title = "Job Postings") {
                if (jobs.isEmpty()) {
                    Text(
                        "No job postings yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MeTontGrey
                    )
                } else {
                    jobs.forEachIndexed { index, job ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF3E5F5)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(job.title, fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                                    Text(
                                        "${job.type}${if (job.salary != null) " • ${job.salary}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MeTontGrey
                                    )
                                    Text(
                                        job.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        color = Color.Black.copy(alpha = 0.7f)
                                    )
                                }
                                IconButton(onClick = {
                                    jobs = jobs.toMutableList().also { it.removeAt(index) }
                                }) {
                                    Icon(Icons.Default.Delete, null, tint = MeTontRed)
                                }
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = { showAddJobDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MeTontRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeTontRed)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Job Posting")
                }
            }

            // ── PROMOTIONS ────────────────────────────────────────
            SectionCard(title = "Promotions & Deals") {
                if (promotions.isEmpty()) {
                    Text(
                        "No promotions yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MeTontGrey
                    )
                } else {
                    promotions.forEachIndexed { index, promo ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF9C4),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, Color(0xFFFBC02D)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        promo.title,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF57F17)
                                    )
                                    Text(
                                        promo.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        color = MeTontGrey
                                    )
                                }
                                IconButton(onClick = {
                                    promotions = promotions.toMutableList().also { it.removeAt(index) }
                                }) {
                                    Icon(Icons.Default.Delete, null, tint = MeTontRed)
                                }
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = { showAddPromoDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MeTontRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeTontRed)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Promotion")
                }
            }

            // ── SAVE BUTTON ───────────────────────────────────────
            Button(
                onClick = {
                    val lat = latitude.toDoubleOrNull()
                    val lng = longitude.toDoubleOrNull()
                    when {
                        name.isBlank() -> Toast.makeText(context, "Business name is required", Toast.LENGTH_SHORT).show()
                        selectedCategory == null -> Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
                        description.isBlank() -> Toast.makeText(context, "Description is required", Toast.LENGTH_SHORT).show()
                        address.isBlank() -> Toast.makeText(context, "Address is required", Toast.LENGTH_SHORT).show()
                        phone.isBlank() -> Toast.makeText(context, "Phone number is required", Toast.LENGTH_SHORT).show()
                        lat == null || lng == null -> Toast.makeText(context, "Please enter valid coordinates", Toast.LENGTH_SHORT).show()
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
                                workingHours = if (isOpen24Hours) emptyMap() else workingHours,
                                jobs = jobs,
                                promotions = promotions
                            )
                            viewModel.updateBusiness(updatedBusiness, newPhotoUri)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeTontRed,
                    contentColor = Color.White
                ),
                enabled = uiState !is EditBusinessUiState.Loading
            ) {
                if (uiState is EditBusinessUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ── IMAGE SOURCE DIALOG ───────────────────────────────────────────
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Change Photo", fontWeight = FontWeight.Bold) },
            text = { Text("Choose photo source", color = MeTontGrey) },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) { Text("Gallery", color = MeTontRed) }
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
                }) { Text("Camera", color = MeTontRed) }
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