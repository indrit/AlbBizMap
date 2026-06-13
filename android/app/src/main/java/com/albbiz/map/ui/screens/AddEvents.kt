/*

// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

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
import coil.compose.rememberAsyncImagePainter
import com.albbiz.map.data.Event
import com.albbiz.map.data.EventsRepository
import com.albbiz.map.ui.LocalAppStrings
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    onBackClick: () -> Unit,
    onEventAdded: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { EventsRepository() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var websiteUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Cultural") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Date fields
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    val strings = LocalAppStrings.current


    val categories = listOf(
        "Cultural", "Concert", "Festival",
        "Community", "Sports", "Food", "Other"
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { selectedImageUri = it } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LocalAppStrings.current.submitEvent) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── EVENT PHOTO ───────────────────────────────────────
            SectionTitle(LocalAppStrings.current.eventPhotoSection)

            if (selectedImageUri != null) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Default.Close, null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AddAPhoto, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedImageUri != null) LocalAppStrings.current.changePhoto else LocalAppStrings.current.addPhoto)

            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // ── EVENT DETAILS ─────────────────────────────────────
            SectionTitle(LocalAppStrings.current.eventDetailsSection)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(LocalAppStrings.current.eventTitle) },
                leadingIcon = { Icon(Icons.Default.Event, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(LocalAppStrings.current.eventCategory) },
                    leadingIcon = { Icon(Icons.Default.Category, null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
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
                onValueChange = { description = it },
                label = { Text(LocalAppStrings.current.eventDescription) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // ── DATE ──────────────────────────────────────────────
            SectionTitle(LocalAppStrings.current.eventDateSection)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = day,
                    onValueChange = { if (it.length <= 2) day = it },
                    label = { Text("Day") },
                    placeholder = { Text("DD") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = month,
                    onValueChange = { if (it.length <= 2) month = it },
                    label = { Text("Month") },
                    placeholder = { Text("MM") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { if (it.length <= 4) year = it },
                    label = { Text("Year") },
                    placeholder = { Text("YYYY") },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // ── LOCATION & WEBSITE ────────────────────────────────
            SectionTitle(LocalAppStrings.current.eventLocationSection)

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text(LocalAppStrings.current.eventLocation) },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                placeholder = { Text("e.g. Tirana, Albania") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = websiteUrl,
                onValueChange = { websiteUrl = it },
                label = { Text(LocalAppStrings.current.eventWebsite) },
                leadingIcon = { Icon(Icons.Default.Language, null) },
                placeholder = { Text("https://...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── SUBMIT ────────────────────────────────────────────
            Button(
                onClick = {
                    // Validate
                    val dayInt = day.toIntOrNull()
                    val monthInt = month.toIntOrNull()
                    val yearInt = year.toIntOrNull()

                    when {
                        title.isBlank() -> {
                            Toast.makeText(context, strings.eventTitleRequired, Toast.LENGTH_SHORT).show()
                        }
                        description.isBlank() -> {
                            Toast.makeText(context, strings.eventDescriptionRequired, Toast.LENGTH_SHORT).show()
                        }
                        locationName.isBlank() -> {
                            Toast.makeText(context, strings.eventLocationRequired, Toast.LENGTH_SHORT).show()
                        }
                        dayInt == null || monthInt == null || yearInt == null -> {
                            Toast.makeText(context, strings.eventInvalidDate, Toast.LENGTH_SHORT).show()
                        }
                        dayInt !in 1..31 || monthInt !in 1..12 || yearInt < 2024 -> {
                            Toast.makeText(context, strings.eventInvalidDate, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            // Build timestamp from date
                            val calendar = Calendar.getInstance().apply {
                                set(yearInt, monthInt - 1, dayInt, 0, 0, 0)
                            }

                            val event = Event(
                                id = UUID.randomUUID().toString(),
                                title = title.trim(),
                                description = description.trim(),
                                locationName = locationName.trim(),
                                date = calendar.timeInMillis,
                                category = selectedCategory,
                                organizerId = userId,
                                websiteUrl = websiteUrl.trim().ifBlank { null }
                            )

                            isLoading = true
                            scope.launch {
                                repository.addEvent(event, selectedImageUri)
                                    .onSuccess {
                                        Toast.makeText(
                                            context,
                                            strings.eventSubmitSuccess,
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onEventAdded()
                                    }
                                    .onFailure { e ->
                                        Toast.makeText(
                                            context,
                                            "${strings.eventSubmitFailed}: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LocalAppStrings.current.submitting)
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LocalAppStrings.current.submitEventButton, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
 */


 */

// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

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
import coil.compose.rememberAsyncImagePainter
import com.albbiz.map.data.Event
import com.albbiz.map.data.EventsRepository
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontRed
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    onBackClick: () -> Unit,
    onEventAdded: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { EventsRepository() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val strings = LocalAppStrings.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var websiteUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Cultural") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    val categories = listOf(
        "Cultural", "Concert", "Festival",
        "Community", "Sports", "Food", "Other"
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { selectedImageUri = it } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.submitEvent,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MeTontRed
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── EVENT PHOTO ───────────────────────────────────────
            SectionCard(title = strings.eventPhotoSection) {
                if (selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, null, tint = MeTontRed)
                        }
                    }
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MeTontRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeTontRed)
                ) {
                    Icon(Icons.Default.AddAPhoto, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedImageUri != null) strings.changePhoto else strings.addPhoto)
                }
            }

            // ── EVENT DETAILS ─────────────────────────────────────
            SectionCard(title = strings.eventDetailsSection) {
                RedOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = strings.eventTitle,
                    icon = Icons.Default.Event
                )

                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.eventCategory) },
                        leadingIcon = {
                            Icon(Icons.Default.Category, null, tint = MeTontRed)
                        },
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
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
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
                    onValueChange = { description = it },
                    label = { Text(strings.eventDescription) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
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

            // ── DATE ──────────────────────────────────────────────
            SectionCard(title = strings.eventDateSection) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = day,
                        onValueChange = { if (it.length <= 2) day = it },
                        label = { Text("Day") },
                        placeholder = { Text("DD") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = month,
                        onValueChange = { if (it.length <= 2) month = it },
                        label = { Text("Month") },
                        placeholder = { Text("MM") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = year,
                        onValueChange = { if (it.length <= 4) year = it },
                        label = { Text("Year") },
                        placeholder = { Text("YYYY") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // ── LOCATION & WEBSITE ────────────────────────────────
            SectionCard(title = strings.eventLocationSection) {
                RedOutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = strings.eventLocation,
                    icon = Icons.Default.LocationOn
                )

                RedOutlinedTextField(
                    value = websiteUrl,
                    onValueChange = { websiteUrl = it },
                    label = strings.eventWebsite,
                    icon = Icons.Default.Language,
                    keyboardType = KeyboardType.Uri
                )
            }

            // ── SUBMIT ────────────────────────────────────────────
            Button(
                onClick = {
                    val dayInt = day.toIntOrNull()
                    val monthInt = month.toIntOrNull()
                    val yearInt = year.toIntOrNull()

                    when {
                        title.isBlank() -> Toast.makeText(context, strings.eventTitleRequired, Toast.LENGTH_SHORT).show()
                        description.isBlank() -> Toast.makeText(context, strings.eventDescriptionRequired, Toast.LENGTH_SHORT).show()
                        locationName.isBlank() -> Toast.makeText(context, strings.eventLocationRequired, Toast.LENGTH_SHORT).show()
                        dayInt == null || monthInt == null || yearInt == null -> Toast.makeText(context, strings.eventInvalidDate, Toast.LENGTH_SHORT).show()
                        dayInt !in 1..31 || monthInt !in 1..12 || yearInt < 2024 -> Toast.makeText(context, strings.eventInvalidDate, Toast.LENGTH_SHORT).show()
                        else -> {
                            val calendar = Calendar.getInstance().apply {
                                set(yearInt, monthInt - 1, dayInt, 0, 0, 0)
                            }
                            val event = Event(
                                id = UUID.randomUUID().toString(),
                                title = title.trim(),
                                description = description.trim(),
                                locationName = locationName.trim(),
                                date = calendar.timeInMillis,
                                category = selectedCategory,
                                organizerId = userId,
                                websiteUrl = websiteUrl.trim().ifBlank { null }
                            )
                            isLoading = true
                            scope.launch {
                                repository.addEvent(event, selectedImageUri)
                                    .onSuccess {
                                        Toast.makeText(context, strings.eventSubmitSuccess, Toast.LENGTH_LONG).show()
                                        onEventAdded()
                                    }
                                    .onFailure { e ->
                                        Toast.makeText(context, "${strings.eventSubmitFailed}: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeTontRed,
                    contentColor = Color.White
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.submitting)
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.submitEventButton, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}