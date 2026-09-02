package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.data.local.Converters

@Entity(tableName = "places")
@TypeConverters(Converters::class)
data class Place(
    @PrimaryKey val id: String,
    val name: String,
    val arabicName: String,
    val category: CategoryType,
    val description: String,
    val arabicDescription: String,
    val rating: Double,
    val reviewCount: Int,
    val priceLevel: BudgetLevel,
    val estimatedCostUsd: Double,
    val address: String,
    val distanceKm: Double,
    val isOpenNow: Boolean,
    val openingHours: String,
    val isIndoor: Boolean,
    val weatherSuitability: String, // "indoor_priority", "outdoor_priority", "all_weather"
    val suitableCompanions: List<String>, // "SOLO", "COUPLE", "FRIENDS", "FAMILY"
    val averageDurationMinutes: Int,
    val coverImageUrl: String,
    val galleryImages: List<String>,
    val features: List<String>,
    val latitude: Double,
    val longitude: Double,
    val whyMatchReason: String,
    val isTrending: Boolean = false,
    val isNew: Boolean = false,
    val isSaved: Boolean = false,
    val isVisited: Boolean = false,
    val isSponsored: Boolean = false,
    val businessOwnerId: String? = null
)

data class PlaceFilter(
    val query: String = "",
    val category: CategoryType = CategoryType.ALL,
    val maxBudget: BudgetLevel? = null,
    val maxDistanceKm: Double = 50.0,
    val companion: CompanionType? = null,
    val maxTimeMinutes: Int = 300,
    val openNowOnly: Boolean = false,
    val indoorOnly: Boolean = false,
    val outdoorOnly: Boolean = false,
    val minRating: Double = 0.0,
    val weatherAware: Boolean = true
)

data class ItineraryStop(
    val timeLabel: String,
    val place: Place,
    val activityTitle: String,
    val estimatedDurationMin: Int,
    val estimatedCost: String,
    val tip: String
)

data class ItineraryPlan(
    val title: String,
    val subtitle: String,
    val totalTime: String,
    val totalCost: String,
    val stops: List<ItineraryStop>,
    val weatherNote: String
)
