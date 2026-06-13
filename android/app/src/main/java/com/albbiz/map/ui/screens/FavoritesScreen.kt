// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onBusinessClick: (String) -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val businesses by viewModel.businesses.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val strings = LocalAppStrings.current

    val favoriteBusinesses = businesses.filter { it.id in favoriteIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.myFavorites,
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
                //.background(Color(0xFFFFF8F0))
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            if (favoriteBusinesses.isEmpty()) {
                // ── EMPTY STATE ───────────────────────────────────
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = RoundedCornerShape(40.dp),
                            color = MeTontRed.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Favorite,
                                    null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MeTontRed.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Text(
                            strings.noFavoritesYet,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MeTontGrey,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Tap the heart icon on any business to save it here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MeTontGrey.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // ── FAVORITES COUNT ───────────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "${favoriteBusinesses.size} saved ${if (favoriteBusinesses.size == 1) "business" else "businesses"}",
                            color = MeTontGrey,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(favoriteBusinesses) { business ->
                        BusinessListItemWrapper(
                            business = business,
                            userLocation = userLocation,
                            onClick = { onBusinessClick(business.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessListItemWrapper(
    business: com.albbiz.map.data.Business,
    userLocation: com.google.android.gms.maps.model.LatLng?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        BusinessListItem(
            business = business,
            userLocation = userLocation,
            isFavorite = true,
            onToggleFavorite = {},
            onClick = onClick
        )
    }
}