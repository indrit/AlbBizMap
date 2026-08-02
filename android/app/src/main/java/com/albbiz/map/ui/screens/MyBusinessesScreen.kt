// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.data.Business
import com.albbiz.map.data.BusinessRepository
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.google.firebase.auth.FirebaseAuth

// The businesses a logged-in user owns — reachable from the "My Businesses" card
// on the Profile screen. Reuses BusinessListItem (same row the Directory and
// Favorites screens use) so tapping a business goes to its detail page, where the
// existing owner-only "Edit" button already lives — no separate edit entry point
// needed here.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBusinessesScreen(
    onBackClick: () -> Unit,
    onBusinessClick: (String) -> Unit,
    onAddBusinessClick: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val strings = LocalAppStrings.current
    val userLocation by viewModel.userLocation.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    val user = FirebaseAuth.getInstance().currentUser
    val businessRepository = remember { BusinessRepository() }
    var ownedBusinesses by remember { mutableStateOf<List<Business>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Same getBusinessesByOwner query UserProfileScreen already uses (there, just
    // to pick a tier badge for the avatar) — this is a live Firestore listener via
    // BusinessRepository, so edits/adds elsewhere reflect here without a refresh.
    LaunchedEffect(user?.uid) {
        val uid = user?.uid
        if (uid == null) {
            isLoading = false
        } else {
            businessRepository.getBusinessesByOwner(uid).collect { list ->
                ownedBusinesses = list
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.myBusinesses,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MeTontRed)
                    }
                }
                ownedBusinesses.isEmpty() -> {
                    // ── EMPTY STATE ───────────────────────────────
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = RoundedCornerShape(40.dp),
                                color = MeTontRed.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Storefront,
                                        null,
                                        modifier = Modifier.size(40.dp),
                                        tint = MeTontRed.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Text(
                                strings.noBusinessesYet,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MeTontGrey,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                strings.noBusinessesYetSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MeTontGrey.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onAddBusinessClick,
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MeTontRed,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.addBusiness, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                else -> {
                    // ── OWNED BUSINESSES LIST ─────────────────────
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            Text(
                                "${ownedBusinesses.size} ${if (ownedBusinesses.size == 1) "business" else "businesses"}",
                                color = MeTontGrey,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(ownedBusinesses) { business ->
                            BusinessListItem(
                                business = business,
                                userLocation = userLocation,
                                isFavorite = favoriteIds.contains(business.id),
                                onToggleFavorite = { viewModel.toggleFavorite(business.id) },
                                onToggleLike = { viewModel.toggleFavorite(business.id) },
                                onClick = { onBusinessClick(business.id) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onAddBusinessClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MeTontRed),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MeTontRed)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.addBusiness)
                            }
                        }
                    }
                }
            }
        }
    }
}
