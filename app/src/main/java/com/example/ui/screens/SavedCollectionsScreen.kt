package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserCollection
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PlaceCard
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.Localization
import com.example.ui.viewmodel.WaygoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCollectionsScreen(
    viewModel: WaygoViewModel,
    onNavigateToPlaceDetail: (String) -> Unit,
    onNavigateToExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savedPlaces by viewModel.savedPlaces.collectAsState()
    val collections by viewModel.collections.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showCreateCollectionDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Localization.get("nav_saved", language),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (selectedTab == 1) {
                        IconButton(
                            onClick = { showCreateCollectionDialog = true },
                            modifier = Modifier.testTag("create_collection_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Create Collection", tint = CoralPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.testTag("saved_collections_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = CoralPrimary,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "${Localization.get("save", language)} (${savedPlaces.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "${Localization.get("collections", language)} (${collections.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Saved Places
                    if (savedPlaces.isEmpty()) {
                        EmptyStateView(
                            emoji = "❤️",
                            title = Localization.get("empty_saved_title", language),
                            subtitle = Localization.get("empty_saved_sub", language),
                            actionButtonText = Localization.get("start_exploring", language),
                            onActionClick = onNavigateToExplore
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(savedPlaces) { place ->
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
                1 -> {
                    // User Collections Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(collections) { col ->
                            CollectionCard(collection = col)
                        }
                    }
                }
            }
        }
    }

    if (showCreateCollectionDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateCollectionDialog = false },
            onCreate = { title, desc, emoji ->
                viewModel.createCollection(title, desc, emoji)
                showCreateCollectionDialog = false
            }
        )
    }
}

@Composable
fun CollectionCard(
    collection: UserCollection,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.height(160.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(CoralPrimary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = collection.coverEmoji, fontSize = 22.sp)
            }

            Column {
                Text(
                    text = collection.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = collection.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🔥") }

    val emojiOptions = listOf("🔥", "☕", "🎮", "🍔", "✨", "❤️", "🏖️", "🌲")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "New Collection 📁",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Emoji Picker Row
                Text(text = "Choose Icon", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    emojiOptions.forEach { emoji ->
                        val isSel = emoji == selectedEmoji
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSel) CoralPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Collection Name (e.g. Late Night Cafes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Short Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (title.isNotBlank()) onCreate(title, desc, selectedEmoji) },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
