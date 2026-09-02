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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.WbSunny
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

    var showCityDialog by remember { mutableStateOf(false) }

    val recommendedPlaces = places.filter { it.rating >= 4.8 }
    val trendingPlaces = places.filter { it.isTrending }

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

    LazyColumn(
        modifier = modifier
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
                            text = "Good day, ${userProfile?.displayName ?: "Explorer"} 👋",
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

        // Large "Surprise Me" Hero Banner
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
                                text = "🎲 SIGNATURE FEATURE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = Localization.get("surprise_me_title", language),
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

        // Recommended For You Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.get("recommended_for_you", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "See all",
                    color = CoralPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onNavigateToExplore)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(recommendedPlaces) { place ->
                    PlaceCard(
                        place = place,
                        language = language,
                        currency = currency,
                        onClick = { onNavigateToPlaceDetail(place.id) },
                        onSaveToggle = { viewModel.toggleSavePlace(place) },
                        modifier = Modifier.width(260.dp),
                        isLarge = true
                    )
                }
            }
        }

        // Trending Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = Localization.get("trending", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(trendingPlaces) { place ->
                    PlaceCard(
                        place = place,
                        language = language,
                        currency = currency,
                        onClick = { onNavigateToPlaceDetail(place.id) },
                        onSaveToggle = { viewModel.toggleSavePlace(place) },
                        modifier = Modifier.width(240.dp)
                    )
                }
            }
        }

        // Near You Vertical Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = Localization.get("near_you", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(places.take(4)) { place ->
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

    // City Location Selector Modal
    if (showCityDialog) {
        Dialog(onDismissRequest = { showCityDialog = false }) {
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
                        text = Localization.get("city_selector", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val cities = listOf(
                        CityLocation("Algiers", "الجزائر العاصمة", "Alger", "Algeria", 36.7538, 3.0588),
                        CityLocation("Oran", "وهران", "Oran", "Algeria", 35.6987, -0.6349),
                        CityLocation("Constantine", "قسنطينة", "Constantine", "Algeria", 36.3650, 6.6147),
                        CityLocation("Paris", "باريس", "Paris", "France", 48.8566, 2.3522),
                        CityLocation("Marseille", "مارسيليا", "Marseille", "France", 43.2965, 5.3698),
                        CityLocation("Montreal", "مونتريال", "Montréal", "Canada", 45.5017, -73.5673),
                        CityLocation("New York", "نيويورك", "New York", "USA", 40.7128, -74.0060),
                        CityLocation("London", "لندن", "Londres", "UK", 51.5074, -0.1278),
                        CityLocation("Dubai", "دبي", "Dubaï", "UAE", 25.2048, 55.2708),
                        CityLocation("Tokyo", "طوكيو", "Tokyo", "Japan", 35.6762, 139.6503)
                    )

                    cities.forEach { city ->
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
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setCity(city)
                                    showCityDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
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
