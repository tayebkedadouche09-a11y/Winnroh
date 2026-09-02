package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CategoryType
import com.example.data.model.Place
import com.example.ui.components.CategoryPill
import com.example.ui.components.PlaceCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaygoViewModel
import kotlin.math.*

@Composable
fun MapViewScreen(
    viewModel: WaygoViewModel,
    onNavigateToPlaceDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val places by viewModel.allPlaces.collectAsState()
    val activeCity by viewModel.activeCity.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()

    var activeSelectedPlace by remember { mutableStateOf<Place?>(null) }
    var zoomLevel by remember { mutableStateOf(14) }
    var panOffsetX by remember { mutableStateOf(0f) }
    var panOffsetY by remember { mutableStateOf(0f) }

    LaunchedEffect(places) {
        if (activeSelectedPlace == null && places.isNotEmpty()) {
            activeSelectedPlace = places.first()
        }
    }

    // Reset pan when city changes
    LaunchedEffect(activeCity) {
        panOffsetX = 0f
        panOffsetY = 0f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("map_view_screen")
    ) {
        // Slippy Tile Map Viewport
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                        if (zoom > 1.15f && zoomLevel < 18) {
                            zoomLevel += 1
                        } else if (zoom < 0.85f && zoomLevel > 11) {
                            zoomLevel -= 1
                        }
                    }
                }
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val density = LocalDensity.current

            val centerLat = activeCity.latitude
            val centerLon = activeCity.longitude

            // Web Mercator tile calculations
            val n = 1 shl zoomLevel
            val centerTileXDouble = (centerLon + 180.0) / 360.0 * n
            val latRad = Math.toRadians(centerLat)
            val centerTileYDouble = (1.0 - asinh(tan(latRad)) / Math.PI) / 2.0 * n

            val centerTileX = centerTileXDouble.toInt()
            val centerTileY = centerTileYDouble.toInt()

            val tileSizePx = with(density) { 256.dp.toPx() }

            // Render 3x3 surrounding tiles for smooth coverage
            for (dx in -2..2) {
                for (dy in -2..2) {
                    val tileX = centerTileX + dx
                    val tileY = centerTileY + dy

                    if (tileX in 0 until n && tileY in 0 until n) {
                        val tileOffsetX = (tileX - centerTileXDouble) * tileSizePx + (widthPx / 2f) + panOffsetX
                        val tileOffsetY = (tileY - centerTileYDouble) * tileSizePx + (heightPx / 2f) + panOffsetY

                        val tileUrl = "https://cartodb-basemaps-a.global.ssl.fastly.net/rastertiles/voyager/$zoomLevel/$tileX/$tileY.png"

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(tileOffsetX.toInt(), tileOffsetY.toInt()) }
                                .size(256.dp)
                        ) {
                            AsyncImage(
                                model = tileUrl,
                                contentDescription = "Map Tile",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Real Place Coordinate Markers
            places.forEach { place ->
                val isSelected = activeSelectedPlace?.id == place.id

                val placeTileXDouble = (place.longitude + 180.0) / 360.0 * n
                val pLatRad = Math.toRadians(place.latitude)
                val placeTileYDouble = (1.0 - asinh(tan(pLatRad)) / Math.PI) / 2.0 * n

                val markerX = (placeTileXDouble - centerTileXDouble) * tileSizePx + (widthPx / 2f) + panOffsetX
                val markerY = (placeTileYDouble - centerTileYDouble) * tileSizePx + (heightPx / 2f) + panOffsetY

                if (markerX in -50f..(widthPx + 50f) && markerY in -50f..(heightPx + 50f)) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(markerX.toInt() - 40, markerY.toInt() - 40) }
                            .shadow(elevation = if (isSelected) 10.dp else 4.dp, shape = RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surface)
                            .clickable { activeSelectedPlace = place }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = place.category.emoji, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (place.name.length > 12) place.name.take(10) + "…" else place.name,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // User GPS Location Pin (at active center)
            val gpsMarkerX = (widthPx / 2f) + panOffsetX
            val gpsMarkerY = (heightPx / 2f) + panOffsetY
            Box(
                modifier = Modifier
                    .offset { IntOffset(gpsMarkerX.toInt() - 14, gpsMarkerY.toInt() - 14) }
                    .size(28.dp)
                    .background(InfoBlue.copy(alpha = 0.25f), CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.White, CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(InfoBlue, CircleShape)
                    )
                }
            }
        }

        // Top Category Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CategoryType.values().take(8)) { cat ->
                    CategoryPill(
                        category = cat,
                        isSelected = filterState.category == cat,
                        language = language,
                        onClick = { viewModel.selectCategory(cat) }
                    )
                }
            }
        }

        // Map Control Floating Buttons (Zoom In, Zoom Out, Center GPS)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 74.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { if (zoomLevel < 18) zoomLevel += 1 },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In")
            }

            SmallFloatingActionButton(
                onClick = { if (zoomLevel > 11) zoomLevel -= 1 },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out")
            }

            FloatingActionButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                    viewModel.refreshPlacesAroundCurrentLocation()
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CoralPrimary,
                shape = CircleShape,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location")
            }
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
