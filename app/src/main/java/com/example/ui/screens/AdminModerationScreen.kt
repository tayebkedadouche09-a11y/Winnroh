package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Place
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.WaygoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminModerationScreen(
    viewModel: WaygoViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val places by viewModel.allPlaces.collectAsState()
    val reports by viewModel.allReports.collectAsState()
    val businessAccounts by viewModel.businessAccounts.collectAsState()
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business & Admin Portal 🛡️", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.testTag("admin_moderation_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Status Banner
            if (statusMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessGreen.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage!!,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Reports Queue Section
            item {
                Text(
                    text = "User Reports Queue (${reports.size}) ⚠️",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (reports.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✨ All user reports are cleared and moderated.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(reports) { rep ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Flag, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Target: ${rep.targetType} (${rep.targetId})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (rep.status == "PENDING") AmberAccent.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = rep.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Reason: ${rep.reason}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = {
                                    viewModel.resolveReport(rep.id, "DISMISSED")
                                    statusMessage = "Dismissed report ${rep.id}"
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dismiss", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        viewModel.resolveReport(rep.id, "RESOLVED")
                                        statusMessage = "Resolved report ${rep.id}"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Resolve & Take Action", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Business Claims Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Business Claims & Sponsorships (${businessAccounts.size}) 🏢",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (businessAccounts.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No pending business claims.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(businessAccounts) { biz ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = biz.businessName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Email: ${biz.ownerEmail} • Phone: ${biz.contactPhone ?: "N/A"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (biz.promotionalOffer != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "🎁 Offer: ${biz.promotionalOffer}", fontSize = 12.sp, color = CoralPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Gamification Engine Controls
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "🧪 Gamification & XP Engine", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Test real-time XP and Level Up progression engine events:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val current = viewModel.allPlaces.value.firstOrNull()
                                    if (current != null) {
                                        viewModel.checkInPlace(current)
                                        statusMessage = "Added +50 XP and verified check-in!"
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+50 XP Check-in", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.rollSurpriseMe()
                                    statusMessage = "Dispatched Surprise Me roulette engine!"
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Roll Roulette", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Places Verification & Management
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "All Verified Places (${places.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(places) { place ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "${place.category.emoji} ${place.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Outlined.Verified, contentDescription = "Verified", tint = CoralPrimary, modifier = Modifier.size(16.dp))
                            }
                            Text(text = "${place.address} • Rating: ${place.rating}★", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Active",
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
