// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import androidx.compose.ui.graphics.Color
import com.albbiz.map.ui.AppLanguage
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.viewmodel.AdminViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onUpgradeClick: () -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onAdminClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val strings = LocalAppStrings.current


    // Load existing display name if available
    LaunchedEffect(Unit) {
        user?.displayName?.let { name ->
            val parts = name.split(" ")
            if (parts.isNotEmpty()) firstName = parts[0]
            if (parts.size > 1) lastName = parts.drop(1).joinToString(" ")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LocalAppStrings.current.myProfile) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile icon
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = LocalAppStrings.current.personalInformation,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text(LocalAppStrings.current.firstName) },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text(LocalAppStrings.current.lastName) },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (firstName.isBlank()) {
                        Toast.makeText(context, strings.firstNameRequired, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    val displayName = "$firstName $lastName".trim()
                    val profileUpdates = userProfileChangeRequest {
                        this.displayName = displayName
                    }
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { task ->
                            isSaving = false
                            if (task.isSuccessful) {
                                Toast.makeText(context, strings.profileSaved, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    strings.profileSaveFailed,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(LocalAppStrings.current.saveProfile)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            // Subscription upgrade button
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFAA00)
                )
            ) {
                Icon(Icons.Default.Star, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text( LocalAppStrings.current.upgradeToPremium, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Show admin panel button only for admins
            val adminViewModel: AdminViewModel = viewModel()
            val isAdmin by adminViewModel.isAdmin.collectAsState()

            LaunchedEffect(user?.uid) {
                user?.uid?.let { adminViewModel.checkAdminStatus(it) }
            }

            if (isAdmin) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Button(
                    onClick = onAdminClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.AdminPanelSettings, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin Panel", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Language / Gjuha",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onLanguageChange(AppLanguage.EN) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (currentLanguage == AppLanguage.EN)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text("🇬🇧 English")
                }
                OutlinedButton(
                    onClick = { onLanguageChange(AppLanguage.SQ) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (currentLanguage == AppLanguage.SQ)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text("🇦🇱 Shqip")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Logout button
            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(LocalAppStrings.current.logout)
            }
        }
    }
}