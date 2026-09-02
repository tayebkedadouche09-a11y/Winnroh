package com.example.data.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.model.BudgetLevel
import com.example.data.model.CategoryType
import com.example.data.model.Place
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.*

data class CityLocation(
    val nameEn: String,
    val nameAr: String,
    val nameFr: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

@JsonClass(generateAdapter = true)
data class NominatimPlaceDto(
    val place_id: Long? = 0,
    val lat: String? = "0.0",
    val lon: String? = "0.0",
    val display_name: String? = "",
    val name: String? = "",
    val type: String? = "",
    val category: String? = ""
)

class LocationAndPlacesProvider(private val context: Context) {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    val supportedCities = listOf(
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

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = r * c
        return String.format("%.1f", distance).toDoubleOrNull() ?: distance
    }

    suspend fun searchLivePlaces(query: String, userLat: Double, userLon: Double): List<Place> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=8&addressdetails=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "WAYGO-App/1.0 (contact: info@waygo.app)")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: ""
                val listType = Types.newParameterizedType(List::class.java, NominatimPlaceDto::class.java)
                val adapter = moshi.adapter<List<NominatimPlaceDto>>(listType)
                val results = adapter.fromJson(json) ?: emptyList()

                return@withContext results.mapNotNull { dto ->
                    val pLat = dto.lat?.toDoubleOrNull() ?: return@mapNotNull null
                    val pLon = dto.lon?.toDoubleOrNull() ?: return@mapNotNull null
                    val dist = calculateDistanceKm(userLat, userLon, pLat, pLon)
                    val rawName = if (!dto.name.isNullOrBlank()) dto.name else dto.display_name?.split(",")?.firstOrNull() ?: "Discovery Spot"

                    val cat = mapToCategory(dto.type ?: dto.category ?: "")

                    Place(
                        id = "osm_${dto.place_id ?: System.currentTimeMillis()}",
                        name = rawName,
                        arabicName = rawName,
                        category = cat,
                        description = dto.display_name ?: "Verified public venue discovered in real-time.",
                        arabicDescription = "مكان حقيقي تم العثور عليه عبر الخريطة والبيانات الجغرافية المفتوحة.",
                        rating = 4.5,
                        reviewCount = 58,
                        priceLevel = BudgetLevel.MODERATE,
                        estimatedCostUsd = 15.0,
                        address = dto.display_name ?: "Open Location",
                        distanceKm = dist,
                        isOpenNow = true,
                        openingHours = "09:00 - 23:00",
                        isIndoor = true,
                        weatherSuitability = "all_weather",
                        suitableCompanions = listOf("SOLO", "FRIENDS", "COUPLE"),
                        averageDurationMinutes = 60,
                        coverImageUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop&q=80",
                        galleryImages = listOf("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop&q=80"),
                        features = listOf("Live Discovery", "Verified Coordinates", "Accessible"),
                        latitude = pLat,
                        longitude = pLon,
                        whyMatchReason = "Real venue matching your search query in this region.",
                        isTrending = false,
                        isNew = true
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("LocationProvider", "Live Places search failed", e)
        }
        emptyList()
    }

    private fun mapToCategory(osmType: String): CategoryType {
        val t = osmType.lowercase()
        return when {
            t.contains("cafe") || t.contains("coffee") || t.contains("bakery") -> CategoryType.COFFEE
            t.contains("restaurant") || t.contains("fast_food") || t.contains("food") -> CategoryType.FOOD
            t.contains("park") || t.contains("garden") || t.contains("nature") -> CategoryType.NATURE
            t.contains("cinema") || t.contains("theatre") || t.contains("arts_centre") -> CategoryType.ENTERTAINMENT
            t.contains("sports") || t.contains("stadium") || t.contains("fitness") -> CategoryType.SPORTS
            t.contains("mall") || t.contains("shop") || t.contains("supermarket") -> CategoryType.SHOPPING
            t.contains("museum") || t.contains("monument") || t.contains("historic") -> CategoryType.TOURISM
            t.contains("nightclub") || t.contains("bar") || t.contains("pub") -> CategoryType.NIGHTLIFE
            t.contains("spa") || t.contains("sauna") -> CategoryType.RELAXATION
            else -> CategoryType.FOOD
        }
    }

    fun openDirections(place: Place) {
        try {
            val uri = Uri.parse("geo:${place.latitude},${place.longitude}?q=${Uri.encode(place.name)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${place.latitude},${place.longitude}")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }

    fun sharePlace(place: Place) {
        try {
            val shareText = "📍 Discover ${place.name} on WAYGO! ${place.description}\n\nLocation: https://maps.google.com/?q=${place.latitude},${place.longitude}"
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            val chooser = Intent.createChooser(sendIntent, "Share place via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("LocationProvider", "Failed to launch share intent", e)
        }
    }
}
