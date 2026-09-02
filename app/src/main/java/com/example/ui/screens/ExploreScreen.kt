package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetLevel
import com.example.data.model.CategoryType
import com.example.data.model.CompanionType
import com.example.ui.components.CategoryPill
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PlaceCard
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.Localization
import com.example.ui.viewmodel.WaygoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: WaygoViewModel,
    onNavigateToPlaceDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredPlaces by viewModel.filteredPlaces.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Localization.get("nav_explore", language),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .testTag("filter_sheet_btn")
                            .background(
                                if (filterState.maxBudget != null || filterState.companion != null || filterState.openNowOnly) CoralPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterAlt,
                            contentDescription = "Filters",
                            tint = if (filterState.maxBudget != null || filterState.companion != null || filterState.openNowOnly) CoralPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.testTag("explore_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Text Field
            OutlinedTextField(
                value = filterState.query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = {
                    Text(
                        Localization.get("search_hint", language),
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CoralPrimary
                    )
                },
                trailingIcon = {
                    if (filterState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("explore_search_input")
            )

            // Categories horizontal bar
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CategoryType.values()) { cat ->
                    CategoryPill(
                        category = cat,
                        isSelected = filterState.category == cat,
                        language = language,
                        onClick = { viewModel.selectCategory(cat) }
                    )
                }
            }

            // Quick Filter Chips Row (Open Now, Solo, Friends, Indoor)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = filterState.openNowOnly,
                    onClick = {
                        viewModel.updateFilter(filterState.copy(openNowOnly = !filterState.openNowOnly))
                    },
                    label = { Text(Localization.get("filter_open_now", language), fontSize = 11.sp) },
                    leadingIcon = {
                        if (filterState.openNowOnly) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                )

                FilterChip(
                    selected = filterState.indoorOnly,
                    onClick = {
                        viewModel.updateFilter(filterState.copy(indoorOnly = !filterState.indoorOnly, outdoorOnly = false))
                    },
                    label = { Text("🏠 " + Localization.get("filter_indoor", language), fontSize = 11.sp) }
                )

                FilterChip(
                    selected = filterState.outdoorOnly,
                    onClick = {
                        viewModel.updateFilter(filterState.copy(outdoorOnly = !filterState.outdoorOnly, indoorOnly = false))
                    },
                    label = { Text("🌳 " + Localization.get("filter_outdoor", language), fontSize = 11.sp) }
                )
            }

            // Results count and clear button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredPlaces.size} experiences found",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (filterState.query.isNotBlank() || filterState.category != CategoryType.ALL || filterState.openNowOnly || filterState.indoorOnly || filterState.outdoorOnly) {
                    Text(
                        text = "Reset all",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralPrimary,
                        modifier = Modifier.clickable { viewModel.resetFilters() }
                    )
                }
            }

            // Places List or Empty State
            if (filteredPlaces.isEmpty()) {
                EmptyStateView(
                    emoji = "🔍",
                    title = "No experiences match your criteria",
                    subtitle = "Try broadening your budget, clearing category filters, or searching for coffee, gaming, or parks.",
                    actionButtonText = "Reset Filters",
                    onActionClick = { viewModel.resetFilters() }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredPlaces) { place ->
                        PlaceCard(
                            place = place,
                            language = language,
                            onClick = { onNavigateToPlaceDetail(place.id) },
                            onSaveToggle = { viewModel.toggleSavePlace(place) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Advanced Bottom Sheet Filter
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Smart Experience Filters ⚙️",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Budget Selector
                Text(
                    text = Localization.get("budget", language),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BudgetLevel.values().forEach { b ->
                        val isSel = filterState.maxBudget == b
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.updateFilter(filterState.copy(maxBudget = if (isSel) null else b))
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = b.symbol,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Companion Selector
                Text(
                    text = Localization.get("companions", language),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompanionType.values().forEach { comp ->
                        val isSel = filterState.companion == comp
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.updateFilter(filterState.copy(companion = if (isSel) null else comp))
                                }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = comp.emoji, fontSize = 14.sp)
                                Text(
                                    text = comp.labelEn.split(" ").first(),
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("APPLY FILTERS (${filteredPlaces.size})", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
