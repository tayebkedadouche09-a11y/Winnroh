package com.example.data.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
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
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.*

data class CityLocation(
    val nameEn: String,
    val nameAr: String,
    val nameFr: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isGpsLocation: Boolean = false
)

@JsonClass(generateAdapter = true)
data class NominatimPlaceDto(
    val place_id: Long? = 0,
    val lat: String? = "0.0",
    val lon: String? = "0.0",
    val display_name: String? = "",
    val name: String? = "",
    val type: String? = "",
    val category: String? = "",
    val address: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class OverpassElement(
    val id: Long? = 0,
    val lat: Double? = 0.0,
    val lon: Double? = 0.0,
    val tags: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class OverpassResponse(
    val elements: List<OverpassElement>? = null
)

interface PlacesProvider {
    suspend fun searchLivePlaces(query: String, userLat: Double, userLon: Double): List<Place>
    suspend fun fetchNearbyPlaces(userLat: Double, userLon: Double, radiusMeters: Int = 8000, category: CategoryType = CategoryType.ALL): List<Place>
    suspend fun geocodeAddress(query: String): CityLocation?
    suspend fun reverseGeocode(lat: Double, lon: Double): CityLocation?
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double
    fun getDeviceLocation(): CityLocation?
    fun openDirections(place: Place)
    fun sharePlace(place: Place)
}

class LocationAndPlacesProvider(private val context: Context) : PlacesProvider {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val defaultGlobalCities = listOf(
        CityLocation("Algiers", "الجزائر العاصمة", "Alger", "Algeria", 36.7538, 3.0588),
        CityLocation("Oran", "وهران", "Oran", "Algeria", 35.6987, -0.6349),
        CityLocation("Constantine", "قسنطينة", "Constantine", "Algeria", 36.3650, 6.6147),
        CityLocation("Paris", "باريس", "Paris", "France", 48.8566, 2.3522),
        CityLocation("London", "لندن", "Londres", "UK", 51.5074, -0.1278),
        CityLocation("Montreal", "مونتريال", "Montréal", "Canada", 45.5017, -73.5673),
        CityLocation("New York", "نيويورك", "New York", "USA", 40.7128, -74.0060),
        CityLocation("Riyadh", "الرياض", "Riyad", "Saudi Arabia", 24.7136, 46.6753),
        CityLocation("Dubai", "دبي", "Dubaï", "UAE", 25.2048, 55.2708),
        CityLocation("Tokyo", "طوكيو", "Tokyo", "Japan", 35.6762, 139.6503)
    )

    override fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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

    @SuppressLint("MissingPermission")
    override fun getDeviceLocation(): CityLocation? {
        try {
            val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            val providers = locManager.getProviders(true)
            var bestLoc: Location? = null
            for (p in providers) {
                val l = locManager.getLastKnownLocation(p) ?: continue
                if (bestLoc == null || l.accuracy < bestLoc.accuracy) {
                    bestLoc = l
                }
            }
            if (bestLoc != null) {
                return CityLocation(
                    nameEn = "Current Location",
                    nameAr = "موقعي الحالي",
                    nameFr = "Position actuelle",
                    country = "GPS",
                    latitude = bestLoc.latitude,
                    longitude = bestLoc.longitude,
                    isGpsLocation = true
                )
            }
        } catch (e: Exception) {
            Log.w("LocationProvider", "Could not get device GPS location", e)
        }
        return null
    }

    override suspend fun geocodeAddress(query: String): CityLocation? = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext null
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=1&addressdetails=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "WINNROH-App/1.0 (contact: info@winnroh.app)")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: ""
                val listType = Types.newParameterizedType(List::class.java, NominatimPlaceDto::class.java)
                val adapter = moshi.adapter<List<NominatimPlaceDto>>(listType).lenient()
                val results = adapter.fromJson(json) ?: emptyList()
                val first = results.firstOrNull()
                if (first != null) {
                    val lat = first.lat?.toDoubleOrNull() ?: return@withContext null
                    val lon = first.lon?.toDoubleOrNull() ?: return@withContext null
                    val city = first.address?.get("city") ?: first.address?.get("town") ?: first.address?.get("village") ?: first.name ?: query
                    val country = first.address?.get("country") ?: ""
                    return@withContext CityLocation(
                        nameEn = city,
                        nameAr = city,
                        nameFr = city,
                        country = country,
                        latitude = lat,
                        longitude = lon
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("LocationProvider", "Geocoding failed for $query", e)
        }
        null
    }

    override suspend fun reverseGeocode(lat: Double, lon: Double): CityLocation? = withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json&addressdetails=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "WINNROH-App/1.0 (contact: info@winnroh.app)")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: ""
                val adapter = moshi.adapter(NominatimPlaceDto::class.java).lenient()
                val dto = adapter.fromJson(json)
                if (dto != null) {
                    val city = dto.address?.get("city") ?: dto.address?.get("town") ?: dto.address?.get("suburb") ?: dto.address?.get("county") ?: "Local Hub"
                    val country = dto.address?.get("country") ?: "Global"
                    return@withContext CityLocation(
                        nameEn = city,
                        nameAr = city,
                        nameFr = city,
                        country = country,
                        latitude = lat,
                        longitude = lon,
                        isGpsLocation = true
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("LocationProvider", "Reverse geocode failed for $lat, $lon", e)
        }
        null
    }

    override suspend fun searchLivePlaces(query: String, userLat: Double, userLon: Double): List<Place> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=15&addressdetails=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "WINNROH-App/1.0 (contact: info@winnroh.app)")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: ""
                val listType = Types.newParameterizedType(List::class.java, NominatimPlaceDto::class.java)
                val adapter = moshi.adapter<List<NominatimPlaceDto>>(listType).lenient()
                val results = adapter.fromJson(json) ?: emptyList()

                return@withContext results.mapNotNull { dto ->
                    val pLat = dto.lat?.toDoubleOrNull() ?: return@mapNotNull null
                    val pLon = dto.lon?.toDoubleOrNull() ?: return@mapNotNull null
                    val dist = calculateDistanceKm(userLat, userLon, pLat, pLon)
                    val rawName = if (!dto.name.isNullOrBlank()) dto.name else dto.display_name?.split(",")?.firstOrNull() ?: "Discovery Venue"

                    val cat = mapToCategory(dto.type ?: dto.category ?: "")
                    val image = getCuratedCategoryImage(cat, rawName)

                    Place(
                        id = "osm_${dto.place_id ?: (pLat.hashCode() + pLon.hashCode())}",
                        name = rawName,
                        arabicName = rawName,
                        category = cat,
                        description = dto.display_name ?: "Real public venue located via live global maps.",
                        arabicDescription = "مكان تم التحقق من إحداثياته وموقعه عبر الخريطة والبيانات الجغرافية العالمية.",
                        rating = 0.0, // Real default: No fake reviews
                        reviewCount = 0,
                        priceLevel = BudgetLevel.MODERATE,
                        estimatedCostUsd = -1.0, // Unspecified
                        address = dto.display_name ?: "Address provided by map data",
                        distanceKm = dist,
                        isOpenNow = true,
                        openingHours = "Hours not listed",
                        isIndoor = isCategoryIndoor(cat),
                        weatherSuitability = if (isCategoryIndoor(cat)) "indoor_priority" else "outdoor_priority",
                        suitableCompanions = listOf("SOLO", "FRIENDS", "COUPLE", "FAMILY"),
                        averageDurationMinutes = getCategoryDefaultDuration(cat),
                        coverImageUrl = image,
                        galleryImages = listOf(image),
                        features = listOf("Live Discovery", "Verified GPS", "Open Access"),
                        latitude = pLat,
                        longitude = pLon,
                        whyMatchReason = "Real venue matching '$query'",
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

    override suspend fun fetchNearbyPlaces(
        userLat: Double,
        userLon: Double,
        radiusMeters: Int,
        category: CategoryType
    ): List<Place> = withContext(Dispatchers.IO) {
        try {
            // Build Overpass API query for real nodes around user coordinates
            val queryBody = """
                [out:json][timeout:15];
                (
                  node["amenity"~"restaurant|cafe|fast_food|cinema|theatre|nightclub|bar|ice_cream|spa|bowling_alley"](around:$radiusMeters,$userLat,$userLon);
                  node["leisure"~"park|sports_centre|fitness_centre|pitch|garden|water_park"](around:$radiusMeters,$userLat,$userLon);
                  node["tourism"~"museum|gallery|attraction|viewpoint|zoo|theme_park"](around:$radiusMeters,$userLat,$userLon);
                  node["shop"~"mall|department_store|supermarket|bakery"](around:$radiusMeters,$userLat,$userLon);
                );
                out body 35;
            """.trimIndent()

            val request = Request.Builder()
                .url("https://overpass-api.de/api/interpreter")
                .post(queryBody.toRequestBody())
                .header("User-Agent", "WINNROH-App/1.0 (contact: info@winnroh.app)")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: ""
                val adapter = moshi.adapter(OverpassResponse::class.java).lenient()
                val data = adapter.fromJson(json)
                val elements = data?.elements ?: emptyList()

                val places = elements.mapNotNull { elem ->
                    val pLat = elem.lat ?: return@mapNotNull null
                    val pLon = elem.lon ?: return@mapNotNull null
                    val tags = elem.tags ?: emptyMap()
                    val nameEn = tags["name:en"] ?: tags["name"] ?: tags["brand"] ?: return@mapNotNull null
                    val nameAr = tags["name:ar"] ?: nameEn
                    val nameFr = tags["name:fr"] ?: nameEn

                    val cat = mapTagsToCategory(tags)
                    if (category != CategoryType.ALL && cat != category) {
                        return@mapNotNull null
                    }

                    val dist = calculateDistanceKm(userLat, userLon, pLat, pLon)
                    val street = tags["addr:street"] ?: ""
                    val houseNumber = tags["addr:housenumber"] ?: ""
                    val city = tags["addr:city"] ?: ""
                    val fullAddr = listOf(houseNumber, street, city).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Coordinates: ${String.format("%.4f", pLat)}, ${String.format("%.4f", pLon)}" }

                    val opening = tags["opening_hours"] ?: "Hours not listed"
                    val phone = tags["phone"] ?: tags["contact:phone"] ?: ""
                    val website = tags["website"] ?: tags["contact:website"] ?: ""
                    val cuisine = tags["cuisine"] ?: ""
                    val fee = tags["fee"] ?: ""

                    val budget = when {
                        fee.equals("no", ignoreCase = true) -> BudgetLevel.FREE
                        tags["amenity"] == "fast_food" -> BudgetLevel.BUDGET
                        tags["amenity"] == "restaurant" && cuisine.contains("fine_dining") -> BudgetLevel.LUXURY
                        else -> BudgetLevel.MODERATE
                    }

                    val image = getCuratedCategoryImage(cat, nameEn)
                    val featuresList = mutableListOf<String>()
                    if (cuisine.isNotBlank()) featuresList.add("Cuisine: $cuisine")
                    if (tags["wheelchair"] == "yes") featuresList.add("Wheelchair Accessible")
                    if (tags["outdoor_seating"] == "yes") featuresList.add("Outdoor Seating")
                    if (tags["internet_access"] == "wlan" || tags["wifi"] == "yes") featuresList.add("Free Wi-Fi")
                    if (featuresList.isEmpty()) featuresList.addAll(listOf("Real Location", "Verified Coordinates"))

                    Place(
                        id = "osm_${elem.id ?: (pLat.hashCode() + pLon.hashCode())}",
                        name = nameEn,
                        arabicName = nameAr,
                        category = cat,
                        description = "Verified real venue in your active discovery radius. ${if (cuisine.isNotBlank()) "Specializes in $cuisine." else ""}",
                        arabicDescription = "مكان حقيقي في محيط استكشافك الجغرافي تم الحصول على بياناته مباشرة من الخرائط الحية.",
                        rating = 0.0, // Honest: 0.0 when no local reviews submitted yet
                        reviewCount = 0,
                        priceLevel = budget,
                        estimatedCostUsd = if (budget == BudgetLevel.FREE) 0.0 else -1.0,
                        address = fullAddr,
                        distanceKm = dist,
                        isOpenNow = true,
                        openingHours = opening,
                        isIndoor = isCategoryIndoor(cat),
                        weatherSuitability = if (isCategoryIndoor(cat)) "indoor_priority" else "outdoor_priority",
                        suitableCompanions = listOf("SOLO", "FRIENDS", "COUPLE", "FAMILY"),
                        averageDurationMinutes = getCategoryDefaultDuration(cat),
                        coverImageUrl = image,
                        galleryImages = listOf(image),
                        features = featuresList,
                        latitude = pLat,
                        longitude = pLon,
                        whyMatchReason = "Real venue discovered near your location",
                        isTrending = false,
                        isNew = true
                    )
                }

                if (places.isNotEmpty()) {
                    return@withContext places
                }
            }
        } catch (e: Exception) {
            Log.w("LocationProvider", "Overpass nearby places failed, trying Nominatim fallback", e)
        }

        // Fallback: search Nominatim amenity terms near coordinates
        return@withContext searchLivePlaces("food cafe park entertainment", userLat, userLon)
    }

    private fun mapTagsToCategory(tags: Map<String, String>): CategoryType {
        val amenity = tags["amenity"]?.lowercase() ?: ""
        val leisure = tags["leisure"]?.lowercase() ?: ""
        val tourism = tags["tourism"]?.lowercase() ?: ""
        val shop = tags["shop"]?.lowercase() ?: ""

        return when {
            amenity in listOf("cafe", "coffee_shop", "bakery") -> CategoryType.COFFEE
            amenity in listOf("restaurant", "fast_food", "food_court") -> CategoryType.FOOD
            amenity in listOf("cinema") -> CategoryType.CINEMA
            amenity in listOf("theatre", "arts_centre") -> CategoryType.ENTERTAINMENT
            amenity in listOf("nightclub", "bar", "pub", "lounge") -> CategoryType.NIGHTLIFE
            amenity in listOf("spa", "sauna", "massage") -> CategoryType.RELAXATION
            leisure in listOf("park", "garden", "nature_reserve") -> CategoryType.NATURE
            leisure in listOf("sports_centre", "fitness_centre", "pitch", "stadium") -> CategoryType.SPORTS
            leisure in listOf("bowling_alley", "water_park", "amusement_arcade") -> CategoryType.GAMING
            tourism in listOf("museum", "gallery") -> CategoryType.ART
            tourism in listOf("historic", "monument", "castle", "ruins") -> CategoryType.CULTURE
            tourism in listOf("attraction", "viewpoint", "theme_park") -> CategoryType.TOURISM
            shop in listOf("mall", "department_store", "supermarket") -> CategoryType.SHOPPING
            else -> CategoryType.FOOD
        }
    }

    private fun mapToCategory(osmType: String): CategoryType {
        val t = osmType.lowercase()
        return when {
            t.contains("cafe") || t.contains("coffee") || t.contains("bakery") -> CategoryType.COFFEE
            t.contains("restaurant") || t.contains("fast_food") || t.contains("food") -> CategoryType.FOOD
            t.contains("cinema") -> CategoryType.CINEMA
            t.contains("park") || t.contains("garden") || t.contains("nature") -> CategoryType.NATURE
            t.contains("game") || t.contains("arcade") || t.contains("bowling") -> CategoryType.GAMING
            t.contains("theatre") || t.contains("entertainment") -> CategoryType.ENTERTAINMENT
            t.contains("sports") || t.contains("fitness") || t.contains("stadium") -> CategoryType.SPORTS
            t.contains("mall") || t.contains("shop") || t.contains("store") -> CategoryType.SHOPPING
            t.contains("museum") || t.contains("gallery") -> CategoryType.ART
            t.contains("monument") || t.contains("historic") -> CategoryType.CULTURE
            t.contains("tourism") || t.contains("attraction") || t.contains("viewpoint") -> CategoryType.TOURISM
            t.contains("nightclub") || t.contains("bar") || t.contains("pub") -> CategoryType.NIGHTLIFE
            t.contains("spa") || t.contains("sauna") || t.contains("wellness") -> CategoryType.RELAXATION
            else -> CategoryType.FOOD
        }
    }

    private fun isCategoryIndoor(category: CategoryType): Boolean {
        return when (category) {
            CategoryType.NATURE, CategoryType.TOURISM -> false
            else -> true
        }
    }

    private fun getCategoryDefaultDuration(category: CategoryType): Int {
        return when (category) {
            CategoryType.COFFEE -> 45
            CategoryType.FOOD -> 75
            CategoryType.CINEMA -> 120
            CategoryType.GAMING -> 90
            CategoryType.NATURE -> 90
            CategoryType.SHOPPING -> 120
            CategoryType.TOURISM, CategoryType.CULTURE, CategoryType.ART -> 90
            CategoryType.SPORTS -> 60
            CategoryType.RELAXATION -> 90
            else -> 60
        }
    }

    private fun getCuratedCategoryImage(category: CategoryType, name: String): String {
        return when (category) {
            CategoryType.COFFEE -> "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800&auto=format&fit=crop&q=80"
            CategoryType.FOOD -> "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop&q=80"
            CategoryType.CINEMA -> "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80"
            CategoryType.GAMING -> "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800&auto=format&fit=crop&q=80"
            CategoryType.NATURE -> "https://images.unsplash.com/photo-1448375240586-882707db888b?w=800&auto=format&fit=crop&q=80"
            CategoryType.ENTERTAINMENT -> "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&auto=format&fit=crop&q=80"
            CategoryType.SPORTS -> "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800&auto=format&fit=crop&q=80"
            CategoryType.SHOPPING -> "https://images.unsplash.com/photo-1567449303078-57ad995bd301?w=800&auto=format&fit=crop&q=80"
            CategoryType.TOURISM, CategoryType.CULTURE -> "https://images.unsplash.com/photo-1569154941061-e231b4725ef1?w=800&auto=format&fit=crop&q=80"
            CategoryType.ART -> "https://images.unsplash.com/photo-1518998053901-5348d3961a04?w=800&auto=format&fit=crop&q=80"
            CategoryType.MUSIC -> "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&auto=format&fit=crop&q=80"
            CategoryType.NIGHTLIFE -> "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&auto=format&fit=crop&q=80"
            CategoryType.ADVENTURE -> "https://images.unsplash.com/photo-1522163182402-834f871fd851?w=800&auto=format&fit=crop&q=80"
            CategoryType.RELAXATION -> "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop&q=80"
            else -> "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop&q=80"
        }
    }

    override fun openDirections(place: Place) {
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

    override fun sharePlace(place: Place) {
        try {
            val shareText = "📍 Discover ${place.name} on WINNROH (وين نروح؟)! ${place.description}\n\nCoordinates: ${place.latitude}, ${place.longitude}\nGoogle Maps: https://maps.google.com/?q=${place.latitude},${place.longitude}"
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
