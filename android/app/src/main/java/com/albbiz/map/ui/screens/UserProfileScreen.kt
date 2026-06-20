// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.ui.AppLanguage
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.albbiz.map.viewmodel.AdminViewModel
import com.albbiz.map.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.albbiz.map.R
import com.albbiz.map.data.BusinessRepository


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
    val strings = LocalAppStrings.current

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val adminViewModel: AdminViewModel = viewModel()
    val isAdmin by adminViewModel.isAdmin.collectAsState()
    val businessRepository = remember { BusinessRepository() }
    var userTierIcon by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            businessRepository.getBusinessesByOwner(uid).collect { ownedBusinesses ->
                userTierIcon = when {
                    ownedBusinesses.any { it.isSponsored } -> R.drawable.metont_gold
                    ownedBusinesses.any { it.isFeatured } -> R.drawable.metont_silver
                    ownedBusinesses.any { it.isPremium } -> R.drawable.metont_bronze
                    else -> null
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        user?.displayName?.let { name ->
            val parts = name.split(" ")
            if (parts.isNotEmpty()) firstName = parts[0]
            if (parts.size > 1) lastName = parts.drop(1).joinToString(" ")
        }
    }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { adminViewModel.checkAdminStatus(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.myProfile,
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
               // .background(Color(0xFFFFF8F0))
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── RED HEADER ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MeTontRed)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Avatar circle
                    if (userTierIcon != null) {
                        Image(
                            painter = painterResource(id = userTierIcon!!),
                            contentDescription = "Tier badge",
                            modifier = Modifier.size(80.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            null,
                            modifier = Modifier.size(60.dp),
                            tint = Color.White
                        )
                    }



                    Text(
                        text = if (firstName.isNotEmpty()) "$firstName $lastName" else "User",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user?.email ?: "",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── PERSONAL INFO CARD ────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        strings.personalInformation,
                        fontWeight = FontWeight.Bold,
                        color = MeTontRed,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(strings.firstName) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = MeTontRed)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            focusedLabelColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(strings.lastName) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = MeTontRed)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            focusedLabelColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (firstName.isBlank()) {
                                Toast.makeText(
                                    context,
                                    strings.firstNameRequired,
                                    Toast.LENGTH_SHORT
                                ).show()
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
                                        Toast.makeText(
                                            context,
                                            strings.profileSaved,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            strings.profileSaveFailed,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MeTontRed,
                            contentColor = Color.White
                        ),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(strings.saveProfile, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── UPGRADE CARD ──────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        null,
                        tint = Color(0xFFFFAA00),
                        modifier = Modifier.size(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            strings.upgradeToPremium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8F00),
                            fontSize = 14.sp
                        )
                        Text(
                            "Unlock all features",
                            color = MeTontGrey,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = onUpgradeClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFAA00)
                        )
                    ) {
                        Text("Upgrade", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            // ── ADMIN PANEL CARD ──────────────────────────────────
            if (isAdmin) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            null,
                            tint = MeTontRed,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Admin Panel",
                                fontWeight = FontWeight.Bold,
                                color = MeTontRed,
                                fontSize = 14.sp
                            )
                            Text(
                                "Manage claims and data",
                                color = MeTontGrey,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = onAdminClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MeTontRed
                            )
                        ) {
                            Text("Open", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── LANGUAGE CARD ─────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Language / Gjuha",
                        fontWeight = FontWeight.Bold,
                        color = MeTontRed,
                        fontSize = 14.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onLanguageChange(AppLanguage.EN) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (currentLanguage == AppLanguage.EN)
                                    MeTontRed else Color.Transparent,
                                contentColor = if (currentLanguage == AppLanguage.EN)
                                    Color.White else MeTontRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, MeTontRed
                            )
                        ) {
                            Text("🇬🇧 English")
                        }
                        OutlinedButton(
                            onClick = { onLanguageChange(AppLanguage.SQ) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (currentLanguage == AppLanguage.SQ)
                                    MeTontRed else Color.Transparent,
                                contentColor = if (currentLanguage == AppLanguage.SQ)
                                    Color.White else MeTontRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, MeTontRed
                            )
                        ) {
                            Text("🇦🇱 Shqip")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── LOGOUT BUTTON ─────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MeTontRed
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeTontRed)
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.logout, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}