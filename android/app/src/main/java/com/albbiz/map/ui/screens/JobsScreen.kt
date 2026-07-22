// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.data.Business
import com.albbiz.map.data.JobPosting
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed

// Same accent used for job postings on a business's own detail page
// (BusinessDetailsScreen's DetailJobItem) — kept consistent here since this
// screen is just every business's postings gathered into one place.
private val JobAccent = Color(0xFF673AB7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsScreen(
    onBackClick: () -> Unit,
    onBusinessClick: (String) -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val businesses by viewModel.businesses.collectAsState()
    val strings = LocalAppStrings.current

    // Every business's job postings, flattened into one list, newest first —
    // this screen is the app-wide job board; a single business's own postings
    // still show on its own detail page too (DetailJobItem), this just
    // aggregates all of them in one browsable place.
    val allJobs = remember(businesses) {
        businesses
            .flatMap { business -> business.jobs.map { job -> business to job } }
            .sortedByDescending { (_, job) -> job.postedAt }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.jobs,
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
            if (allJobs.isEmpty()) {
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
                            color = JobAccent.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Work,
                                    null,
                                    modifier = Modifier.size(40.dp),
                                    tint = JobAccent.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Text(
                            "No job postings yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MeTontGrey,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Businesses that are hiring will show up here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MeTontGrey.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "${allJobs.size} open ${if (allJobs.size == 1) "position" else "positions"}",
                            color = MeTontGrey,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(allJobs) { (business, job) ->
                        JobListingCard(
                            business = business,
                            job = job,
                            onViewProfileClick = { onBusinessClick(business.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JobListingCard(
    business: Business,
    job: JobPosting,
    onViewProfileClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── BUSINESS NAME / CATEGORY ──────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MeTontRed
                ) {}
                Text(
                    business.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MeTontRed
                )
                if (business.category.isNotBlank()) {
                    Text(
                        "• ${business.category}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MeTontGrey
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // ── JOB TITLE ──────────────────────────────────────────
            Text(
                job.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // ── TYPE / SALARY ──────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = JobAccent.copy(alpha = 0.12f)
                ) {
                    Text(
                        job.type,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = JobAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (job.salary != null) {
                    Text(
                        "💰 ${job.salary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MeTontGrey,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── DESCRIPTION ────────────────────────────────────────
            Text(
                job.description,
                style = MaterialTheme.typography.bodySmall,
                color = MeTontGrey,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── VIEW PROFILE BUTTON ────────────────────────────────
            Button(
                onClick = onViewProfileClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeTontRed,
                    contentColor = Color.White
                )
            ) {
                Text("View Profile", fontWeight = FontWeight.Bold)
            }
        }
    }
}
