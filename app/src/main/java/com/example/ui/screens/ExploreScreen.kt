package com.example.ui.screens

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
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.AppLanguage
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
    val liveSearchResults by viewModel.liveSearchResults.collectAsState()
    val isSearchingLive by viewModel.isSearchingLive.collectAsState()
    val isDiscovering by viewModel.isDiscoveringNearby.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()
    val activeCity by viewModel.activeCity.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    val displayPlaces = if (filterState.query.length >= 3 && liveSearchResults.isNotEmpty()) {
        liveSearchResults
    } else {
        filteredPlaces
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = Localization.get("nav_explore", language),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${activeCity.nameEn}, ${activeCity.country}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoralPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshPlacesAroundCurrentLocation() },
                        modifier = Modifier.testTag("refresh_places_btn")
                    ) {
                        if (isDiscovering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = CoralPrimary
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = CoralPrimary)
                        }
                    }

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

            // Quick Filter Chips Row
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

            // Results count and reset action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${displayPlaces.size} real places discovered",
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

            // Place Cards List
            if (isSearchingLive) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CoralPrimary)
                }
            } else if (displayPlaces.isEmpty()) {
                EmptyStateView(
                    emoji = "🔍",
                    title = "No places found for this filter",
                    subtitle = "Try adjusting your filters or tap Discover to query real venues around ${activeCity.nameEn}.",
                    actionButtonText = "Discover Nearby",
                    onActionClick = { viewModel.refreshPlacesAroundCurrentLocation() }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(displayPlaces) { place ->
                        PlaceCard(
                            place = place,
                            language = language,
                            currency = currency,
                            onClick = { onNavigateToPlaceDetail(place.id) },
                            onSaveToggle = { viewModel.toggleSavePlace(place) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = Localization.get("filter_title", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Budget Level
                Text(
                    text = Localization.get("budget_title", language),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BudgetLevel.values().forEach { budget ->
                        val isSelected = filterState.maxBudget == budget
                        val budgetLabel = when (language) {
                            AppLanguage.ARABIC -> budget.labelAr
                            else -> budget.labelEn
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.updateFilter(
                                        filterState.copy(maxBudget = if (isSelected) null else budget)
                                    )
                                }
                        ) {
                            Text(
                                text = budgetLabel,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Companion Type
                Text(
                    text = Localization.get("companions_title", language),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompanionType.values().forEach { companion ->
                        val isSelected = filterState.companion == companion
                        val compLabel = when (language) {
                            AppLanguage.ARABIC -> companion.labelAr
                            AppLanguage.FRENCH -> companion.labelFr
                            AppLanguage.ENGLISH -> companion.labelEn
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.updateFilter(
                                        filterState.copy(companion = if (isSelected) null else companion)
                                    )
                                }
                        ) {
                            Text(
                                text = "${companion.emoji} $compLabel",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Apply button
                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Show Experiences", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
