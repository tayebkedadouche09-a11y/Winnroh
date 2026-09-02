package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryType
import com.example.data.model.Place
import com.example.ui.components.CategoryPill
import com.example.ui.components.PlaceCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaygoViewModel

@Composable
fun MapViewScreen(
    viewModel: WaygoViewModel,
    onNavigateToPlaceDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val places by viewModel.allPlaces.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()

    var activeSelectedPlace by remember { mutableStateOf<Place?>(null) }

    LaunchedEffect(places) {
        if (activeSelectedPlace == null && places.isNotEmpty()) {
            activeSelectedPlace = places.first()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF131A26))
            .testTag("map_view_screen")
    ) {
        // Map Canvas Simulator
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (places.isNotEmpty()) {
                            activeSelectedPlace = places.random()
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // Draw background terrain and roads
            drawRect(Color(0xFF1E293B))

            // Main avenues / grid lines
            drawLine(
                color = Color(0xFF334155),
                start = Offset(0f, height * 0.4f),
                end = Offset(width, height * 0.4f),
                strokeWidth = 14f
            )
            drawLine(
                color = Color(0xFF334155),
                start = Offset(width * 0.35f, 0f),
                end = Offset(width * 0.35f, height),
                strokeWidth = 12f
            )
            drawLine(
                color = Color(0xFF475569),
                start = Offset(0f, height * 0.7f),
                end = Offset(width, height * 0.2f),
                strokeWidth = 8f
            )

            // Radius Circle
            drawCircle(
                color = CoralPrimary.copy(alpha = 0.08f),
                radius = 280f,
                center = Offset(width * 0.5f, height * 0.45f)
            )
            drawCircle(
                color = CoralPrimary.copy(alpha = 0.3f),
                radius = 280f,
                center = Offset(width * 0.5f, height * 0.45f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )

            // Current User Location Pulse
            drawCircle(
                color = InfoBlue.copy(alpha = 0.25f),
                radius = 32f,
                center = Offset(width * 0.5f, height * 0.45f)
            )
            drawCircle(
                color = Color.White,
                radius = 12f,
                center = Offset(width * 0.5f, height * 0.45f)
            )
            drawCircle(
                color = InfoBlue,
                radius = 8f,
                center = Offset(width * 0.5f, height * 0.45f)
            )
        }

        // Custom Overlay Map Pins
        Box(modifier = Modifier.fillMaxSize()) {
            places.take(6).forEachIndexed { index, place ->
                val isSelected = activeSelectedPlace?.id == place.id
                val xPos = when (index) {
                    0 -> 0.3f
                    1 -> 0.7f
                    2 -> 0.2f
                    3 -> 0.8f
                    4 -> 0.55f
                    else -> 0.4f
                }
                val yPos = when (index) {
                    0 -> 0.32f
                    1 -> 0.25f
                    2 -> 0.58f
                    3 -> 0.62f
                    4 -> 0.22f
                    else -> 0.72f
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 180.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val pinX = maxWidth * xPos
                        val pinY = maxHeight * yPos

                        Box(
                            modifier = Modifier
                                .offset(x = pinX, y = pinY)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surface)
                                .clickable { activeSelectedPlace = place }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = place.category.emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = place.priceLevel.symbol,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Filter Floating Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CategoryType.values().take(6)) { cat ->
                    CategoryPill(
                        category = cat,
                        isSelected = filterState.category == cat,
                        language = language,
                        onClick = { viewModel.selectCategory(cat) }
                    )
                }
            }
        }

        // Floating GPS Center Button
        FloatingActionButton(
            onClick = { /* Re-center to user */ },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CoralPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 70.dp, end = 16.dp)
                .size(44.dp)
        ) {
            Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location", modifier = Modifier.size(20.dp))
        }

        // Bottom Selected Place Card Card
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 90.dp, start = 16.dp, end = 16.dp)
        ) {
            val place = activeSelectedPlace
            if (place != null) {
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
