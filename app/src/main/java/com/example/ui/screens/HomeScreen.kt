package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.CategoryType
import com.example.data.service.CityLocation
import com.example.ui.components.CategoryPill
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PlaceCard
import com.example.ui.components.XPProgressBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaygoViewModel

@Composable
fun HomeScreen(
    viewModel: WaygoViewModel,
    onNavigateToExplore: () -> Unit,
    onNavigateToSurprise: () -> Unit,
    onNavigateToPlaceDetail: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val places by viewModel.allPlaces.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()
    val activeCity by viewModel.activeCity.collectAsState()
    val weather by viewModel.weatherState.collectAsState()
    val isDiscovering by viewModel.isDiscoveringNearby.collectAsState()
    val checkInMsg by viewModel.checkInMessage.collectAsState()

    var showCityDialog by remember { mutableStateOf(false) }
    var citySearchText by remember { mutableStateOf("") }

    val recommendedPlaces = places.take(6)
    val trendingPlaces = places.filter { it.isTrending }.ifEmpty { places.drop(6).take(6) }

    val cityName = when (language) {
        AppLanguage.ARABIC -> activeCity.nameAr
        AppLanguage.FRENCH -> activeCity.nameFr
        AppLanguage.ENGLISH -> activeCity.nameEn
    }

    val weatherHint = when (language) {
        AppLanguage.ARABIC -> weather?.recommendationHintAr ?: Localization.get("weather_sunny", language)
        AppLanguage.FRENCH -> weather?.recommendationHintFr ?: Localization.get("weather_sunny", language)
        AppLanguage.ENGLISH -> weather?.recommendationHint ?: Localization.get("weather_sunny", language)
    }

    LaunchedEffect(checkInMsg) {
        if (checkInMsg != null) {
            kotlinx.coroutines.delay(3500)
            viewModel.clearCheckInMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("home_screen"),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Hero Header & Dynamic Greeting
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            // Location & City Selector Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CoralPrimary.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showCityDialog = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$cityName, ${activeCity.country}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CoralPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "▼",
                                        fontSize = 9.sp,
                                        color = CoralPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "وين نروح؟ WINNROH 👋",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = Localization.get("app_tagline", language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Notification Bell Button
                        IconButton(
                            onClick = onNavigateToNotifications,
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .testTag("notification_bell_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real-Time Weather Recommendation Banner
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AmberAccent.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = weather?.iconEmoji ?: "🌤️",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${weather?.temperatureC?.toInt() ?: 23}°C",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = weather?.conditionTitle ?: "Pleasant Weather",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = CoralPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = weatherHint,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // XP Progress Strip
                    XPProgressBar(userProfile = userProfile)
                }
            }

            // Search Bar Trigger
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp)
                        .testTag("home_search_bar")
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onNavigateToExplore)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = CoralPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = Localization.get("search_hint", language),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Horizontal Categories
            item {
                Spacer(modifier = Modifier.height(18.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CategoryType.values()) { cat ->
                        CategoryPill(
                            category = cat,
                            isSelected = filterState.category == cat,
                            language = language,
                            onClick = {
                                viewModel.selectCategory(cat)
                                if (cat != CategoryType.ALL) {
                                    onNavigateToExplore()
                                }
                            }
                        )
                    }
                }
            }

            // Signature "Surprise Me" Hero Banner
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .testTag("surprise_me_banner_card")
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(onClick = onNavigateToSurprise)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_surprise_me),
                            contentDescription = "Surprise Me",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CoralPrimary
                            ) {
                                Text(
                                    text = "🎲 " + Localization.get("surprise_me_title", language),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "وين نروح؟ WINNROH",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )

                            Text(
                                text = Localization.get("surprise_me_sub", language),
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                maxLines = 2,
                                modifier = Modifier.width(220.dp)
                            )
                        }
                    }
                }
            }

            // Real Discovery Header with Refresh Action
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Localization.get("recommended_for_you", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isDiscovering) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = CoralPrimary
                            )
                        }
                    }

                    Text(
                        text = "See all",
                        color = CoralPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onNavigateToExplore)
                    )
                }
            }

            // Places List / Empty State
            if (places.isEmpty()) {
                item {
                    EmptyStateView(
                        emoji = "🗺️",
                        title = "Discovering real places around $cityName",
                        subtitle = "Fetching live venues, restaurants, cafes, and sights from OpenStreetMap.",
                        actionButtonText = "Refresh Discovery",
                        onActionClick = { viewModel.refreshPlacesAroundCurrentLocation() }
                    )
                }
            } else {
                items(recommendedPlaces) { place ->
                    PlaceCard(
                        place = place,
                        language = language,
                        currency = currency,
                        onClick = { onNavigateToPlaceDetail(place.id) },
                        onSaveToggle = { viewModel.toggleSavePlace(place) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Floating Check-In Proximity Feedback Snackbar
        if (checkInMsg != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 105.dp, start = 20.dp, end = 20.dp)
            ) {
                Text(
                    text = checkInMsg ?: "",
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }

    // Global City & Live Geocode Selector Modal
    if (showCityDialog) {
        Dialog(onDismissRequest = { showCityDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = Localization.get("city_selector", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Geocode City / Address Input
                    OutlinedTextField(
                        value = citySearchText,
                        onValueChange = { citySearchText = it },
                        placeholder = { Text("Search any global city or address…", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            if (citySearchText.isNotBlank()) {
                                IconButton(onClick = {
                                    viewModel.searchAndSelectCity(citySearchText)
                                    showCityDialog = false
                                }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = CoralPrimary)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // GPS Quick Select Option
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = InfoBlue.copy(alpha = 0.12f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.useDeviceGpsLocation()
                                showCityDialog = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.MyLocation, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Use My Current GPS Location",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = InfoBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Default City Hubs
                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(viewModel.defaultGlobalCities) { city ->
                            val isSelected = city.nameEn == activeCity.nameEn
                            val cName = when (language) {
                                AppLanguage.ARABIC -> city.nameAr
                                AppLanguage.FRENCH -> city.nameFr
                                AppLanguage.ENGLISH -> city.nameEn
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) CoralPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.setCity(city)
                                        showCityDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = cName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = city.country,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = CoralPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
